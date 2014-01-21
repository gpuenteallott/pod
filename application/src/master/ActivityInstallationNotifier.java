package master;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

import interaction.Sender;
import dao.ActivityDAO;
import dao.InstallationDAO;
import dao.WorkerDAO;
import model.Activity;
import model.Installation;
import model.Worker;

/**
 * This class implements runnable. 
 * When executed, it retrieves all workers information and sends them a message informing them about the new activity they have to install
 * In the database, an installation record will be added with the status 'notifyingInstallation'
 */
public class ActivityInstallationNotifier implements Runnable {

	private Activity activity;
	private String action;   //installActivity or uninstallActivity
	
	public ActivityInstallationNotifier ( Activity activity , String action ) {
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
		
		System.out.println("thread notifying workers");
		
		// Retrieve all workers
		WorkerDAO wdao = new WorkerDAO();
		Worker[] workers = wdao.list();
		
		// Prepare message
		JsonObject message = new JsonObject();
		message.add("action", this.action);
		message.add("activity", activity.toJsonObject());
		
		// For each one of these
		for ( Worker worker : workers ) {
			
			// Create an installation record with status notifyingInstallation (in case this is an installation process)
			if ( action.equals("installActivity") ) {
				InstallationDAO idao = new InstallationDAO();
				idao.insert(activity.getId(), worker.getId(), "notifyingInstallation");
			}
			
			// Set the public DNS of the worker. If empty, it will mean this same machine
			Sender sender = new Sender();
			sender.setDestination( worker.getDns() );
			sender.setMessage(message);
			System.out.println("a message is going to be sent from master to "+worker.getDns());
			try {
				String response = sender.send();
				System.out.println("thread notifying workers response "+response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/* If this is an uninstallation, we schedule an activity removal operation
		 * We will check if there are still workers uninstalling
		 * If there aren't, we will remove the activity from the database
		 * If there are, we will wait and try later
		*/
		if ( this.action.equals("uninstallActivity") ) {
			
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
	
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
