package worker;

import interaction.Action;
import model.Activity;
import model.Execution;

import com.eclipsesource.json.JsonObject;

/**
 * This class handles the requests directed to a worker
 * The reason to make this class is allow worker request handling either if the worker is a separate server or the same server than the manager
 */
public class WorkerRequestHandler {
	
	/**
	 * Performs the request to a worker with the given json message
	 * This method is called when the request comes from another machine, but also when the request is from the same machine
	 * @param json
	 * @return
	 */
	public JsonObject doWorkerRequest (JsonObject json) {
		
		if ( json.get("action") == null ) {
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("error", "no action specified");
			return jsonResponse;
		}
		
		int actionId = json.get("action").asInt();
		Action action = Action.get(actionId);
		
		// Request to start a new execution of a given activity
		if ( action == Action.NEW_EXECUTION ) {
			
			// Get message information
			JsonObject executionJson = json.get("execution").asObject();
			Execution execution = new Execution (executionJson);
			
			// Start execution in a new thread
			new Thread ( new ExecutionPerformer(execution) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("execution", executionJson);
			jsonResponse.add("status", "inProgress");
			return jsonResponse;
		}
		
		// Request to install a new activity, retrieving its code
		else if ( action == Action.INSTALL_ACTIVITY ) {
			
			// Get message information
			JsonObject activityJson = json.get("activity").asObject();
			int id = activityJson.get("id").asInt();
			String name = activityJson.get("name").asString();
			String installationScriptLocation = activityJson.get("installationScriptLocation").asString();
			String status = activityJson.get("status").asString();
			
			// Set up installer
			Activity activity = new Activity();
			activity.setId(id);
			activity.setName(name);
			activity.setInstallationScriptLocation(installationScriptLocation);
			activity.setStatus(status);
			
			// Start installer execution in a new thread
			new Thread ( new ActivityInstaller(activity) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("name", name);
			jsonResponse.add("status", "installing");
			return jsonResponse;
		}
		
		// Request to uninstall an activity
		else if ( action == Action.UNINSTALL_ACTIVITY ) {
			
			// Get message information
			JsonObject activityJson = json.get("activity").asObject();
			int id = activityJson.get("id").asInt();
			String name = activityJson.get("name").asString();
			String installationScriptLocation = activityJson.get("installationScriptLocation").asString();
			
			Activity activity = new Activity();
			activity.setId(id);
			activity.setName(name);
			activity.setInstallationScriptLocation(installationScriptLocation);
			
			// Start installer execution in a new thread. parameter true means that this is uninstallation
			new Thread ( new ActivityInstaller(activity , true) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("name", name);
			jsonResponse.add("status", "uninstalling");
			return jsonResponse;
		}
		
		return new JsonObject().add("error", "this worker doesn't recognize that request");
	}
}