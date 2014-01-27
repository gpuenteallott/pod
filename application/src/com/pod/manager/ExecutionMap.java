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
	private static Map<Integer, String> statuses;
	private static Map<Integer, String> errors;
	
	/**
	 * Create an execution map object and initialize internal static variables
	 * This method has the initialization synchronized, so no concurrent threads start the structures at the same time
	 */
	public ExecutionMap () {
		
		synchronized (this.getClass()){
			if ( !initialized ) {
				executions = new HashMap<Integer, Execution>();
				statuses = new HashMap<Integer, String>();
				errors = new HashMap<Integer, String>();
				initialized = true;
			}
		}
	}
	
	/**
	 * Puts a copy of the execution object into the bag, as well as the given status
	 * If the execution by its id already existed, it overrides both execution and status
	 * @param execution
	 * @param status
	 */
	public void put ( Execution execution, String status ) {
		executions.put(execution.getId(), execution);
		statuses.put(execution.getId(), status);
	}
	
	/**
	 * Puts an error description associated with the execution id
	 * It also sets the status of the execution with "error"
	 * @param executionId
	 * @param errorDescription
	 */
	public void putError ( int executionId, String errorDescription ) {
		statuses.put(executionId, "error");
		errors.put(executionId, new String(errorDescription));
	}
	
	/**
	 * Retrieves the status associated with the given execution id
	 * This method returns null of there's no info associated with that id
	 * @param executionId
	 * @return
	 */
	public String getStatus ( int executionId ) {
		return statuses.get(executionId);
	}
	
	/**
	 * Retrieves the Execution object associated with the given id and deletes it from the bag
	 * The associated status is also removed
	 * @param executionId
	 * @return
	 */
	public Execution pullExecution ( int executionId ) {
		statuses.remove(executionId);
		errors.remove(executionId);
		return executions.remove(executionId);
	}
	
	
	/**
	 * Retrieves the error description associated with the executionId
	 * Call this method only if getStatus() retrieved "error"
	 * This method will also remove the execution and its status from the bag
	 * @param executionId
	 * @return error  description
	 */
	public String pullError ( int executionId ) {
		statuses.remove(executionId);
		executions.remove(executionId);
		return errors.remove(executionId);
	}

}
