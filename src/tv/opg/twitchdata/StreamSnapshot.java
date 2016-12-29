/**
 * Represents characteristics of a Twitch channel at a specific moment while broadcasting.
 * All members of this class are final, and should not be changed.
 * 
 * Class StreamSnapshot
 * Bugs: none known
 *
 * @author       Jeremy Owens
 * @company      OP Group
 * @version      1.0
 * @since        2016-12-28
 * @see also     Broadcast
 */
package tv.opg.twitchdata;

import java.sql.Timestamp; //Standard for storing the time, initially designed for use in a relational database

public class StreamSnapshot  implements Comparable<StreamSnapshot> {
	/**The channel on which the broadcast occurs*/
	public final String CHANNEL;
	/**The time at which the data were captured*/
	public final Timestamp TIME;
	/**The game being played at this moment*/
	public final String GAME;
	/**The number of viewers at this moment*/
	public final long VIEWERS;
	/**The number of channel followers at this moment*/
	public final long FOLLOWERS;
	
	/**
	 * Block usage of the generic constructor.
	 */
	public StreamSnapshot() {
		throw new UnsupportedOperationException("Must be instantiated with all members.");
	}
	
	/**
	 * Explicit constructor defining all members.
	 * @param channel String
	 * @param time java.sql.Timestamp
	 * @param game String
	 * @param viewers long
	 * @param followers long
	 */
	public StreamSnapshot(String channel, Timestamp time, String game, long viewers, long followers){
		this.CHANNEL = channel;
		this.TIME = time;	
		this.GAME = game;
		this.VIEWERS = viewers;
		this.FOLLOWERS = followers;
	}
	
	@Override
	public int compareTo(StreamSnapshot o) {
		return (int)(this.TIME.getTime() - o.TIME.getTime());
	}
	@Override
	public int hashCode() {
		int hash = 13;
		hash += this.TIME.getTime() + this.CHANNEL.hashCode();
		return hash;
	}
	@Override
	public boolean equals(Object iL) {
		if (!StreamSnapshot.class.isAssignableFrom(iL.getClass())) {
	        return false;
	    }
		final StreamSnapshot other = (StreamSnapshot) iL;	
		if ((this.TIME.getTime() == other.TIME.getTime()) && this.CHANNEL.equals(other.CHANNEL)) return true;
		return false;
	}

}
