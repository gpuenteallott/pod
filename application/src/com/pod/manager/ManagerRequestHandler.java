package com.pod.manager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;

/**
 * This class handles the requests directed to a manager from inside the cloud
 * The reason to make this class is allow manager request handling either if the manager is a separate server or the same server than the worker that sends the message
 */

public class ManagerRequestHandler {
	
	
	/**
	 * Performs the request to the manager with the given json message
	 * This method is called when the request comes from another machine, but also when the request is from the same machine
	 * @param json
	 * @return
	 */
	public JsonObject doManagerRequest (JsonObject json) {
		
		// Prepare response object
		JsonObject jsonResponse = new JsonObject();
		
		if ( json.get("action") == null ) {
			jsonResponse = new JsonObject();
			jsonResponse.add("error", "no action specified");
			return jsonResponse;
		}
		
		int actionId = json.get("action").asInt();
		Action action = Action.get(actionId);
		
		// New execution request from the client
		if ( action == Action.NEW_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.newExecution(json);
		}
		
		// Get execution status for client
		else if ( action == Action.GET_EXECUTION_STATUS ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.getExecutionStatus(json);
		}
		
		// A message from a worker informing about the result of an execution
		else if ( action == Action.REPORT_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.handleExecutionReport(json);
		}
		
		// A message from a worker informing about the status of the installation of a new activity
		else if ( action == Action.REPORT_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.handleActivityReport(json);
		}
		
		// A new activity has been submitted by the client to the cloud
		else if ( action == Action.NEW_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.newActivity(json);
		}
		
		// Activity status request from the client
		else if ( action == Action.GET_ACTIVITY_STATUS ) {
			ActivityHandler h = new ActivityHandler();
			return h.getActivityStatus(json);
		}
		
		// Request from the client to delete an activity
		else if ( action == Action.DELETE_ACTIVITY ) {
			ActivityHandler h = new ActivityHandler();
			return h.deleteActivity(json);
		}
		
		// Request from the client to delete an activity
		else if ( action == Action.TERMINATE_EXECUTION ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.terminateExecution(json);
		}
		
		// Request from client to create a new policy
		else if ( action == Action.NEW_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.newPolicy(json);
		}

		// Request from client to apply a policy
		else if ( action == Action.APPLY_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.applyPolicy(json);
		}

		// Request from client to delete a policy
		else if ( action == Action.DELETE_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.deletePolicy(json);
		}

		// Request from client to reset policies
		else if ( action == Action.RESET_POLICIES ) {
			PolicyHandler h = new PolicyHandler();
			return h.reset();
		}

		// Request from client to list all policies
		else if ( action == Action.GET_POLICIES ) {
			PolicyHandler h = new PolicyHandler();
			return h.getPolicies();
		}
		
		// Request from client to view the info of the active policy
		else if ( action == Action.VIEW_ACTIVE_POLICY ) {
			PolicyHandler h = new PolicyHandler();
			return h.viewActivePolicy();
		}
		
		// Request from client to list all workers
		else if ( action == Action.GET_WORKERS ) {
			WorkerHandler h = new WorkerHandler();
			return h.getWorkers();
		}
		
		// A message from a worker informing that it is ready after deployment
		else if ( action == Action.WORKER_DEPLOYED ) {
			WorkerHandler h = new WorkerHandler();
			return h.workerDeployed(json);
		}
		
		// A message from client requesting logs
		else if ( action == Action.GET_LOGS ) {
			return readLog(json);
		}
		
		// A message from client requesting all executions in a summary
		else if ( action == Action.GET_LOGS ) {
			ExecutionHandler h = new ExecutionHandler();
			return h.getAllExecutions();
		}
	
		return jsonResponse.add("error", "this manager doesn't recognize that request");
	}
	
	
	public JsonObject readLog (JsonObject json) {
		
		JsonObject response = new JsonObject();
		JsonArray logJson = new JsonArray();
		
		int linesToShow = 20;
		
		String filename = "";
		String type = json.get("type").asString();
		if ( "setup".equals(type) )
			filename = "/home/pod/setup.log";
		else if ( "properties".equals(type) )
			filename = "/home/pod/server.properties";
		else if ( "server".equals(type) ) {
			filename = "/var/lib/tomcat7/logs/catalina.out";
			linesToShow = 100;
		}
		
		response.add("filename", filename);
		
		 try{
			 
			 int count = countLines(filename);
			 
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(filename);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  int line = 0;
			  while ((strLine = br.readLine()) != null)   {
				  if ( line > count - linesToShow )
					  logJson.add(strLine);
				  line++;
			  }
			  //Close the input stream
			  in.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
		 
		 return response.add("contents", logJson);
	}


	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
}
