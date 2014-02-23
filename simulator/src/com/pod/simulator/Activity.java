package com.pod.simulator;

public class Activity {
	
	private static int last_id = 0;
	
	private int duration;
	private int id;
	
	public Activity (int duration) {
		this.duration = duration;
		id = generateId();
	}
	
	public int getDuration() {
		return this.duration;
	}
	
	public int getId(){
		return this.id;
	}
	
	private synchronized int generateId() {
		last_id++;
		return last_id;
	}
}
