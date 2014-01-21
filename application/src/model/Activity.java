package model;

import com.eclipsesource.json.JsonObject;

/**
 * Represents an activity object
 */
public class Activity implements java.io.Serializable {
	
	private static final long serialVersionUID = 7683896163309413071L;
	private int id;
	private String name;
	private String installationScriptLocation;
	
	public Activity(){}
	
	public Activity (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("name") != null) name = json.get("name").asString();
		if (json.get("installationScriptLocation") != null) installationScriptLocation = json.get("installationScriptLocation").asString();
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

	public JsonObject toJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		json.add("name", name);
		json.add("installationScriptLocation", installationScriptLocation);
		return json;
	}
}
