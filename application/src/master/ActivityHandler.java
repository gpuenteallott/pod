package master;

import model.Activity;
import dao.ActivityDAO;

/**
 * This class provides the necessary functions to make operations on activities, such us add or delete
 */
public class ActivityHandler {

	private String result;
	
	/**
	 * Creates a new activity and puts it in the information database
	 * If there is an error, the result accessible by 
	 * @param activityName unique name of the activity
	 * @param codeLocation URL with the file containing the code, that will be unzipped
	 * @param executeCommand command to execute the program
	 * @return the id of the new activity, or -1 in case of error getResult();
	 */
	public int newActivity( String activityName, String codeLocation, String executeCommand ) {
		
		// Validate parameters
		if( activityName == null ) { result = "Parameter activityName is null"; return -1; }
		if( codeLocation == null ) { result = "Parameter codeLocation is null"; return -1; }
		if( executeCommand == null ) { result = "Parameter executeCommand is null"; return -1; }
		if ( activityName.equals("") ) { result = "Parameter activityName is empty"; return -1; }
		if ( codeLocation.equals("") ) { result = "Parameter codeLocation is empty"; return -1; }
		if ( executeCommand.equals("") ) { result = "Parameter executeCommand is empty"; return -1; }
		
		// Create activity object
		Activity activity = new Activity();
		activity.setName(activityName);
		activity.setCodeLocation(codeLocation);
		activity.setExecuteCommand(executeCommand);
		activity.setStatus("installing");
		
		// Insert object in persistent information storage (database)
		ActivityDAO adao = new ActivityDAO();
		int activityId = adao.insert(activity);
		
		System.out.println("activity inserted with id "+activityId);
		
		// Some error happened
		if ( activityId < 0 ) {
			
			if( adao.getError().contains("Duplicate entry") ){
				result = "The activity name "+activityName+" already exists";
			}
			return activityId;
		}
		
		// Launch a thread that for every worker, sends a request to perform the installation
		new Thread ( new ActivityInstallationNotifier(activity) ).start();
		
		// No error happened
		return activityId;
	}
	
	/**
	 * Deletes the activity. If the activity can't be deleted, the result message should be checked using getResult()
	 * @param activityName
	 * @return true if success, false otherwise
	 */
	public boolean deleteActivity( String activityName ) {
		
		// Validate parameters
		if( activityName == null ) { result = "Parameter activityName is null"; return false; }
		if ( activityName.equals("") ) { result = "Parameter activityName is empty"; return false; }
		
		// Delete the activity from the database
		ActivityDAO adao = new ActivityDAO();
		boolean deleted = adao.delete(activityName);
		
		// Some error might have happened
		if ( !deleted && !adao.getError().equals("")) {
			return false;
		}
		// In case it wasn't deleted but there was no error, it's because it didn't exist
		else if ( !deleted && adao.getError().equals("")) {
			result = "The activity "+activityName+" didn't exist previously.";
			return true;
		}
		
		// No error happened
		result = "Activity "+activityName+" deleted, in uninstalling process";
		return true;
	}
	
	/**
	 * Retrieves the status and saves it in the object. It can be later accessed using getResult()
	 * @param activityName
	 * @return true if success, false otherwise
	 */
	public boolean retrieveActivityStatus( String activityName ) {
		
		// Validate parameters
		if( activityName == null ) { result = "Parameter activityName is null"; return false; }
		if ( activityName.equals("") ) { result = "Parameter activityName is empty"; return false; }
		
		// Delete the activity from the database
		ActivityDAO adao = new ActivityDAO();
		Activity activity = adao.select(activityName);
		
		// Some error might have happened
		if ( activity == null ) {
			result = adao.getError();
			return false;
		}
		
		// No error happened
		result = activity.getStatus();
		return true;
	}
	
	/**
	 * This method allows access to result description. If no result happened, it returns null
	 * @return
	 */
	public String getResult(){
		return this.result;
	}
}
