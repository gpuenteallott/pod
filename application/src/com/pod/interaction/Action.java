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
	INSTALL_ACTIVITY_REPORT(13),
	GET_ACTIVITY_STATUS(14),  // Send from the client to the manager in order to fetch information about the activity
	DELETE_ACTIVITY(15),  // Send from the client to the manager in order to delete an activity from the cloud
	
	NEW_EXECUTION(20),  // Sent from client to manager to ask for a new execution
	PERFORM_EXECUTION(21),  // Sent from manager to worker to make it perform the specified execution
	GET_EXECUTION_STATUS(22),
	EXECUTION_REPORT(23),
	TERMINATE_EXECUTION(24); // Send by client and by manager. Its meaning is that the given execution must be terminated
	
	
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
