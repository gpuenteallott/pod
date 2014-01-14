package worker;

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
			
			// Get message information
			JsonObject activityJson = json.get("activity").asObject();
			String activityName = activityJson.get("activityName").asString();
			String codeLocation = activityJson.get("codeLocation").asString();
			String executeCommand = activityJson.get("executeCommand").asString();
			
			// Set up installer
			ActivityInstaller ai = new ActivityInstaller();
			ai.setActivityName(activityName);
			ai.setCodeLocation(codeLocation);
			ai.setExecuteCommand(executeCommand);
			
			// Start installer execution in a new thread
			new Thread ( ai ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("activityName", activityName);
			jsonResponse.add("status", "installing");
			return jsonResponse;
		}
		
		return null;
	}
}
