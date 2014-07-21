package com.pod.model;

import com.eclipsesource.json.JsonObject;

public class Policy implements java.io.Serializable {

	private static final long serialVersionUID = -3600353772333742543L;
	
	private int id;
	private String name;
	private boolean active;
	private JsonObject rules;
	
	public Policy( String name ) {
		this();
		this.name = name;
	}
	
	public Policy() {
		this.active = false;
		this.rules = new JsonObject();
	}
	
	public Policy (JsonObject json) {
		if (json.get("id") != null) id = json.get("id").asInt();
		if (json.get("name") != null) name = json.get("name").asString();
		if (json.get("active") != null) active = json.get("active").asBoolean();
		if (json.get("rules") != null) rules = json.get("rules").asObject();
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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * Sets the active flag to true if the parameter is equals to 1, or false otherwise
	 * @param active
	 */
	public void setActive(int active) {
		this.active = active == 1;
	}
	public JsonObject getRules() {
		return rules;
	}
	public void setRules(JsonObject rules) {
		this.rules = rules;
	}
	public void setRules(String rules) {
		this.rules = JsonObject.readFrom(rules);
	}
	/**
	 * Adds the specified rule to the policy. If the rule existed previously, its value is updated,
	 * @param ruleName
	 * @param ruleValue
	 */
	public void setRule ( String ruleName, String ruleValue ) {
		rules.set(ruleName, ruleValue);
	}
	/**
	 * Adds the specified rule to the policy. If the rule existed previously, its value is updated,
	 * @param ruleName
	 * @param ruleValue
	 */
	public void setRule ( String ruleName, int ruleValue ) {
		rules.set(ruleName, ruleValue);
	}
	/**
	 * Retrieves the rule indicated by its name, or null if it's not found
	 * @param ruleName
	 * @return
	 */
	public String getRule ( String ruleName ) {
		return rules.get(ruleName) != null ? rules.get(ruleName).asString() : null;
	}
	
	/**
	 * Shortcut function
	 * @return
	 */
	public int getMaxWorkers () {
		if ( rules.get("fixedWorkers") != null )
			return rules.get("fixedWorkers").asInt();
		else
			return Integer.parseInt( rules.get("maxWorkers").asString() );
	}
	
	/**
	 * Deletes and retrieves the rule indicated by its name, or null if it's not found
	 * @param ruleName
	 * @return
	 */
	public String removeRule ( String ruleName ) {
		return rules.get(ruleName) != null ? rules.remove(ruleName).asString() : null;
	}
	
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		json.add("id", id);
		if ( name != null ) json.add("name", name);
		json.add("active", active);
		json.add("rules", rules);
		return json;
	}
}
