package com.pod.simulator;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class Worker {

	private static int last_id = 0;
	public static final int HISTORY_SIZE = 20;
	
	private int launchTime;
	private int id;
	
	private Activity currentActivity;
	private int executionTime;
	
	private CircularFifoQueue<Integer> queue;
	
	public Worker () {
		this.launchTime = 0;
		id = generateId();
		queue = new CircularFifoQueue <Integer> (HISTORY_SIZE);
		for ( int i = 0; i < HISTORY_SIZE; i++ )
			queue.add(0);
	}
	
	public Worker (int launchTime) {
		this.launchTime = launchTime;
		id = generateId();
	}
	
	public int getLaunchTime() {
		return this.launchTime;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getActivityId(){
		if ( currentActivity == null ) return 0;
		return currentActivity.getId();
	}
	
	public Integer [] getHistory() {
		Integer [] array = new Integer [HISTORY_SIZE];
		return queue.toArray(array);
	}
	
	private synchronized int generateId() {
		last_id++;
		return last_id;
	}
	
	public void startActivity (Activity activity) {
		this.currentActivity = activity;
		this.executionTime = 0;
		System.out.println("Activity started");
	}
	
	/**
	 * This methods performs a time iteration. Returns true if the activity was finished or false if not
	 * @return
	 */
	public boolean iterate () {
		
		if ( currentActivity == null ) {
			queue.add(0);
			return false;
		}
		executionTime++;
		
		queue.add(currentActivity.getId());
		
		if ( currentActivity.getDuration() == executionTime ) {
			currentActivity = null;
			System.out.println("Activity finished");
			return true;
		}
		
		return false;
	}
	
	public boolean isBusy () {
		return currentActivity != null;
	}
}
