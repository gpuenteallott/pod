package com.pod.manager;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.pod.dao.ActivityDAO;
import com.pod.dao.InstallationDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.model.Activity;
import com.pod.model.Execution;
import com.pod.model.Worker;

public class ExecutionHandler {

	/**
	 * Handles the event of a new execution
	 * This method will check that the given activity is valid. That means, it is in the system and its source code has been verified
	 * After the check, this method will look for an available worker to send the execution request right away
	 * If no worker is available at the moment, the execution request will be put in the queue
	 * @param json with this structure { execution: { name: "name", input: "input" } }
	 * @return JsonObject with the response for the client that requested the new execution
	 */
	public JsonObject newExecution( JsonObject json ) {
		
		// Validate parameters
		JsonValue executionJsonValue = json.get("execution");
		if ( executionJsonValue == null ) return new JsonObject().add("error", "Parameter execution is null");
		if ( !executionJsonValue.isObject() ) return new JsonObject().add("error", "Parameter execution isn't a json object");
		JsonObject executionJson = executionJsonValue.asObject();
		
		JsonValue nameValue = executionJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		JsonValue inputValue = executionJson.get("input");
		String input = null; // Parameter input can be null
		if ( inputValue != null && !inputValue.isString() ) return new JsonObject().add("error", "Parameter input isn't a string");
		if ( inputValue != null ) input = inputValue.asString();
		
		// Prepare response
		JsonObject jsonResponse = new JsonObject();
		
		// Verify that the given activity exists in the system
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		if ( activity == null ) {
			if ( adao.getError() == null || adao.getError().equals("") )
				return jsonResponse.add("error", "The requested activity doesn't exist");
			else
				return jsonResponse.add("error", adao.getError());
		}
		
		// Check that its status is approved or in process of being verified
		if ( !"approved".equals(activity.getStatus()) && !"verifying".equals(activity.getStatus()) )
			return jsonResponse.add("error", "The requested activity isn't approved for executions. Its current status is "+activity.getStatus());
		
		// Get a new execution ID to identify this execution
		int executionId = IdGenerator.newId();
		
		Execution execution = new Execution();
		execution.setId(executionId);
		execution.setStdin(input);
		execution.setActivityName(name);
		execution.setActivityId(activity.getId());
		execution.setStatus("in progress");
		
		// Check if there are completed installations for this worker
		// If there are, we will try to send the execution request directly to the worker
		WorkerDAO wdao = new WorkerDAO();
		Worker worker = null;  // This variable isn't static, belong to the object, so no concurrent executions will override the value assigned in the sync statement
		
		// This lines of code can only be being executed by one thread because the lock is associated with the class, not the object
		synchronized (this.getClass()) {
			
			worker = wdao.getAvailableByActivityAndStatus( activity.getId() , "ready");
			
			// If we got a worker, set it as 'working'
			if ( worker != null ) {
				
				worker.setStatus("working");
				wdao.update(worker);
			}
		}
		
		// If in the sync method a worker was assigned, we sent it a message so it starts the execution
		if ( worker != null ) {
			
			// Prepare message
			JsonObject message = new JsonObject();
			message.add("action", Action.PERFORM_EXECUTION.getId() );
			message.add("execution", execution.toJsonObject());
			
			// Set the public DNS of the worker. If empty, it will mean this same machine
			HttpSender sender = new HttpSender();
			sender.setDestinationIP( worker.getDns() );
			sender.setDestinationRole("worker");
			sender.setMessage(message);
			
			boolean sent = false;
			try {
				sender.send();
				sent = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// If the message was sent, everything is wonderful
			if ( sent ) {
				jsonResponse.add("execution", execution.toJsonObject());
				
				// We put the execution in the execution map including the workerIP
				ExecutionMap bag = new ExecutionMap();
				execution.setWorkerIP( worker.getDns() );
				bag.put(execution);
				
			}
			else {
				// If there was an error sending this execution, problematic
				// We put the worker with error status, so no more executions are sent to this worker
				worker.setStatus("error");
				wdao.update(worker);

				ExecutionMap bag = new ExecutionMap();
				bag.put(execution);
				
				/*
				 * ERROR HANDLING
				 * SOMETHING MUST BE DONE HERE
				 * Put the execution in the queue for example (easy), or
				 * Look for a new worker to send this message (hard)
				 */
				// For now, we send an error message
				jsonResponse.add("error", "Couldn't contact with a worker to start this execution with id "+executionId);
			}
		}
		
		// In case there is no worker available, the execution request must go to the waiting queue
		else {
			
			ExecutionMap bag = new ExecutionMap();
			bag.put(execution);
			
			ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
			queue.put(execution);
			jsonResponse.add("execution", execution.toJsonObject());
		}
		
		return jsonResponse;
	}

	
	/**
	 * Fetches the execution status and information
	 * If the execution is done, the json response will contain the execution in json format
	 * If there was an error, the json response will contain the error description
	 * In any other case, the json response will only inform about the current status
	 * @param json with this structure { execution: { id: id } }
	 * @return
	 */
	public JsonObject getExecutionStatus (JsonObject json) {
		
		// Validate parameters
		JsonValue executionJsonValue = json.get("execution");
		if ( executionJsonValue == null ) return new JsonObject().add("error", "Parameter execution is null");
		if ( !executionJsonValue.isObject() ) return new JsonObject().add("error", "Parameter execution isn't a json object");
		JsonObject executionJson = executionJsonValue.asObject();
		
		JsonValue executionIdValue = executionJson.get("id");
		if ( executionIdValue == null ) return new JsonObject().add("error", "Parameter id is null");
		if ( !executionIdValue.isNumber() ) return new JsonObject().add("error", "Parameter id isn't a string");
		int executionId = executionIdValue.asInt();
		
		// Retrieve execution status
		ExecutionMap map = new ExecutionMap();
		Execution execution = map.get(executionId);
		
		// In case the execution isn't in the bag
		if ( execution == null ) {
			return new JsonObject().add("error", "Execution with id "+executionId+" doesn't exist, its result has already been retrieved or it expired");
		}
		
		// In case the execution is done, we pull it (remove it)
		if ( "finished".equals(execution.getStatus()) || "error".equals(execution.getStatus()) ) {
			return new JsonObject().add("execution", map.pull(executionId).toJsonObject());
		}
		
		// In case it's "in progress", we ask the worker about the progress
		if ( "in progress".equals(execution.getStatus()) ) {
			
			JsonObject message = new JsonObject();
			message.add("action", Action.GET_EXECUTION_PROGRESS.getId());
			message.add("execution", new JsonObject().add("id", executionId));
			
			HttpSender sender = new HttpSender();
			sender.setDestinationIP( execution.getWorkerIP() );
			sender.setDestinationRole("worker");
			sender.setMessage(message);
			String response = null;
			try {
				response = sender.send();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// We add the stdout and stderr to the execution object retrieved from the map
			// That way, the response execution object contains all the info
			JsonObject responseJson = JsonObject.readFrom(response);
			execution.setStdout( responseJson.get("execution").asObject().get("stdout").asString() );
			execution.setStderr( responseJson.get("execution").asObject().get("stderr").asString() );
			return execution.toJsonObject();
		}
		
		// Otherwise, we just get it
		return new JsonObject().add("execution", map.get(executionId).toJsonObject());
	}

	/**
	 * This method will be invoked by a request from a worker
	 * It will read the execution object passed via JSON and put it in the Execution map so the client can retrieve it
	 * @param json with this structure { execution: { status : "status" } }
	 * @return
	 */
	public JsonObject handleExecutionReport ( JsonObject json ) {
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		Execution execution = new Execution (json.get("execution").asObject());
		
		// Put the execution object in the bag, so the client can retrieve it later
		ExecutionMap map = new ExecutionMap();
		map.put(execution);
		
		// In case there was an error in the worker with the execution
		if ( "error".equals(execution.getStatus()) ) {
			
			jsonResponse = new JsonObject();
			jsonResponse.add("action", Action.ACK.getId());
			return jsonResponse;
		}
		
		
		// If the message contains executionChaining=false, we don't try to find another execution to send
		// because the worker is busy installing something
		if ( json.get("executionChaining") != null && !json.get("executionChaining").asBoolean() ) {
			
			jsonResponse.add("action", Action.ACK.getId());
			return jsonResponse;
		}
		
		// Check if there is a pending execution in the queue that this worker could handle
		InstallationDAO idao = new InstallationDAO();
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
			Worker worker = wdao.select(json.get("workerId").asInt());
			map.setWorkerIP(newExecution.getId(), worker.getDns());
			
			jsonResponse.add("action", Action.PERFORM_EXECUTION.getId());
			jsonResponse.add("execution", newExecution.toJsonObject());
		}
		
		return jsonResponse;
	}

	/**
	 * This method is triggered by the client. After verifying that the execution exists and it is being executed or waiting,
	 * it is removed from the queue and a message is sent to the worker with the instruction of terminating it
	 * @param json with this structure { execution: { id : id } }
	 * @return
	 */
	public JsonObject terminateExecution(JsonObject json) {
		
		// Validate parameters
		JsonValue executionJsonValue = json.get("execution");
		if ( executionJsonValue == null ) return new JsonObject().add("error", "Parameter execution is null");
		if ( !executionJsonValue.isObject() ) return new JsonObject().add("error", "Parameter execution isn't a json object");
		JsonObject executionJson = executionJsonValue.asObject();
		
		JsonValue executionIdValue = executionJson.get("id");
		if ( executionIdValue == null ) return new JsonObject().add("error", "Parameter id is null");
		if ( !executionIdValue.isNumber() ) return new JsonObject().add("error", "Parameter id isn't a string");
		int executionId = executionIdValue.asInt();
		
		// The execution is removed from the map
		ExecutionMap map = new ExecutionMap();
		Execution execution = map.pull(executionId);
		
		// In case the execution didn't exist, notify the client
		if ( execution == null )
			return new JsonObject().add("error", "The given execution wasn't being processed");
		
		// In case the execution is being processed right now by a worker
		if ( execution.getWorkerIP() != null ) {
			// Send termination request
			JsonObject message = new JsonObject();
			message.add("action", Action.TERMINATE_EXECUTION.getId());
			message.add("execution", new JsonObject().add("id", executionId));
			
			HttpSender sender = new HttpSender();
			sender.setDestinationIP( execution.getWorkerIP() );
			sender.setDestinationRole("worker");
			sender.setMessage(message);
			String response = null;
			try {
				response = sender.send();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return JsonObject.readFrom(response);
		}
		
		if ( "in progress".equals(execution.getStatus()) ) {
			execution.setStatus("terminated");
		}
		
		return new JsonObject().add("execution", execution.toJsonObject());
	}
}