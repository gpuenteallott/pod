package com.pod.manager;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.pod.dao.ActivityDAO;
import com.pod.dao.InstallationDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.model.Activity;
import com.pod.model.Execution;
import com.pod.model.Installation;
import com.pod.model.Worker;

/**
 * This class provides the necessary functions to make operations on activities, such us add or delete
 */
public class ActivityHandler {
	
	/**
	 * Creates a new activity and puts it in the information database
	 * If there is an error, the result accessible by 
	 * @param json with structure { activity: { name: "name", installationScriptLocation: "installationScriptLocation" } }
	 * @return a JsonObject representing the just created activity, including its unique id
	 */
	public JsonObject newActivity( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue activityJsonValue = json.get("activity");
		if ( activityJsonValue == null ) return new JsonObject().add("error", "Parameter activity is null");
		if ( !activityJsonValue.isObject() ) return new JsonObject().add("error", "Parameter activity isn't a json object");
		JsonObject activityJson = activityJsonValue.asObject();
		
		JsonValue nameValue = activityJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		JsonValue installationScriptLocationValue = activityJson.get("installationScriptLocation");
		if ( installationScriptLocationValue == null ) return new JsonObject().add("error", "Parameter installationScriptLocation is null");
		if ( !installationScriptLocationValue.isString() ) return new JsonObject().add("error", "Parameter installationScriptLocation isn't a string");
		String installationScriptLocation = installationScriptLocationValue.asString();
		
		// Create activity object
		Activity activity = new Activity();
		activity.setName(name);
		activity.setInstallationScriptLocation(installationScriptLocation);
		activity.setStatus("verifying");
		
		// Insert object in persistent information storage (database)
		ActivityDAO adao = new ActivityDAO();
		int activityId = adao.insert(activity);
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		// Some error happened
		if ( activityId < 0 ) {
			
			if( adao.getError().contains("Duplicate entry") ){
				return jsonResponse.add("error", "The activity name "+name+" already exists");
			}
			return jsonResponse.add("error", "Error creating the activity "+name+", retrieved id is "+activityId);
		}
		
		// Launch a thread that for every worker, sends a request to perform the installation
		new Thread ( new ActivityInstallationNotifier(activity, Action.INSTALL_ACTIVITY ) ).start();
		
		// No error happened"uninstallActivity
		return jsonResponse.add("activity", activity.toJsonObject()).add("status", "installing");
	}
	
	/**
	 * Deletes the activity. If the activity can't be deleted, the result message should be checked using getResult()
	 * @param json with structure { activity: { name: "name"} }
	 * @return a JsonObject representing the activity and a status parameter that indicates if the activity is being uninstalled
	 */
	public JsonObject deleteActivity( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue activityJsonValue = json.get("activity");
		if ( activityJsonValue == null ) return new JsonObject().add("error", "Parameter activity is null");
		if ( !activityJsonValue.isObject() ) return new JsonObject().add("error", "Parameter activity isn't a json object");
		JsonObject activityJson = activityJsonValue.asObject();
		
		JsonValue nameValue = activityJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		// Delete the activity from the database
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		// Some error might have happened
		if ( activity == null ) {
			
			if ( adao.getError() == null || adao.getError().equals("") )
				return new JsonObject().add("error", "The requested activity doesn't exist");
			else
				return new JsonObject().add("error", adao.getError());
		}
		
		// We only accept uninstallation requests if the activity status is installed or error
		// For example, if the activity status is installing, we shouldn't accept them
		if ( ! ( "installed".equals(activity.getStatus()) || "error".equals(activity.getStatus()) ) )
			return new JsonObject().add("error", "The requested activity isn't available for uninstalling yet");
		
		// Update the activity status to uninstalling so we don't accept more execution requests
		adao.updateStatus( activity.getId() , "uninstalling");
		
		// Delete pending executions of this activity from queue
		ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
		queue.deleteAll( activity.getId() );
		
		// Launch a thread that for every worker, sends a request to perform the installation
		new Thread ( new ActivityInstallationNotifier(activity, Action.UNINSTALL_ACTIVITY ) ).start();
		
		// No error happened
		return new JsonObject().add("activity", activity.toJsonObject()).add("status", "uninstalling");
	}
	
