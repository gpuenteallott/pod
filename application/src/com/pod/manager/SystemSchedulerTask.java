package com.pod.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.model.Policy;
import com.pod.model.Worker;

/**
 * This class contains the logic to be performed in a periodic basis, such us the termination procedure
 * 
 * This approach to terminate workers isn't very good, although it works for now
 * The problem consists in that a task could be assigned to a worker while we are deciding if we should terminate it or not
 * @author will
 *
 */
public class SystemSchedulerTask extends TimerTask {
	
	public static int DEFAULT_TERMINATION_TIME = 45*60*1000; // 45 mins
	public static int DEFAULT_ERROR_TIMEOUT = 60*1000; // 5 mins
	
	public void run () {
		
		System.out.println("StatusCheckerTask routine");
		
		WorkerHandler wh = new WorkerHandler();
		PolicyDAO pdao = new PolicyDAO();
		Policy policy = pdao.getActive();
		
		// Before we even check if we can terminate a worker, we make sure we have room to terminate
		if ( wh.getTotalWorkers() > policy.getMinWorkers() ) {
			
			WorkerDAO wdao = new WorkerDAO();
			Worker[] workers = wdao.list();
			
			Date now = new Date();
			int terminationTime = policy.getRule("terminationTime") == null ? DEFAULT_TERMINATION_TIME : Integer.parseInt( policy.getRule("terminationTime") );
			
			List<String> instanceIds = new ArrayList<String>();
			
			for ( Worker worker : workers ) {
				
				if ( !worker.isManager() && worker.getStatus().equals("ready") 
						&& worker.getLastTimeWorked().getTime() < now.getTime() - terminationTime ) {
					instanceIds.add(worker.getInstanceId());
					worker.setStatus("terminated");
					wdao.update(worker);
				}
			}
			
			wh.terminateWorkerAction(instanceIds);
			
			System.out.println("Workers terminated="+instanceIds.size()+", terminationTime="+terminationTime+" ms");
		}
		
		WorkerDAO wdao = new WorkerDAO();
		Worker[] workers = wdao.list();
		
		// Terminate workers that are in error status
		int toTerminate = 0;
		List<String> instanceIds = new ArrayList<String>();
		for ( Worker worker : workers ) {
			if ( !worker.isManager() && worker.getStatus().equals("error") ) {
				instanceIds.add(worker.getInstanceId());
				worker.setStatus("terminated");
				wdao.update(worker);
				toTerminate++;
			}
		}
		if (instanceIds.size() > 0)
			wh.terminateWorkerAction(instanceIds);
		System.out.println("SystemSchedulerTask: found "+toTerminate+" workers that are going to be terminated due to errors");
		
		// Check if there are workers not giving signs of being active
		int errorStatusWorkers = 0;
		for ( Worker worker : workers ) {
			if ( !worker.isManager() && worker.getStatus().equals("ready") || worker.getStatus().equals("working") ) {
				if ( worker.getLastTimeAlive().getTime() + DEFAULT_ERROR_TIMEOUT < new Date().getTime() ) {
					worker.setStatus("error");
					wdao.update(worker);
					errorStatusWorkers++;
				}
			}
		}
		System.out.println("SystemSchedulerTask: found "+errorStatusWorkers+" workers with errors");
	}
}
