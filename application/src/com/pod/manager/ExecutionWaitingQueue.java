package com.pod.manager;

import java.util.ArrayList;
import java.util.List;

import com.pod.model.Execution;

/**
 * Implements a queue to store executions
 * 
 * Some concurrent situations are solved, others aren't
 * See the method descriptions for more details
 */
public class ExecutionWaitingQueue {

	private static boolean initialized;
	private static List<Execution> queue;
	
	/**
	 * Create an execution queue object and initialize internal static variables
	 * This method has the initialization synchronized, so no concurrent threads start the structures at the same time
	 */
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
	
	public synchronized void put (Execution execution){
		queue.add(execution);
	}
	
	/**
	 * Pull an execution from the queue that has an activity id included in the given array
	 * It returns null if no pending execution was found
	 * 
	 * This method accesses the queue in a non synchronized way, so concurrent changes might or might not be reflected
	 * The reason behind this is avoid the bottleneck of having multiple threads trying to iterate through the queue
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
			 System.err.println(e);
		}
		return null;
	}
	/*public Execution pull ( int [] activityIds ) {
		Execution e = null;
		try {
			for ( int i = 0; i < queue.size(); i++ )
				for ( int id : activityIds )
					if ( queue.get(i) != null && queue.get(i).getActivityId() == id )
						e = queue.remove(i);
		
		} catch (IndexOutOfBoundsException ie){
			// Used to avoid problems with concurrent threads removing elements
			System.err.println(e);
		}
		return e;
	}*/
	
	/**
	 * Deletes all executions with the given activityId from the queue
	 * 
	 * This method accesses the queue in a non synchronized way, so concurrent changes might or might not be reflected
	 * The reason behind this is avoid the bottleneck of having multiple threads trying to iterate through the queue
	 * @param activityId
	 * @return an array with all execution objects removed
	 */
	public synchronized Execution[] deleteAll ( int activityId ) {
		
		List<Execution> executions = new ArrayList<Execution>();
		
		try {
			Execution e;
			for ( int i = 0; i < queue.size(); i++ ) {
				e = queue.get(i);
				if ( e != null && e.getActivityId() == activityId ) {
					executions.add(queue.remove(i)); i--;  // the size of the queue has decreased so we must reduce the index in one
				}
			}
				
		
		} catch (IndexOutOfBoundsException e){
			// Used to avoid problems with concurrent threads removing elements
			System.err.println(e);
		}
		return executions.toArray( new Execution [executions.size()] );
	}
}
