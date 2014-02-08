package com.pod.manager;

import java.util.Date;
import java.util.TimerTask;

import com.pod.listeners.ServerProperties;
import com.pod.model.Execution;

/**
 * This class takes care of removing the expired executions from the output map
 * Otherwise, if the client doesn't retrieve the execution output, the map would fill
 */
public class ExecutionMapCleaner extends TimerTask {
	
	private static boolean running;

	/**
	 * Returns the flag indicating if the cleaner has been initialized
	 * @return
	 */
	public static boolean isRunning() {
		return running;
	}
	
	public ExecutionMapCleaner(){
		running = true;
	}


	@Override
	public void run() {
		
		long now = new Date().getTime();
		
		int newestId = ExecutionMap.getNewestId();
		int oldestId = ExecutionMap.getOldestId();
		
		int chunk = ServerProperties.getExecutionMapChunk();
		
		ExecutionMap map = new ExecutionMap();
		
		int deleted = 0;
		
		// While we don't remove executions newer than the newest execution id
		while ( oldestId + chunk < newestId ) {
			
			System.out.println("Iterating. oldest="+oldestId+ " chunk="+chunk+" newest="+newestId+ " deleted="+deleted);
			
			Execution execution = map.get( oldestId + chunk );
			long startTime = execution.getStartTime();
			
			// If the execution was started before than the expiration time ago, delete
			if ( startTime < now - ServerProperties.getExecutionMapExpirationTimeout() ) {
				System.out.println("positive");
				deleted += map.deleteUntil(oldestId + chunk);
				// Update oldest id value
				oldestId = ExecutionMap.getOldestId();
			}
			else {
				System.out.println("negative"); break;
			}
			
		}
		
		// Logging
			System.out.println();
			System.out.println("ExecutionMapCleaner. Deleted "+deleted+" executions. New oldest execution id "+ExecutionMap.getOldestId());
			System.out.println();
		// End logging
	}
}
