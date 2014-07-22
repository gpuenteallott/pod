package com.pod.interaction;

/**
 * This enum includes all possible messages that can be sent INSIDE the cloud
 * It doens't include the messages that the external user sends to the system
 * Each message has an identification number
 * They must be ordenated in the code because the class internally uses binary search algorithm
 */
public enum Action {
	
	NULL(0),
	ACK(1),
	
	NEW_ACTIVITY(10),  // Sent from client to manager to indicate a new activity
	INSTALL_ACTIVITY(11),   // Sent from manager to worker to make it install the specified activity
	UNINSTALL_ACTIVITY(12),
	REPORT_ACTIVITY(13),     // Sent from the worker to the manager informing about a (un)installing activity result
	GET_ACTIVITY_STATUS(14),  // Sent from the client to the manager in order to fetch information about the activity
	DELETE_ACTIVITY(15),  // Sent from the client to the manager in order to delete an activity from the cloud
	
	NEW_EXECUTION(20),  // Sent from client to manager to ask for a new execution
	PERFORM_EXECUTION(21),  // Sent from manager to worker to make it perform the specified execution
	GET_EXECUTION_STATUS(22),  // Sent from client to manager requesting the execution status
	GET_EXECUTION_PROGRESS(23),    // Sent from manager to worker to retrieve the current stdout and stderr
	REPORT_EXECUTION(24),     // Sent from the worker to the manager informing about a finished execution
	TERMINATE_EXECUTION(25), // Sent by client and by manager. Its meaning is that the given execution must be terminated
	GET_ALL_EXECUTIONS(26), // sent by the client to obtain a summary of all executions
	
	NEW_POLICY(30),
	DELETE_POLICY(31),
	APPLY_POLICY(32),
	RESET_POLICIES(33),
	GET_ACTIVE_POLICY(34),
	GET_POLICIES(35),
	
	WORKER_DEPLOYED(40), // sent from new deployed worker to manager
	
	GET_WORKERS(50),
	
	STILL_ALIVE(55), // sent from worker to manager to inform that it is still listening for activities
	                 // it is also a way for the worker to see if the manager is alive
	
	GET_LOGS(60);
	
	
	private int id;
	
	private Action (int id) {
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	/**
	 * Returns the id of the action as a string object
	 */
	public String toString(){
		return ""+getId();
	}
	
	/**
	 * Gets the corresponding action to the id. Returns null of there's no action for that id
	 * https://en.wikipedia.org/wiki/Binary_search_algorithm
	 * @param id
	 * @return
	 */
	public static Action get(int id) {
		
		Action [] actions = Action.values();
		
		if ( id > actions[actions.length-1].getId() || id < 0 )
			return null;
		
		int imin = 0;
		int imax = actions.length-1;
		
		while (imin < imax) {
			
			int imid = (imin+imax)>>1;
			
			assert(imid < imax);
 
			if (actions[imid].getId() < id)
				imin = imid + 1;
			else
				imax = imid;
		}
 
		// deferred test for equality
		if ((imax == imin) && (actions[imin].getId() == id))
			return actions[imin];
		else
			return null;
	}
}
