package manager;

import java.util.ArrayList;
import java.util.List;

import model.Execution;

/**
 * Implements a queue to store executions
 * 
 * Some concurrent situations are solved, other aren't yet
 */
public class ExecutionWaitingQueue {

	private static boolean initialized;
	private static List<Execution> queue;
	
	public ExecutionWaitingQueue() {
		if (!initialized) {
			synchronized (this.getClass()){
				initialized = true;
				queue = new ArrayList<Execution>();
			}
		}
	}
	
	public int size() {
		return queue.size();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public void put (Execution execution){
		System.out.println(execution.toJsonObject().toString());
		queue.add(execution);
	}
	
	/**
	 * Pull an execution from the queue that has an activity id included in the given array
	 * It returns null if no pending execution was found
	 * @param activityIds
	 * @return 
	 */
	public Execution pull ( int [] activityIds ) {
		
		try {
			for ( int i = 0; i < queue.size(); i++ )
				for ( int id : activityIds )
					if ( queue.get(i) != null && queue.get(i).getActivityId() == id )
						return queue.remove(i);
		
		} catch (IndexOutOfBoundsException e){
			// Used to avoid problems with concurrent threads removing elements
		}
		return null;
	}
	
	/**
	 * Deletes all executions with the given activityId from the queue
	 * @param activityId
	 */
	public void deleteAll ( int activityId ) {
		
		try {
			for ( int i = 0; i < queue.size(); i++ )
				if ( queue.get(i).getActivityId() == activityId ) {
					queue.remove(i); i++;
				}
		
		} catch (IndexOutOfBoundsException e){
			// Used to avoid problems with concurrent threads removing elements
		}
	}
}
