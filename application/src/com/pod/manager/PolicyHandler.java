package com.pod.manager;

import java.util.regex.Pattern;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.pod.dao.PolicyDAO;
import com.pod.model.Policy;

public class PolicyHandler {
	
	private static final int DEFAULT_MAX_WAIT = 60*1000;
	
	/**
	 * Constructor that initializes the default policy internal object
	 */
	public PolicyHandler() {
	}

	/**
	 * Creates a new policy in POD using the information from the parameter
	 * The parameter must have a JsonObject called policy, which must contain the name and rules
	 * @param json
	 * @return
	 */
	public JsonObject newPolicy( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue policyJsonValue = json.get("policy");
		if ( policyJsonValue == null ) return new JsonObject().add("error", "Parameter policy is null");
		if ( !policyJsonValue.isObject() ) return new JsonObject().add("error", "Parameter policy isn't a json object");
		JsonObject policyJson = policyJsonValue.asObject();
				
		JsonValue nameValue = policyJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		JsonValue rulesValue = policyJson.get("rules");
		if ( rulesValue == null ) return new JsonObject().add("error", "Parameter rules is null");
		if ( !rulesValue.isString() ) return new JsonObject().add("error", "Parameter rules isn't a string");
		String rules = rulesValue.asString();
		
		// Create policy object
		Policy policy = new Policy (name);
		
		// Add the rules
		boolean thereAreRules = false;
		for ( String rule : rules.split(",") ) {
			// Verify that the rule contains at least three characters and an equals sign
			if ( !Pattern.matches("^[ 0-9a-zA-Z]+[=]{1}[ 0-9a-zA-Z]+$", rule) ) 
				return new JsonObject().add("error", "Invalid rule "+rule+". It must have the form ruleName=ruleValue");
			
			policy.setRule( rule.split("=")[0].trim() , rule.split("=")[1].trim());
			thereAreRules = true;
		}
		
		// Check that at least one rule was added
		if ( !thereAreRules )
			return new JsonObject().add("error", "No rules declared. They must be in the form of ruleName=ruleValue");
		
		// Set default max wait time if no max wait time was given and it is scalable
		if ( policy.getRule("fixedWorkers") == null && policy.getRule("maxWait") == null ) {
			policy.setRule( "maxWait", ""+DEFAULT_MAX_WAIT );
		}
		
		// Insert policy in the database
		PolicyDAO pdao = new PolicyDAO();
		int policyId = pdao.insert(policy);
		
		if ( policyId < 0 ) {
			if( pdao.getError().contains("Duplicate entry") ){
				return new JsonObject().add("error", "The policy name "+name+" already exists");
			}
			return new JsonObject().add("error", "Error creating the policy "+name+", retrieved id is "+policyId);
		}
		
		// No error happened
		return new JsonObject().add("policy", policy.toJsonObject());
		
	}
	
	/**
	 * A policy will be deleted. If this policy is the active one, the system will start using the default policy
	 * The parameter must have a JsonObject called policy, which must contain the name
	 * @param json
	 * @return
	 */
	public JsonObject deletePolicy ( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue policyJsonValue = json.get("policy");
		if ( policyJsonValue == null ) return new JsonObject().add("error", "Parameter policy is null");
		if ( !policyJsonValue.isObject() ) return new JsonObject().add("error", "Parameter policy isn't a json object");
		JsonObject policyJson = policyJsonValue.asObject();
		
		JsonValue nameValue = policyJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		// Delete policy from database
		PolicyDAO pdao = new PolicyDAO();
		boolean deleted = pdao.delete(name);
		
		if ( !deleted ) {
			if ( "".equals(pdao.getError()) )
				return new JsonObject().add("error", "The requested policy doesn't exist");
			else
				return new JsonObject().add("error", pdao.getError());
		}
		
		// No error happened
		return new JsonObject().add("policy", policyJson).add("status", "deleted");
	}
	
