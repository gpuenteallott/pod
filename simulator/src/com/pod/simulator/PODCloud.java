package com.pod.simulator;

import java.util.ArrayList;
import java.util.List;

public class PODCloud {

	private List<Activity> activityQueue;
	private List<Worker> workers;
	
	public PODCloud () {

		activityQueue = new ArrayList<Activity>();
		workers = new ArrayList<Worker>();
	}
	
	public void iterate () {
		
		System.out.println("Iterating ------------");
		
		for ( Worker worker : workers ) {
			worker.iterate();
			if ( !worker.isBusy() && !activityQueue.isEmpty() ) {
				worker.startActivity(activityQueue.remove(0));
			}
		}
		
		GUI.repaintPOD();
	}
	
	public void newActivity ( Activity activity ) {
		
		activityQueue.add(activity);
	}
	
	public void newWorker ( Worker worker ) {
		
		workers.add(worker);
	}
	
	public List<Worker> getWorkers() {
		return workers;
	}
	
	public int getQueueSize (){
		return activityQueue.size();
	}
	
	public void setup() {
		Worker w1 = new Worker();
		Worker w2 = new Worker();
		Worker w3 = new Worker();
		Activity a1 = new Activity (2);
		Activity a2 = new Activity (3);
		
		newWorker(w1); newWorker(w2); newWorker(w3);
		newActivity(a1); newActivity(a2);newActivity(a1);newActivity(a1);newActivity(a1);
		newActivity(a1); newActivity(a2);newActivity(a1);newActivity(a1);newActivity(a1);
		newActivity(a1); newActivity(a2);newActivity(a1);newActivity(a1);newActivity(a1);
	}
	
	public static void main (String [] args) {
		
		PODCloud pod = new PODCloud();
		
		Worker w1 = new Worker();
		Worker w2 = new Worker();
		Activity a1 = new Activity (2);
		Activity a2 = new Activity (3);
		
		pod.newWorker(w1); pod.newWorker(w2);
		pod.newActivity(a1); pod.newActivity(a2);
		
		for ( int i = 0; i < 10; i++ ) {
			
			pod.iterate();
			
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
