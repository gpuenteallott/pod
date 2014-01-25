package manager;

import interaction.Action;
import interaction.Sender;

import java.io.IOException;

import model.Activity;
import model.Execution;
import model.Worker;

import com.eclipsesource.json.JsonObject;

import dao.ActivityDAO;
import dao.WorkerDAO;

public class ExecutionHandler {

	/**
	 * Handles the event of a new execution
	 * This method will check that the given activity is valid. That means, it is in the system and its source code has been verified
	 * After the check, this method will look for an available worker to send the execution request right away
	 * If no worker is available at the moment, the execution request will be put in the queue
	 * @param activityName
	 * @param input
	 * @return JsonObject with the response for the client that requested the new execution
	 */
	public JsonObject newExecution(String name, String input) {
		
		// Prepare response
		JsonObject json = new JsonObject();

		// Validate parameters. Parameter input can be null
		if( name == null ) return json.add("error", "Parameter name is null");
		if ( name.equals("") ) return json.add("error", "Parameter name is empty");
		
		// Verify that the given activity exists in the system
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		if ( activity == null ) {
			if ( adao.getError() == null || adao.getError().equals("") )
				return json.add("error", "The requested activity doesn't exist");
			else
				return json.add("error", adao.getError());
		}
		
		// Verify that its status is approved
		if ( !"approved".equals(activity.getStatus()) )
			return json.add("error", "The requested activity isn't approved for executions. Its current status is "+activity.getStatus());
		
		// Get a new execution ID to identify this execution
		int executionId = IdGenerator.newId();
		
		Execution execution = new Execution();
		execution.setId(executionId);
		execution.setInput(input);
		execution.setActivityName(name);
		execution.setActivityId(activity.getId());
		
		// Put the execution in the execution bag, so the client can retrieve its status
		ExecutionMap bag = new ExecutionMap();
		bag.put(execution, "in progress");
		
		// Verify that there are completed installations for this activity
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
			message.add("action", Action.NEW_EXECUTION.getId() );
			message.add("execution", execution.toJsonObject());
			
			// Set the public DNS of the worker. If empty, it will mean this same machine
			Sender sender = new Sender();
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
				json.add("execution", execution.toJsonObject()).add("status", "in progress");
			}
			else {
				// If there was an error sending this execution, problematic
				// We put the worker with error status, so no more executions are sent to this worker
				worker.setStatus("error");
				wdao.update(worker);

				/*
				 * ERROR HANDLING
				 * SOMETHING MUST BE DONE HERE
				 * Put the execution in the queue for example (easy), or
				 * Look for a new worker to send this message (hard)
				 */
				// For now, we send an error message
				json.add("error", "Couldn't contact with a worker to start this execution with id "+executionId);
			}
		}
		
		// In case there is no worker available, the execution request must go to the waiting queue
		else {
			ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
			queue.put(execution);
			json.add("execution", execution.toJsonObject()).add("status", "in progress");
		}
		
		return json;
	}

	
	/**
	 * Fetches the execution status and information
	 * If the execution is done, the json response will contain the execution in json format
	 * If there was an error, the json response will contain the error description
	 * In any other case, the json response will only inform about the current status
	 * @param executionIdS
	 * @return
	 */
	public JsonObject getExecutionStatus (String executionIdS) {
		
		// Prepare response
		JsonObject json = new JsonObject();
		
		// Validate parameters
		if ( executionIdS == null ) return json.add("error", "Parameter executionId is null");
		if ( executionIdS.equals("") ) return json.add("error", "Parameter executionId is empty");
		
		int executionId;
		try {
			executionId = Integer.parseInt(executionIdS);
		} catch ( NumberFormatException e ){
			return json.add("error", "Parameter executionId is not an integer");
		}
		
		// Retrieve execution status
		ExecutionMap bag = new ExecutionMap();
		String status = bag.getStatus(executionId);
		
		// In case the execution isn't in the bag
		if ( status == null ) return json.add("error", "Execution with id "+executionId+" doesn't exist");
		
		// In case the execution is finished
		if ( status.equals("finished") ) return json.add("execution", bag.pullExecution(executionId).toJsonObject())
													.add("status", "finished");
		
		// In case there was an error
		else if ( status.equals("error") ) return json.add("status", "error")
												     .add("errorDescription", bag.pullError(executionId));
		
		// In other situation (not finished)
		else return json.add("status", status);
		
	}
}