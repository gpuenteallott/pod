package com.pod.worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.manager.WorkerHandler;
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
public class StatusCheckerTask extends TimerTask {
	
	public static int DEFAULT_TERMINATION_TIME = 45*60*1000; // 45 mins
	
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
	}
}
