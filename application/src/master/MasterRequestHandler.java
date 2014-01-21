package master;

import model.Activity;

import com.eclipsesource.json.JsonObject;

import dao.InstallationDAO;

/**
 * This class handles the requests directed to a master from inside the cloud
 * The reason to make this class is allow master request handling either if the master is a separate server or the same server than the worker that sends the message
 */

public class MasterRequestHandler {
	
	
	/**
	 * Performs the request to the master with the given json message
	 * This method is called when the request comes from another machine, but also when the request is from the same machine
	 * @param json
	 * @return
	 */
	public JsonObject doMasterRequest (JsonObject json) {
		
		if ( json.get("action") == null ) {
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("error", "no action specified");
			return jsonResponse;
		}
		
		String action = json.get("action").asString();
		
		// A message from a worker informing about the status of the installation of a new activity
		if ( action.equals("installActivityReport") ) {
			
			// Get the activity that it's referring to and the status of the installation
			Activity activity = new Activity(json.get("activity").asObject());
			String status = json.get("status").asString();
			int workerId = json.get("workerId").asInt();
			
			InstallationDAO idao = new InstallationDAO();
			
			// In case that there was an error, we add the error description information
			if ( !status.equals("error") )
				idao.update(activity.getId(), workerId, status);
			else
				idao.update(activity.getId(), workerId, status, json.get("errorDescription").asString());
			
			// Send response
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("action", "ack");
			return jsonResponse;
			
		}
	
		return  new JsonObject().add("error", "this master doesn't recognize that request");
	}
}
