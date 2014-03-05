package com.pod.manager;

import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;

/**
 * This class handles the requests directed to a manager from inside the cloud
 * The reason to make this class is allow manager request handling either if the manager is a separate server or the same server than the worker that sends the message
 */

public class ManagerRequestHandler {
	
	
	/**
	 * Performs the request to the manager with the given json message
	 * This method is called when the request comes from another machine, but also when the request is from the same machine
	 * @param json
	 * @return
	 */
	public JsonObject doManagerRequest (JsonObject json) {
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		if ( json.get("action") == null ) {
			jsonResponse = new JsonObject();
			jsonResponse.add("error", "no action specified");
			return jsonResponse;
		}
		
		int actionId = json.get("action").asInt();
		Action action = Action.get(actionId);
		
		// New execution request from the client
		if ( action == Action.NEW_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.newExecution(json);
		}
		
		// Get execution status for client
		else if ( action == Action.GET_EXECUTION_STATUS ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.getExecutionStatus(json);
		}
		
		// A message from a worker informing about the result of an execution
		else if ( action == Action.REPORT_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.handleExecutionReport(json);
		}
		
		// A message from a worker informing about the status of the installation of a new activity
		else if ( action == Action.REPORT_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.handleActivityReport(json);
		}
		
		// A new activity has been submitted by the client to the cloud
		else if ( action == Action.NEW_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.newActivity(json);
		}
		
		// Activity status request from the client
		else if ( action == Action.GET_ACTIVITY_STATUS ) {
			ActivityHandler h = new ActivityHandler();
			return h.getActivityStatus(json);
		}
		
		// Request from the client to delete an activity
		else if ( action == Action.DELETE_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.deleteActivity(json);
		}
		
		// Request from the client to delete an activity
		else if ( action == Action.TERMINATE_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.terminateExecution(json);
		}
		
		// Request from client to create a new policy
		else if ( action == Action.NEW_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.newPolicy(json);
		}

		// Request from client to apply a policy
		else if ( action == Action.APPLY_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.applyPolicy(json);
		}

		// Request from client to delete a policy
		else if ( action == Action.DELETE_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.deletePolicy(json);
		}

		// Request from client to reset policies
		else if ( action == Action.RESET_POLICIES ) {
			PolicyHandler h = new PolicyHandler();
			return h.reset();
		}

		// Request from client to list all policies
		else if ( action == Action.LIST_POLICIES ) {
			PolicyHandler h = new PolicyHandler();
			return h.listPolicies();
		}
		
		// Request from client to view the info of the active policy
		else if ( action == Action.VIEW_ACTIVE_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.viewActivePolicy();
		}
	
		return jsonResponse.add("error", "this manager doesn't recognize that request");
	}
}
