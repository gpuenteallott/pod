package manager;

import java.util.Random;

/**
 * This is a very simple utility class that has a method to return a unique new id for an execution
 */
public class IdGenerator {
	
	/**
	 * When the count is started, a random value will be seleted between 0 and this value
	 */
	public static int MAX_INIT_VALUE = 1000;
	
	private static int id;
	
	/**
	 * Generates a new id, that is random the first time that this method is called and then is consecutive to the previous one
	 * This method is synchronized so multiple threads won't get the same id
	 */
	public synchronized static int newId () {
		
		// If the id is 0 (init) or less than 0 (minimum value) then we generate another id.
		// The max value of the new id is 1,000,000, which is 3 magnitude orders less than the MAX_INTEGER value
		if ( id <= 0 ) id = new Random().nextInt(MAX_INIT_VALUE);
		
		return id++;
	}
	
}
