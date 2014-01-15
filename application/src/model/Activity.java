package model;

import com.eclipsesource.json.JsonObject;

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
	
	public Activity(){}
	
	public Activity (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("name") != null) name = json.get("name").asString();
		if (json.get("codeLocation") != null) codeLocation = json.get("codeLocation").asString();
		if (json.get("executeCommand") != null) executeCommand = json.get("executeCommand").asString();
		if (json.get("status") != null) status = json.get("status").asString();
	}
	
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
	public JsonObject toJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		json.add("name", name);
		json.add("codeLocation", codeLocation);
		json.add("executeCommand", executeCommand);
		json.add("status", status);
		return json;
	}
}
