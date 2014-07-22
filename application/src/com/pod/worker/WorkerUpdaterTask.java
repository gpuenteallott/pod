package com.pod.worker;

import java.io.IOException;
import java.util.TimerTask;

import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.listeners.ServerProperties;

/**
 * This scheduler is in charge of updating the manager periodically, and letting it know that the worker is alive
 * @author will
 *
 */
public class WorkerUpdaterTask extends TimerTask {

	@Override
	public void run() {
		
		JsonObject message = new JsonObject().add("action", Action.STILL_ALIVE.getId());
		
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