	/**
	 * List all the policies in the system and return them in a Json array
	 * @return
	 */
	public JsonObject getPolicies () {
		
		// Retrieve policies form database
		PolicyDAO pdao = new PolicyDAO();
		Policy [] policies = pdao.list();
		
		JsonArray jsonPolicies = new JsonArray();
		boolean defaultActive = true;
		for ( Policy policy : policies ) {
			if ( defaultActive && policy.isActive() ) defaultActive = false;
			jsonPolicies.add(policy.toJsonObject());
		}
		
		return new JsonObject().add("policies", jsonPolicies);
	}
	
	/**
	 * Activate the policy indicated by its name
	 * This method will first activate the given one, then deactivate the previous activity and finally retrieve a json object containing the info of the new active policy
	 * @param json
	 * @return
	 */
	public JsonObject applyPolicy ( JsonObject json ) {
		
		// Get and validate parameters
		JsonValue policyJsonValue = json.get("policy");
		if ( policyJsonValue == null ) return new JsonObject().add("error", "Parameter policy is null");
		if ( !policyJsonValue.isObject() ) return new JsonObject().add("error", "Parameter policy isn't a json object");
		JsonObject policyJson = policyJsonValue.asObject();
		
		JsonValue nameValue = policyJson.get("name");
		if ( nameValue == null ) return new JsonObject().add("error", "Parameter name is null");
		if ( !nameValue.isString() ) return new JsonObject().add("error", "Parameter name isn't a string");
		String name = nameValue.asString();
		
		// Retrieve the previous active policy
		PolicyDAO pdao = new PolicyDAO();
		Policy previousPolicy = pdao.getActive();
		
		boolean updated = pdao.setActive(name);
		
		if ( !updated )
			if ( pdao.getError() == null || pdao.getError().equals("") ) 
				return new JsonObject().add("error", "The requested policy doesn't exist");
			else
				return new JsonObject().add("error", pdao.getError());
		
		// Deactivate the previous policy
		if ( previousPolicy != null && !previousPolicy.getName().equals(name) ) {
			updated = pdao.setInactive( previousPolicy );
			if ( !updated )
				return new JsonObject().add("error", pdao.getError());
		}
		
		// Now we must launch a thread to test if the current configuration is adaptable to the policy, and if not, modify it
		new Thread( new Runnable() {
			public void run() {
				
				PolicyDAO pdao = new PolicyDAO();
				Policy policy = pdao.getActive();
				WorkerHandler wh = new WorkerHandler();
				
				int minWorkers = 1;
				int maxWorkers = 1;
				if ( policy.getRules().get("fixedWorkers") != null ) {
					minWorkers = Integer.parseInt( policy.getRules().get("fixedWorkers").asString() );
					maxWorkers = Integer.parseInt( policy.getRules().get("fixedWorkers").asString() );
				}
				else {
					if ( policy.getRules().get("minWorkers") != null ) minWorkers = Integer.parseInt( policy.getRules().get("minWorkers").asString() );
					if ( policy.getRules().get("maxWorkers") != null ) maxWorkers = Integer.parseInt( policy.getRules().get("maxWorkers").asString() );
				}
				
				
				// If the current nº of workers is less than the minimum specified, deploy more
				if ( wh.getTotalWorkers() < minWorkers ) {
					int workersToDeploy = minWorkers-wh.getTotalWorkers();
					for ( int i = 0; i < workersToDeploy; i++ )
						wh.deployWorker();
				}
				if ( wh.getTotalWorkers() > maxWorkers ) {
					int workersToTerminate = wh.getTotalWorkers()-maxWorkers;
					wh.terminateWorkers(workersToTerminate);
				}
				
			}
		}).start();
		
		// Return the information of the new active policy
		return getActivePolicy();
	}
	

	
	public JsonObject getActivePolicy() {
		
		// Retrieve the previous active policy
		PolicyDAO pdao = new PolicyDAO();
		
		return pdao.getActive().toJsonObject();
	}

	/**
	 * Removes the active flag from all policies, so the default configuration will be used
	 * @return
	 */
	public JsonObject reset() {
		
		// Call the reset method from the DAO
		PolicyDAO pdao = new PolicyDAO();
		boolean updated = pdao.reset();
		
		if ( updated )
			return new JsonObject().add("status","Policy deactivated. Running in default configuration");
		else
			return new JsonObject().add("error","No modification, couldn't find an active policy. Running in default configuration");
	}
}
