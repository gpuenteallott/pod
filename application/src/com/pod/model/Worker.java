package com.pod.model;

import java.util.Date;

import com.eclipsesource.json.JsonObject;

/**
 * Represents a worker object
 */
public class Worker implements java.io.Serializable {
	
	private static final long serialVersionUID = -5174950454384771612L;
	private int id;
	private String status;
	private String localIp;
	private String publicIp;
	private String instanceId;
	private boolean isManager;
	private Date lastTimeWorked;
	private Date lastTimeAlive;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLocalIp() {
		return localIp;
	}
	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}
	
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( status != null ) json.add("status", status);
		if ( localIp != null ) json.add("localIp", localIp);
		if ( publicIp != null ) json.add("publicIp", publicIp);
		if ( instanceId != null ) json.add("instanceId", instanceId);
		if ( lastTimeWorked != null ) json.add("lastTimeWorked", lastTimeWorked.getTime());
		if ( lastTimeAlive != null ) json.add("lastTimeAlive", lastTimeAlive.getTime());
		json.add("isManager", isManager);
		return json;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getPublicIp() {
		return publicIp;
	}
	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}
	public boolean isManager() {
		return isManager;
	}
	public void setManager(boolean isManager) {
		this.isManager = isManager;
	}
	public Date getLastTimeWorked() {
		return lastTimeWorked;
	}
	public void setLastTimeWorked(Date lastTimeWorked) {
		this.lastTimeWorked = lastTimeWorked;
	}
	public Date getLastTimeAlive() {
		return lastTimeAlive;
	}
	public void setLastTimeAlive(Date lastTimeAlive) {
		this.lastTimeAlive = lastTimeAlive;
	}
}
