package master;

import java.util.Random;

public class IdGenerator {
	
	private static int id;
	
	/**
	 * Generates a new id, that is random the first time that this method is called and then is consecutive to the previous one
	 * This method is synchronized so multiple threads won't get the same id
	 */
	public synchronized static int newId () {
		
		// If the id is 0 (init) or less than 0 (min value) then we generate another id.
		// The max value of the new id is 1,000,000, which is 3 magnitude orders less than the MAX_INTEGER value
		if ( id <= 0 ) id = new Random().nextInt(1000000);
		
		return id++;
	}
	
}
