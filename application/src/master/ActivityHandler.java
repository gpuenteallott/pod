package master;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import model.Activity;
import model.Installation;
import dao.ActivityDAO;
import dao.InstallationDAO;

/**
 * This class provides the necessary functions to make operations on activities, such us add or delete
 */
public class ActivityHandler {
	
	/**
	 * Creates a new activity and puts it in the information database
	 * If there is an error, the result accessible by 
	 * @param activityName unique name of the activity
	 * @param installationScriptLocation URL to the installation script
	 * @param executionScriptLocation URL to the script to execute when there are execution requests
	 * @return a JsonObject representing the just created activity, including its unique id
	 */
	public JsonObject newActivity( String name, String installationScriptLocation ) {
		
		// Validate parameters
		if( name == null ) { return new JsonObject().add("error", "Parameter name is null"); }
		if( installationScriptLocation == null ) { return new JsonObject().add("error", "Parameter installationScriptLocation is null"); }
		if ( name.equals("") ) { return new JsonObject().add("error", "Parameter name is empty"); }
		if ( installationScriptLocation.equals("") ) { return new JsonObject().add("error", "Parameter installationScriptLocation is empty"); }
		
		// Create activity object
		Activity activity = new Activity();
		activity.setName(name);
		activity.setInstallationScriptLocation(installationScriptLocation);
		
		// Insert object in persistent information storage (database)
		ActivityDAO adao = new ActivityDAO();
		int activityId = adao.insert(activity);
		
		// Prepare response object
		JsonObject json = new JsonObject();
		
		// Some error happened
		if ( activityId < 0 ) {
			
			if( adao.getError().contains("Duplicate entry") ){
				return json.add("error", "The activity name "+name+" already exists");
			}
			return json.add("error", "Error creating the activity "+name+", retrieved id is "+activityId);
		}
		
		// Launch a thread that for every worker, sends a request to perform the installation
		new Thread ( new ActivityInstallationNotifier(activity, "installActivity") ).start();
		
		// No error happened
		return json.add("activity", activity.toJsonObject()).add("status", "installing");
	}
	
	/**
	 * Deletes the activity. If the activity can't be deleted, the result message should be checked using getResult()
	 * @param activityName
	 * @return a JsonObject representing the activity and a status parameter that indicates if the activity is being uninstalled
	 */
	public JsonObject deleteActivity( String name ) {
		
		// Validate parameters
		if( name == null ) { return new JsonObject().add("error", "Parameter name is null"); }
		if ( name.equals("") ) { return new JsonObject().add("error", "Parameter name is empty"); }
		
		// Delete the activity from the database
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		// Some error might have happened
		if ( activity == null ) {
			return new JsonObject().add("error", adao.getError());
		}
		
		// Launch a thread that for every worker, sends a request to perform the installation
		new Thread ( new ActivityInstallationNotifier(activity, "uninstallActivity") ).start();
		
		// No error happened
		return new JsonObject().add("activity", activity.toJsonObject()).add("status", "uninstalling");
	}
	
	/**
	 * Retrieves the status and saves it in the object. It can be later accessed using getResult()
	 * @param activityName
	 * @return JsonObject representing the status of the activity and its information
	 */
	public JsonObject retrieveActivityStatus( String name ) {
		
		// Prepare response object
		JsonObject json = new JsonObject();
		
		// Validate parameters
		if( name == null ) { return json.add("error", "Parameter name is null"); }
		if ( name.equals("") ) { return json.add("error", "Parameter name is empty"); }
		
		// Select the activity from the database
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(name);
		
		// Some error might have happened
		if ( activity == null ) {
			return json.add("error", "The requested activity doesn't exist");
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
	    json.add("activity", jsonActivity);
		
		return json;
	}
	
}
