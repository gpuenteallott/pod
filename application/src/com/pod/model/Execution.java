package com.pod.model;

import com.eclipsesource.json.JsonObject;

public class Execution implements java.io.Serializable , Cloneable {
	
	private static final long serialVersionUID = 8850777939592004595L;
	
	private int id;
	private String stdin;
	private String stdout;
	private String stderr;
	private int activityId;
	private String activityName;
	private String status;
	private String error;
	private String workerIP;
	
	public Execution () {}
	
	public Execution (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("stdin") != null) stdin = json.get("stdin").asString();
		if (json.get("stdout") != null) stdout = json.get("stdout").asString();
		if (json.get("stderr") != null) stderr = json.get("stderr").asString();
		if (json.get("status") != null) status = json.get("status").asString();
		if (json.get("workerIP") != null) workerIP = json.get("workerIP").asString();
		if (json.get("error") != null) error = json.get("error").asString();
		if (json.get("activityId") != null) activityId = json.get("activityId").asInt();
		if (json.get("activityName") != null) activityName = json.get("activityName").asString();
	}
	
	public JsonObject toJsonObject() {
		
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( stdin != null ) json.add("stdin", stdin);
		if ( stdout != null ) json.add("stdout", stdout);
		if ( stderr != null ) json.add("stderr", stderr);
		if ( status != null ) json.add("status", status);
		if ( error != null ) json.add("error", error);
		//if ( workerIP != null ) json.add("workerIP", workerIP);    We don't send this in the json messages so the client doesn't receive this private IPs
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

	public String getStdin() {
		return stdin;
	}

	public void setStdin(String stdin) {
		this.stdin = stdin;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getWorkerIP() {
		return workerIP;
	}

	public void setWorkerIP(String workerIP) {
		this.workerIP = workerIP;
	}
}
