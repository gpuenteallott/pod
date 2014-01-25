package interaction;

/**
 * This enum includes all possible messages that can be sent INSIDE the cloud
 * It doens't include the messages that the external user sends to the system
 * Each message has an identification number
 * They must be ordenated in the code because the class internally uses binary search algorithm
 */
public enum Action {
	
	NULL(0),
	ACK(1),
	
	INSTALL_ACTIVITY(10),
	UNINSTALL_ACTIVITY(11),
	INSTALL_ACTIVITY_REPORT(12),
	
	NEW_EXECUTION(20),
	EXECUTION_REPORT(21);
	
	
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
