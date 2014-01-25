package model;

import com.eclipsesource.json.JsonObject;

public class Execution implements java.io.Serializable , Cloneable {
	
	private static final long serialVersionUID = 8850777939592004595L;
	
	int id;
	String input;
	String output;
	String error;
	int activityId;
	String activityName;
	
	public Execution () {}
	
	public Execution (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("input") != null) input = json.get("input").asString();
		if (json.get("output") != null) output = json.get("output").asString();
		if (json.get("error") != null) error = json.get("error").asString();
		if (json.get("activityId") != null) activityId = json.get("activityId").asInt();
		if (json.get("activityName") != null) activityName = json.get("activityName").asString();
	}
	
	public JsonObject toJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( input != null ) json.add("input", input);
		if ( output != null ) json.add("output", output);
		if ( error != null ) json.add("error", error);
		json.add("activityId", activityId);
		if ( activityName != null ) json.add("activityName", activityName);
		return json;
	}
	
	 @Override
     public Execution clone(){
         try {
                 return (Execution) super.clone();
         } catch (CloneNotSupportedException e) {
                 e.printStackTrace();
                 return null;
         }
     }
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
}
