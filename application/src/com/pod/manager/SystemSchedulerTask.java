package com.pod.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import main.resources.PodLogger;

import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.listeners.ServerProperties;
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
	
	public static PodLogger log = new PodLogger("SystemSchedulerTask");
	
	public void run () {
		
		WorkerHandler wh = new WorkerHandler();
		PolicyDAO pdao = new PolicyDAO();
		Policy policy = pdao.getActive();
		
		int terminatedWorkers = 0;
		int errorWorkers = 0;
		
		if ( policy == null )
			log.e("Active policy retrieved is null");
		// Before we even check if we can terminate a worker, we make sure we have room to terminate
		else if ( wh.getTotalWorkers() > policy.getMinWorkers() ) {
			
			WorkerDAO wdao = new WorkerDAO();
			Worker[] workers = wdao.list();
			
			Date now = new Date();
			int terminationTime = policy.getRule("terminationTime") == null ? ServerProperties.DEFAULT_TERMINATION_TIME : Integer.parseInt( policy.getRule("terminationTime") );
			
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
			
			terminatedWorkers = instanceIds.size();
			
		}
		
		WorkerDAO wdao = new WorkerDAO();
		Worker[] workers = wdao.list();
		
		// Terminate workers that are in error status
		List<String> instanceIds = new ArrayList<String>();
		for ( Worker worker : workers ) {
			if ( !worker.isManager() && worker.getStatus().equals("error") ) {
				instanceIds.add(worker.getInstanceId());
				worker.setStatus("terminated");
				wdao.update(worker);
				errorWorkers++;
			}
		}
		wh.terminateWorkerAction(instanceIds);
		
		// Check if there are workers not giving signs of being active
		int errorStatusWorkers = 0;
		for ( Worker worker : workers ) {
			if ( !worker.isManager() && worker.getStatus().equals("ready") || worker.getStatus().equals("working") ) {
				if ( worker.getLastTimeAlive().getTime() + ServerProperties.DEFAULT_ERROR_TIMEOUT < new Date().getTime() ) {
					worker.setStatus("error");
					wdao.update(worker);
					errorStatusWorkers++;
				}
			}
		}
		log.i("Routine: workers terminated by lack of activity = "+terminatedWorkers+", terminated by error = "+errorWorkers+", error marked = "+errorStatusWorkers);
	}
}
