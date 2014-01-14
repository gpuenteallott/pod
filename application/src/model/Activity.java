package model;

/**
 * Represents an activity object
 */
public class Activity implements java.io.Serializable {
	
	private static final long serialVersionUID = 7683896163309413071L;
	private int id;
	private String name;
	private String codeLocation;
	private String executeCommand;
	private String status;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
