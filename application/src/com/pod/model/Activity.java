package com.pod.model;

import com.eclipsesource.json.JsonObject;

/**
 * Represents an activity object
 */
public class Activity implements java.io.Serializable {
	
	private static final long serialVersionUID = 7683896163309413071L;
	private int id;
	private String name;
	private String installationScriptLocation;
	private String status;
	
	public Activity(){}
	
	public Activity (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("name") != null) name = json.get("name").asString();
		if (json.get("installationScriptLocation") != null) installationScriptLocation = json.get("installationScriptLocation").asString();
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
	public String getInstallationScriptLocation() {
		return installationScriptLocation;
	}

	public void setInstallationScriptLocation(String installationScriptLocation) {
		this.installationScriptLocation = installationScriptLocation;
	}

	/**
	 * Transforms the Activity into a JsonObject with all its attributes and values
	 * @return
	 */
	public JsonObject toJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( name != null ) json.add("name", name);
		if ( installationScriptLocation != null ) json.add("installationScriptLocation", installationScriptLocation);
		if ( status != null ) json.add("status", status);
		return json;
	}
	
	/**
	 * Transforms the Activity into a JsonObject just like toJsonObject() but only including fundamental attributes
	 * Better for a shorter representation
	 * @return 
	 */
	public JsonObject toSmallJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( name != null ) json.add("name", name);
		if ( status != null ) json.add("status", status);
		return json;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
