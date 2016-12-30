
/**
 * Represents a continuous unit of broadcast time, and contains methods that
 * calculate performance metrics.
 * 
 * Class Broadcast
 * Bugs: none known
 *
 * @author       Jeremy Owens
 * @company      OP Group
 * @version      1.0
 * @since        2016-12-28
 * @see also     StreamSnapshot
 */

package tv.opg.twitchdata;

import java.sql.Timestamp; //All time elements stored in the format to facilitate interaction with relational databases
import java.util.ArrayList; //Used for sorting
import java.util.SortedSet; //Parent of TreeSet
import java.util.TreeSet; //Used to store ordered StreamSnapshot elements
import org.apache.commons.math3.stat.regression.SimpleRegression; //Required to calculate the Subsequent Game Slope

public class Broadcast {
	/**Set of StreamSnapshots ordered by the Timestamp*/
	private TreeSet<StreamSnapshot> snapshots;
	/**A saved reference to the largest VIEWER value for each StreamSnapshot in snapshots */
	private long peak;
	/**A saved reference to the sum of VIEWER values in StreamSnapshots in snapshots
	 * This is kept updated to avoid repetitive looping through the TreeSet*/
	private long vsum;
	
	/**
	 * This method denied use of the standard constructor.
	 */
	public Broadcast() {
		throw new UnsupportedOperationException("Broadcasts must be instantiated with a first StreamSnapshot.");
	}
	
	/**Basic constructor- instantiates the collection to store StreamSnapshots
	 * and assigns initial values for the stored calculated members peak, and vsum.
	 * @param ss the fist StreamSnapshot added to the Broadcast
	 */
    public Broadcast(StreamSnapshot ss) {
    	snapshots = new TreeSet<>(); //Instantiate TreeSet
    	snapshots.add(ss);           //Add first snapshot to the Treeset
    	this.peak = ss.VIEWERS;      //Since there is only one snapshot peak is assigned to the VIEWERS value of the ss parameter
    	this.vsum = ss.VIEWERS;      //Since there is only one snapshot vsum is assigned to the VIEWERS value of the ss parameter
    }
    
    /**
     * This method adds a snapshot to the set of snapshots.
     * @param ss The StreamSnapshot to be added to the set.
     */
    public void addSnapShot(StreamSnapshot ss) {
    	
    	if (!ss.CHANNEL.equals(getChannel()))               //Broadcasts are defined to contain only StreamSnapshots from one channel
    		throw new IllegalArgumentException("All snapshots in a Broadcast must have the same CHANNEL name.");
    	snapshots.add(ss);                                  //Add snapshot to set.
    	if (ss.VIEWERS > this.peak) this.peak = ss.VIEWERS; //If ss has a higher VIEWER value than peak, assign peak to the new value.
    	this.vsum += ss.VIEWERS;                            //Add ss.VIEWERS to the ongoing viewer sum reference
    }
    
    /**
     * Getter for the channel on which the broadcast occurred. No extra reference is stored
     * in this class, since StreamSnapshots already store it.
     * @return String The channel on which the broadcast occurred, all lowercase.
     */
    public String getChannel() {
    	return this.snapshots.first().CHANNEL; //Access public final member in the first StreamSnapshot in the TreeSet and return it.
    }
    
    /**
     * Getter for the time at which the first StreamSnapshot was captured. 
     * 
     * @return java.sql.Timestamp 
     */
    public Timestamp getStart() {
    	return snapshots.first().TIME;  //Access public final member in the first StreamSnapshot in the TreeSet and return it.
    }
    
    /**
     * Getter for the time at which the last StreamSnapshot was captured.
     * @return java.sql.Timestamp
     */
    public Timestamp getEnd() {
    	return snapshots.last().TIME;  //Access public final member in the last StreamSnapshot in the TreeSet and return it.
    }
    
