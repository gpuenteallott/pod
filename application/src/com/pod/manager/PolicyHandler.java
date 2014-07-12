package com.pod.manager;

import java.util.regex.Pattern;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.pod.dao.PolicyDAO;
import com.pod.model.Policy;

public class PolicyHandler {
	
	private static Policy defaultPolicy;
	private static final String DEFAULT_POLICY_NAME = "default";
	
	/**
	 * Constructor that initializes the default policy internal object
	 */
	public PolicyHandler() {
		
		// Set up the default policy
		if ( defaultPolicy == null ) {
			defaultPolicy = new Policy(DEFAULT_POLICY_NAME);
			defaultPolicy.addRule("fixedWorkers", 1);
		}
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
		
		// We don't allow a policy with the default name
		if ( name.equals(DEFAULT_POLICY_NAME) )
			return new JsonObject().add("error", "The policy name "+name+" already exists");
		
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
			
			policy.addRule( rule.split("=")[0].trim() , rule.split("=")[1].trim());
			thereAreRules = true;
		}
		
		// Check that at least one rule was added
		if ( !thereAreRules )
			return new JsonObject().add("error", "No rules declared. They must be in the form of ruleName=ruleValue");
		
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
		
		// We don't allow a policy with the default name
		if ( name.equals(DEFAULT_POLICY_NAME) )
			return new JsonObject().add("error", "The default policy can't be deleted");
		
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
		
		defaultPolicy.setActive(defaultActive);
		jsonPolicies.add( defaultPolicy.toJsonObject() );
		
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
		
		// Set it as active
		boolean updated = pdao.setActive(name);
		
		if ( !updated )
			if ( pdao.getError() == null || pdao.getError().equals("") ) 
				return new JsonObject().add("error", "The requested policy doesn't exist");
			else
				return new JsonObject().add("error", pdao.getError());
		
		// Deactivate the previous policy
		if ( previousPolicy != null ) {
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
				
				// If the current nº of workers is less than the minimum specified, deploy more
				int minWorkers = Integer.parseInt( policy.getRules().get("minWorkers").asString() );
				if ( wh.getTotalWorkers() < minWorkers ) {
					int workersToDeploy = minWorkers-wh.getTotalWorkers();
					for ( int i = 0; i < workersToDeploy; i++ )
						wh.deployWorker();
				}
				
			}
		}).start();
		
		// Return the information of the new active policy
		return viewActivePolicy();
	}
	
	/**
	 * Retrieves the information for the active policy
	 * @return
	 */
	public JsonObject viewActivePolicy () {
		
		// Retrieve the previous active policy
		PolicyDAO pdao = new PolicyDAO();
		Policy policy = pdao.getActive();
		
		if ( policy == null ) {
			defaultPolicy.setActive(true);
			return new JsonObject().add("policy",defaultPolicy.toJsonObject());
		}
		else {
			defaultPolicy.setActive(false);
			return new JsonObject().add("policy",policy.toJsonObject());
		}
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