	/**
	 * Retrieves the status and saves it in the object. It can be later accessed using getResult()
	 * @param json with structure { activity: { name: "name"} }
	 * @return JsonObject representing the status of the activity and its information
	 */
	public JsonObject getActivityStatus( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue activityJsonValue = json.get("activity");
		if ( activityJsonValue == null ) return new JsonObject().add("error", "Parameter activity is null");
		if ( !activityJsonValue.isObject() ) return new JsonObject().add("error", "Parameter activity isn't a json object");
		JsonObject activityJson = activityJsonValue.asObject();
		
		JsonValue nameValue = activityJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		// Select the activity from the database
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		// Some error might have happened
		if ( activity == null ) {
			return jsonResponse.add("error", "The requested activity doesn't exist");
		}
		
		// Prepare json objects
		JsonObject jsonActivity = activity.toJsonObject();
		JsonArray installationsJson = new JsonArray();
		
		// Select the worker ids that have anything to do with this activity
		InstallationDAO idao = new InstallationDAO();
		Installation[] installations = idao.selectByActivity(activity.getId());
		
		// Iterate through array
		for ( Installation installation : installations ) {
			
			JsonObject installationJson = new JsonObject();
			installationJson.add("workerId", installation.getWorkerId()).add("status", installation.getStatus());
			
			if ( installation.getErrorDescription() != null && !installation.getErrorDescription().equals("") )
				installationJson.add("errorDescription", installation.getErrorDescription());
			
			installationsJson.add(installationJson);
		}
	    
	    jsonActivity.add("installations", installationsJson);
	    jsonResponse.add("activity", jsonActivity);
		
		return jsonResponse;
	}
	
	/**
	 * This method is triggered by the worker when there is a report on an activity installation or uninstallation
	 * The corresponding record in the database will be updated
	 * @param json with structure { activity: { status: "status", workerId: workerId} }
	 * @return
	 */
	public JsonObject handleActivityReport ( JsonObject json ) {
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		// Get the activity that it's referring to and the status of the installation
		Activity activity = new Activity(json.get("activity").asObject());
		String status = json.get("status").asString();
		int workerId = json.get("workerId").asInt();
		
		InstallationDAO idao = new InstallationDAO();
		
		// No error happened
		if ( !status.equals("error") ) {
			idao.update(activity.getId(), workerId, status);
			
			// In case the activity needed verification, we mark it as approved
			if ( "verifying".equals(activity.getStatus()) ) {
				ActivityDAO adao = new ActivityDAO();
				adao.updateStatus( activity.getId() , "approved");
			}
		}

		// In case that there was an error, we add the error description information
		else {
			idao.update(activity.getId(), workerId, status, json.get("errorDescription").asString());
			
			// If the activity needed verification, we mark it as rejected
			// We also delete all pending executions in the execution waiting queue
			if ( activity.getStatus().equals("verifying") ) {
				ActivityDAO adao = new ActivityDAO();
				adao.updateStatus( activity.getId() , "rejected");
				
				ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
				Execution[] deletedExecs = queue.deleteAll( activity.getId() );
				
				// For every pending execution for this rejected activity, we update its output status
				ExecutionMap map = new ExecutionMap();
				for ( Execution execution : deletedExecs ) {
					execution.setStatus("Activity rejected");
					map.put(execution);
				}
			}
		}
		
		// Now that the installation is completed (successfully or not), we check if there are new executions that this worker could handle
		int[] activityIds = idao.selectInstalledActivityIdsByWorker( json.get("workerId").asInt() );
		
		ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
		Execution newExecution = queue.pull(activityIds);
		
		// If no pending executions in the queue are found
		// we set the worker status to "ready" because it's available
		if ( newExecution == null ) {
			WorkerDAO wdao = new WorkerDAO();
			wdao.updateStatus( json.get("workerId").asInt() , "ready");

			jsonResponse.add("action", Action.ACK.getId());
		}
		
		// If there is a pending execution we don't change the status of the worker (keep it "working")
		else {
			// We need to get its IP address to put it in the execution map
			// This is necessary in order to allow clients to terminate executions
			WorkerDAO wdao = new WorkerDAO();
			Worker worker = wdao.select(workerId);
			ExecutionMap map = new ExecutionMap();
			map.setWorkerIP(newExecution.getId(), worker.getDns());
						
			jsonResponse.add("action", Action.PERFORM_EXECUTION.getId());
			jsonResponse.add("execution", newExecution.toJsonObject());
		}
		
		return jsonResponse;
	}
}