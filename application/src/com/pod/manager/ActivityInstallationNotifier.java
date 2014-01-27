package com.pod.manager;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.pod.dao.ActivityDAO;
import com.pod.dao.InstallationDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.interaction.Sender;
import com.pod.model.Activity;
import com.pod.model.Installation;
import com.pod.model.Worker;

/**
 * This class implements runnable. 
 * When executed, it retrieves all workers information and sends them a message informing them about the new activity they have to install
 * In the database, an installation record will be added with the status 'notifyingInstallation'
 */
public class ActivityInstallationNotifier implements Runnable {

	private Activity activity;
	private Action action;   //installActivity or uninstallActivity
	
	public ActivityInstallationNotifier ( Activity activity , Action action ) {
		this.activity = activity;
		this.action = action;
	}
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {

		// Retrieve all workers
		WorkerDAO wdao = new WorkerDAO();
		Worker[] workers = wdao.list();
		
		// Prepare message
		JsonObject message = new JsonObject();
		message.add("action", this.action.getId() );
		message.add("activity", activity.toJsonObject());
		
		// For each one of these
		for ( Worker worker : workers ) {
			
			// If the worker isn't already in working state, we mark it as working so no new executions will be sent during this installation
			if ( !worker.getStatus().equals("working") )
				wdao.updateStatus( worker.getId() , "working");
				
			// Create an installation record with status notifyingInstallation (in case this is an installation process)
			if ( this.action == Action.INSTALL_ACTIVITY ) {
				InstallationDAO idao = new InstallationDAO();
				idao.insert(activity.getId(), worker.getId(), "notifyingInstallation");
			}
			
			// Set the public DNS of the worker. If empty, it will mean this same machine
			Sender sender = new Sender();
			sender.setDestinationIP( worker.getDns() );
			sender.setDestinationRole("worker");
			sender.setMessage(message);
			
			try {
				sender.send();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/* If this is an uninstallation, we schedule an activity removal operation
		 * We will check if there are still workers uninstalling
		 * If there aren't, we will remove the activity from the database
		 * If there are, we will wait and try later
		*/
		if ( this.action == Action.UNINSTALL_ACTIVITY ) {
			
			int attempts = 1;
			int max_attempts = 10;
			int interval_in_ms = 10*1000;
			while (attempts <= max_attempts) {
				
				// Sleep thread. In case of looping, we make the interval longer
				try { Thread.sleep(interval_in_ms*attempts);
				} catch (Exception e) { e.printStackTrace(); }
				
				InstallationDAO idao = new InstallationDAO();
				Installation[] installations = idao.selectByActivity(activity.getId());
				
				boolean allUninstalled = true;
				for ( Installation installation : installations )
					if ( !installation.getStatus().equals("uninstalled") ) {
						allUninstalled = false; attempts++; break;
					}
				
				// If there all records have status uninstalled
				if ( allUninstalled ) {
					idao.deleteAll( activity.getId() ); // First we delete installation records
					new ActivityDAO().delete( activity.getId() ); // Then the activity, so we don't vulnerate foreign constraint
				}
			}
		}
		
		// Finish thread
	}
	
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
}
