package worker;

public class ActivityInstaller implements Runnable {
	
	private String activityName;
	private String codeLocation;
	private String executeCommand;
	
	
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public String getCodeLocation() {
		return codeLocation;
	}
	public void setCodeLocation(String codeLocation) {
		this.codeLocation = codeLocation;
	}
	public String getExecuteCommand() {
		return executeCommand;
	}
	public void setExecuteCommand(String executeCommand) {
		this.executeCommand = executeCommand;
	}



	@Override
	public void run() {
		
		// Install activity
		
		// Send message to master when done
		
	}

}
