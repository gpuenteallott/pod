package com.pod.worker;

import java.util.ArrayList;
import java.util.List;

import com.pod.model.Activity;


public class ActivityInstallationQueue {
	
	private static boolean initialized;
	private static List<Activity> queue;
	
	/**
	 * Create an activity queue object and initialize internal static variables
	 * This method has the initialization synchronized, so no concurrent threads start the structures at the same time
	 */
	public ActivityInstallationQueue() {
		if (!initialized) {
			synchronized (this.getClass()){
				initialized = true;
				queue = new ArrayList<Activity>();
			}
		}
	}
	
	public int size() {
		return queue.size();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized void put (Activity activity){
		queue.add(activity);
	}
	
	public synchronized Activity pull() {
		if ( queue.size() == 0 )
			return null;
		return queue.remove(0);
	}
}
