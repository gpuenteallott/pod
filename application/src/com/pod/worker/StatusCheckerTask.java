package com.pod.worker;

import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.listeners.ServerProperties;

/**
 * This class contains the logic to be performed in a periodic basis, such us the self termination procedure
 * @author will
 *
 */
public class StatusCheckerTask extends TimerTask {
	
	private static Date lastTimeWorking;
	
	public void run () {
		
		// If we have been free more time than the time to disconnect, we send a request to the manager to terminate this worker
		if ( !ExecutionPerformer.isExecutionInProcess() && new Date().getTime() - lastTimeWorking.getTime() > ServerProperties.getTerminationTime() ) {
			
			JsonObject message = new JsonObject();
			message.add( "action", Action.SELF_TERMINATION_REQUEST.getId() );
			
			HttpSender sender = new HttpSender();
			sender.setDestinationIP( ServerProperties.getManagerLocalIp() );
			sender.setDestinationRole("manager");
			sender.setMessage(message);
			
			String response = null;
			try {
				response = sender.send();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void setLastTimeWorking ( Date d ) {
		lastTimeWorking = d;
	}

}
