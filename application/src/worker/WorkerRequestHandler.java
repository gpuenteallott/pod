package worker;

import model.Activity;

import com.eclipsesource.json.JsonObject;

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
		
		String action = json.get("action").asString();
		
		// Request to install a new activity, retrieving its code
		if ( action.equals("installActivity") ) {
			
			System.out.println("worker processing install activity");
			
			// Get message information
			JsonObject activityJson = json.get("activity").asObject();
			String name = activityJson.get("name").asString();
			String codeLocation = activityJson.get("codeLocation").asString();
			String executeCommand = activityJson.get("executeCommand").asString();
			
			// Set up installer
			Activity activity = new Activity();
			activity.setName(name);
			activity.setCodeLocation(codeLocation);
			activity.setExecuteCommand(executeCommand);
			
			// Start installer execution in a new thread
			new Thread ( new ActivityInstaller(activity) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("name", name);
			jsonResponse.add("status", "installing");
			return jsonResponse;
		}
		
		return new JsonObject().add("error", "this worker doesn't recognize that request");
	}
}
