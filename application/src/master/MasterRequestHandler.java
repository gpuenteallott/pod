package master;

import model.Activity;

import com.eclipsesource.json.JsonObject;

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
			
			// If the status is installed, then add am installation record with this info
			
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("ok", "ok");
			return jsonResponse;
			
		}
	
		return  new JsonObject().add("error", "this master doesn't recognize that request");
	}
}
