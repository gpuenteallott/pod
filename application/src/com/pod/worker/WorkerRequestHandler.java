package com.pod.worker;

import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;
import com.pod.model.Activity;
import com.pod.model.Execution;

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
		if ( action == Action.PERFORM_EXECUTION ) {
			
			// Get message information
			JsonObject executionJson = json.get("execution").asObject();
			Execution execution = new Execution (executionJson);
			
			// Start execution in a new thread
			new Thread ( new ExecutionPerformer(execution) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			executionJson.set("status", "in progress");
			jsonResponse.add("execution", executionJson);
			return jsonResponse;
		}
		
		// Request to install a new activity, retrieving its code
		else if ( action == Action.GET_EXECUTION_PROGRESS ) {
			
			// Get message information
			JsonObject executionJson = json.get("execution").asObject();
			Execution execution = new Execution (executionJson);
			
			// Start execution in a new thread
			execution.setStdout(ExecutionPerformer.getStdout());
			execution.setStderr(ExecutionPerformer.getStderr());
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			executionJson.set("status", "in progress");
			jsonResponse.add("execution", execution.toJsonObject());
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
			
			// If the worker is busy processing something, we add the installation request to the queue
			if ( ExecutionPerformer.isExecutionInProcess() ) {
				ActivityInstallationQueue aiqueue = new ActivityInstallationQueue();
				aiqueue.put(activity);
			}
			
			// Start installer execution in a new thread
			else {
				new Thread ( new ActivityInstaller(activity) ).start();
			}
			
			// Compose response. In any case, we want the manager to know that it is in process of being installed
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
			
			// If the worker is busy processing something, we add the installation request to the queue
			if ( ExecutionPerformer.isExecutionInProcess() ) {
				ActivityInstallationQueue aiqueue = new ActivityInstallationQueue();
				aiqueue.put(activity);
				aiqueue.putFlag(activity.getId(), true);
			}
			
			// Start installer execution in a new thread. parameter true means that this is uninstallation
			new Thread ( new ActivityInstaller(activity , true) ).start();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("name", name);
			jsonResponse.add("status", "uninstalling");
			return jsonResponse;
		}
		
		// Request to terminate an execution
		else if ( action == Action.TERMINATE_EXECUTION ) {
			
			// Get message information
			JsonObject executionJson = json.get("execution").asObject();
			Execution execution = new Execution (executionJson);
			
			// Check if there is one execution in process
			if ( !ExecutionPerformer.isExecutionInProcess() ) {
				return new JsonObject().add("error", "There isn't any execution working at the moment");
			}
			
			// In case there is, stop it
			ExecutionPerformer.terminate();
			
			// Compose response
			JsonObject jsonResponse = new JsonObject();
			JsonObject executionJsonResponse = new JsonObject().add("id", execution.getId()).add("status", "terminated");
			jsonResponse.add("execution", executionJsonResponse);
			return jsonResponse;
		}
		
		return new JsonObject().add("error", "this worker doesn't recognize that request");
	}
}
