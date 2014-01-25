package manager;

import interaction.Action;
import model.Activity;
import model.Execution;

import com.eclipsesource.json.JsonObject;

import dao.ActivityDAO;
import dao.InstallationDAO;
import dao.WorkerDAO;

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
		
		if ( json.get("action") == null ) {
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("error", "no action specified");
			return jsonResponse;
		}
		
		int actionId = json.get("action").asInt();
		Action action = Action.get(actionId);
		
		// A message from a worker informing about the result of an execution
		if ( action == Action.EXECUTION_REPORT ) {
			
			String status = json.get("status").asString();
			Execution execution = new Execution (json.get("execution").asObject());
			
			// In case there was an error in the worker with the execution
			if ( "error".equals(status) ) {
				
				ExecutionMap bag = new ExecutionMap();
				bag.putError(execution.getId(), json.get("errorDescription").asString());
				
				JsonObject jsonResponse = new JsonObject();
				jsonResponse.add("action", Action.ACK.getId());
				return jsonResponse;
			}
			
			// Put the execution object in the bag, so the client can retrieve it later
			ExecutionMap bag = new ExecutionMap();
			bag.put(execution, status);
			
			// Check if there is a pending execution in the queue that this worker could handle
			InstallationDAO idao = new InstallationDAO();
			int[] activityIds = idao.selectInstalledActivityIdsByWorker( json.get("workerId").asInt() );
			
			ExecutionWaitingQueue queue = new ExecutionWaitingQueue();
			Execution newExecution = queue.pull(activityIds);
			System.out.println(newExecution != null);
			
			// Prepare response object
			JsonObject jsonResponse = new JsonObject();
			
			// If no pending executions in the queue are found
			// we set the worker status to "ready" because it's available
			if ( newExecution == null ) {
				WorkerDAO wdao = new WorkerDAO();
				wdao.updateStatus( json.get("workerId").asInt() , "ready");

				jsonResponse.add("action", Action.ACK.getId());
			}
			
			// If there is a pending execution we don't change the status of the worker (keep it "working")
			else {
				jsonResponse.add("action", Action.NEW_EXECUTION.getId());
				jsonResponse.add("execution", newExecution.toJsonObject());
			}
			
			
			return jsonResponse;
		}
		
		// A message from a worker informing about the status of the installation of a new activity
		else if ( action == Action.INSTALL_ACTIVITY_REPORT ) {
			
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
				if ( activity.getStatus().equals("verifying") ) {
					ActivityDAO adao = new ActivityDAO();
					adao.updateStatus( activity.getId() , "rejected");
				}
			}
			
			// Send response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("action", Action.ACK.getId());
			return jsonResponse;
			
		}
	
		return  new JsonObject().add("error", "this manager doesn't recognize that request");
	}
}