    /**
     * Getter for the difference in time between first and last snapshots
     * Adds the average difference in timestamps to account for margin of error. 
     * @return int The amount of time in minutes
     */
    public int getLength() {
    	if (snapshots.size() > 1) {
    		//The initial project used snapshots 6 minutes apart, but this method is designed to work with any frequency.
    		int l = (int)(getEnd().getTime()-getStart().getTime())/60000; //Difference between snapshots in minutes.
        	return l + l/(snapshots.size()-1); //Difference plus the average difference between snapshots
    	  }   
    	else return 0;
    }
    
    /**
     * Getter for the highest VIEWER number captured.
     * @return
     */
    public long getPeak() {
    	return peak; //Since a reference is updated every time a snapshot is added, return that value.
    }
    
    /**
     * Method to return the average number of viewers present during the broadcast.
     * @return long
     */
    public long avgViewers() {
    	return vsum/snapshots.size(); //Since the sum is maintained, simply divide by the number of snapshots.
    }
    
    /**
     * Method to return the change in follower count during the stream.
     * @return long
     */
    public long getFollowerDelta() {
    	//Access public final members of the first and last snapshots. Returns the difference
    	return snapshots.last().FOLLOWERS - snapshots.first().FOLLOWERS; 
    }
    
    /**
     * Calculates and returns the standard deviation of the VIEWER members of each StreamSnapshot
     * @return int
     */
    public int getSameStreamViewerVariance() { 
    	long mean = vsum/snapshots.size();                //Determine VIEWER mean
    	int sqSum = 0;                                    //Define reference for the sum of the squares
    	for (StreamSnapshot ss: snapshots) {              //Loop through snapshots
    		sqSum += Math.pow(ss.VIEWERS-mean, 2);        //Square the difference between VIEWERS and the MEAN
    	}
    	return (int) Math.sqrt(sqSum/snapshots.size());   //Take the square root of the sum of difference squares
    	
    }
    
    /**
     * Method to return the point at which the highest viewership 20% of a stream starts.
     * Value is represented as a percentage of the total broadcast time between 0 and 80.
     * @return int
     */
    public int getPeakFifthStart() {
    	int step = snapshots.size()/5;               //Determine the number of snapshots present in 1/5 of the broadcast
    	long topSum = 0;                             //Create reference for the highest viewer sum and set to zero
    	double tsPos = 0.0;                               //Create reference for the index of the beginning of the highest viewer period
    	long[] fifth = new long[step];               //Instantiate array to fit the 1/5 of viewer values
    	StreamSnapshot[] sArr = new StreamSnapshot[snapshots.size()];
    	sArr = snapshots.toArray(sArr);              //Create array from TreeSet so snapshots can be accessed by index
    	
    	for (int i = 0; i < sArr.length-step; i++) { //Step through array until remaining values can no longer fit in the fifth array
    		for (int j = 0; j < step; j++) {         //Build array of the 1/5 of VIEWERS values starting at i
    			fifth[j] = sArr[i+j].VIEWERS;
    		}
    		int sum = 0;                             //Declare reference for the sum
    		for (long l: fifth) {                    //Add the VIEWERS values in the array
    			sum += l;
    		}
    		if (sum > topSum) {                      //If the sum of this array is the highest, save it's position
    			topSum = sum;
    			tsPos = i;
    		}
    	}
    	//Return the starting percentage, represented by the index of the start + 1 divided
    	//by the size of the array and multiplied by 100.
    	//Cast to int for implicit rounding.
    	return (int) (((tsPos+1)/snapshots.size())*100);     
    }
    
