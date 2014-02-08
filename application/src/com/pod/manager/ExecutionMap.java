package com.pod.manager;

import java.util.HashMap;
import java.util.Map;

import com.pod.model.Execution;

/**
 * This class represents a map containing all executions that are currently in the cloud
 * An execution is added when the new execution command from the client is accepted
 * An execution is removed when the client requests its status and the execution was already finished, so all the info is sent to the client and removed from here
 * 
 * TO DO ################################################################
 * 
 * An algorithm must be implemented in order to delete automatically the executions that are not removed externally before the expiration time
 * This is necessary to avoid a memory problem due to executions not retrieved by the client
 */
public class ExecutionMap {
	
	private static boolean initialized;
	private static Map<Integer, Execution> executions;
	
	private static int oldestId;
	private static int newestId;
	
	/**
	 * Create an execution map object and initialize internal static variables
	 * This method has the initialization synchronized, so no concurrent threads start the structures at the same time
	 */
	public ExecutionMap () {
		
		synchronized (this.getClass()){
			if ( !initialized ) {
				executions = new HashMap<Integer, Execution>();
				initialized = true;
			}
		}
	}
	
	/**
	 * Puts a copy of the execution object into the map
	 * If the execution by its id already existed, it overrides the previous one
	 * @param execution
	 */
	public void put ( Execution execution ) {
		executions.put(execution.getId(), execution);
		newestId = newestId > execution.getId() ? newestId : execution.getId();
		if ( oldestId == 0 ) oldestId = execution.getId();
	}
	
	/**
	 * Puts an error description associated with the execution id
	 * It also sets the status of the execution with "error"
	 * @param executionId
	 */
	public void setError ( int executionId , String error ) {
		Execution execution = executions.get(executionId);
		execution.setError(error);
		executions.put(executionId, execution);
	}
	
	/**
	 * Retrieves the error associated with the given execution id
	 * This method returns null of there's no info associated with that id
	 * @param executionId
	 * @return
	 */
	public String getError ( int executionId ) {
		return executions.get(executionId).getError();
	}
	
	/**
	 * Retrieves the associated worker IP to the given execution
	 * @param executionId
	 * @return
	 */
	public String getWorkerIP (int executionId) {
		return executions.get(executionId).getWorkerIP();
	}
	
	/**
	 * Updates the associated worker IP to the given execution
	 * @param executionId
	 * @param workerIP
	 */
	public void setWorkerIP (int executionId, String workerIP) {
		Execution execution = executions.get(executionId);
		execution.setWorkerIP(workerIP);
		executions.put(executionId, execution);
	}
	
	/**
	 * Retrieves the Execution object associated with the given id and deletes it from the map
	 * The associated status is also removed
	 * @param executionId
	 * @return
	 */
	public Execution pull ( int executionId ) {
		oldestId = executionId <= oldestId ? executionId : oldestId;
		return executions.remove(executionId);
	}
	
	/**
	 * Retrieves the execution object
	 * @param executionId
	 * @return
	 */
	public Execution get ( int executionId ) {
		return executions.get(executionId);
	}

	/**
	 * Returns the id of the newest execution from the map
	 * @return
	 */
	public static int getNewestId () {
		return newestId;
	}
	
	/**
	 * Returns the id of the olders execution from the map
	 * @return
	 */
	public static int getOldestId () {
		return oldestId;
	}
	
	/**
	 * Delete all executions that have ids between the start id (included) and end id (not included)
	 * @param startId
	 * @param endId
	 * @return
	 */
	public int deleteUntil (int endId ) {
		
		// Not go further than the newest
		if ( newestId < endId ) endId = newestId;
		
		// Update oldest id
		int oldestId2 = oldestId;
		oldestId = endId;
		
		int deleted = 0;
		for ( int i = oldestId2; i<endId; i++ ) {
			executions.remove(i); deleted++;
		}
		
		return deleted;
	}
	
}