    /**
     * Return the viewership slopes of any subsequent games played
     * @return double[] an array of the slopes of regression lines for any number of subsequent games
     */
    public double[] getSubsequentGameSlopes() {
    	ArrayList<StreamSnapshot[]> gameSplits = new ArrayList<>();   //Instantiate a list to contain the first and last snapshots for each game
    	String currentGame = null;                                    //Declare variable to reference the name of the current game
    	for (StreamSnapshot ss:snapshots) {                           //Iterate through snapshots
    		/*
    		 * If the game in the snapshot is the first or not the same as the previous, 
    		 * Instantiate a new array[2] with the current snapshot as the first and second
    		 * element, representing the first and last snapshots in the game split.
    		 * Add the array to the list of game splits, and set the current game to the
    		 * GAME of the current snapshot.
    		 */

    		if (currentGame == null || !ss.GAME.equals(currentGame)) {
    			currentGame = ss.GAME;
    			StreamSnapshot[] sArr = {ss, ss};
    			gameSplits.add(sArr);
    		}
    		/*
    		 * If the game is the same as the previous snapshot, update the second element
    		 * in the array, representing the last snapshot of this game split.
    		 */
    		
    		else {
    			gameSplits.get(gameSplits.size()-1)[1] = ss;
    		}
    	}
    	
    	assert(gameSplits.size() != 0);//Should never be zero.
    	if (gameSplits.size() == 1) {  //If gameSplits only has one element, there are no SubsequentGameSlope values. Return null.
    		return null;
    	}
    	gameSplits.remove(0);                            //Remove the first gamesplit. It is not a subsequent game, by definition.
    	double[] slopes = new double[gameSplits.size()]; //Instantiate array to be returned.
    	int i = 0;
    	for (StreamSnapshot[] sArr: gameSplits) {
    		SortedSet<StreamSnapshot> set = snapshots.subSet(sArr[0], sArr[1]);
    		if (set.size() < 2) { 
    			//If there is only one snapshot in the subset, set the value in the array to 0.0 and increment i.
    			slopes[i++] = 0.0;
    			
    			//There is no need to complete further logic in this method. If this is the last or only element, return the array.
    			//Otherwise move on to the next element.
    			if (gameSplits.size() == 1 || i >= gameSplits.size()) return slopes;
    			else continue;
    		}
    		SimpleRegression regression = new SimpleRegression(); //Instantiate regression
    		Timestamp start = set.first().TIME;
    		for (StreamSnapshot ss:set) {
    			//Add the minutes after the start, and the number of viewers to the regression.
    			regression.addData((ss.TIME.getTime()-start.getTime())/60000, ss.VIEWERS);
    		}
    		slopes[i++] = regression.getSlope(); //Calculate regression, add to array, increment i.
    	}
    	return slopes;
    }
    
    /**
     * Method to return split averages by game session during the broadcast
     * @return long[] an array of all game session viewership averages
     */
    public long[] getAvgsByGame() {
    	ArrayList<Long> avgList = new ArrayList<>();
    	
    	String currentGame = snapshots.first().GAME;
    	long currentSum = 0;
    	int count = 0;
    	
    	for (StreamSnapshot ss: snapshots) { //Iterate through snapshots. Sum viewers, and count snapshots.
    		if (!currentGame.equals(ss.GAME)) { //If GAME changes, calculate average, add to list, reset sum and count
    			avgList.add(new Long(currentSum/count));
    			currentGame = ss.GAME;
    			currentSum = ss.VIEWERS;
    			count = 1;
    		}
    		else {
    			currentSum += ss.VIEWERS;
    			count++;
    		}
    	}
    	avgList.add(new Long(currentSum/count)); //Add last element to list
    	
    	//Convert list to long[]
    	long[] arr = new long[avgList.size()];
    	for (int i = 0; i < arr.length; i++) {
    		arr[i] = avgList.get(i).longValue();
    	}
    	return arr;
    }
    
    /**
     * Method that returns the first VIEWER count after 29 minutes of stream time have passed.
     * @return
     */
    public int getRampUp() {
    	if (this.getLength() <= 30) { //If stream is less than or equal to 30 minutes long, return the last viewer count.
    		return (int) snapshots.last().VIEWERS;
    	}
    	//Otherwise, iterate through the snapshots, and the capture the VIEWER value of the first one that occurs 29
    	//minutes after the beginning.
    	else {                       
    		int ramp = 0;
    		for (StreamSnapshot ss: snapshots) {
    			if ((ss.TIME.getTime() - snapshots.first().TIME.getTime())/60000 > 29) ramp =- (int) ss.VIEWERS;
    		}
    		return ramp;
    	}
 
    }
    
    @Override
    public String toString() {
    	return getChannel() + ", " + getStart() + ", " + getEnd() + ", " + getLength() + ", " + avgViewers();
    }
}
