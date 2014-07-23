package com.pod.interaction;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.resources.PodLogger;

import com.eclipsesource.json.JsonObject;
import com.pod.listeners.ServerProperties;
import com.pod.manager.ManagerRequestHandler;

/**
 * Servlet implementation class FrontServlet
 * This servlet receives all requests that are directed to the root path of the application
 */
@WebServlet("/HttpFrontServlet")
public class HttpFrontServlet extends HttpServlet {

	public static PodLogger log = new PodLogger("HttpFrontServlet");
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Do request processing as manager receiving request from outside the cloud
		doManagerFromOutside(request,response);
	}


	/**
	 * This method checks the message that was sent from the outside to this server, which is working in MASTER mode
	 * @param request
	 * @param response
	 */
	private void doManagerFromOutside(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String action = request.getParameter("action");
		
		
		// Prepare json object with parameters
		JsonObject json = new JsonObject();
		// Prepare json response object
		JsonObject jsonResponse = new JsonObject();
		
		// In case no action parameter was provided
		if ( action == null ) {
			PrintWriter out = response.getWriter();
			out.print( "Welcome to Processing On Demand" );
			out.close();
			return;
		}
		
        // ########################################################
		//
		// TEST FOR WORKER DEPLOYMENT
		//
		if ( action.equals("w") ) {
			
			com.pod.manager.WorkerHandler wh = new com.pod.manager.WorkerHandler();
			wh.deployWorker();
			
			return;
		}
		// #######################################################

		/*
		 * We fill the json parameter object with the corresponding parameters received through HTTP
		 * Then we execute the ManagerRequestHandler with that json object
		 */

		// A new execution is requested
		else if ( action.equals("newExecution") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.NEW_EXECUTION.getId());
			JsonObject executionJson = new JsonObject();
			executionJson.add("name", request.getParameter("name"));
			if ( request.getParameter("input") != null ) executionJson.add("input", request.getParameter("input"));
			json.add("execution", executionJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name")+", input:"+request.getParameter("input"));
			// End logging
		}
		
		// The status of a previous started execution is requested
		else if ( action.equals("getExecutionStatus") ) {
			
			// Convert HTTP parameter into int
			String executionIdS = request.getParameter("executionId");
			int executionId;
			try {
				executionId = Integer.parseInt(executionIdS);
			} catch ( NumberFormatException e ){
				jsonResponse.add("error", "Parameter executionId is not an integer");
				// Send response
				response.setContentType("application/json");
				PrintWriter out = response.getWriter();
				out.print( jsonResponse.toString() );
				out.close();	
				return;
			}
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_EXECUTION_STATUS.getId());
			JsonObject executionJson = new JsonObject();
			executionJson.add("id", executionId);
			json.add("execution", executionJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", executionId:"+executionId);
			// End logging
		}
		
		// A new activity with new code to process is given
		else if ( action.equals("newActivity") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.NEW_ACTIVITY.getId());
			JsonObject activityJson = new JsonObject();
			activityJson.add("name", request.getParameter("name"));
			activityJson.add("installationScriptLocation", request.getParameter("installationScriptLocation"));
			json.add("activity", activityJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name")+", installationScriptLocation:"+request.getParameter("installationScriptLocation"));
			// End logging
		}
		
		// The status of the activity is requested with the purpose to know if it's ready for executions
		else if ( action.equals("getActivityStatus") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_ACTIVITY_STATUS.getId());
			JsonObject activityJson = new JsonObject();
			activityJson.add("name", request.getParameter("name"));
			json.add("activity", activityJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name"));
			// End logging
		}
		
		// Requesting to delete an activity from the system
		else if ( action.equals("deleteActivity") ) {
			
			// Prepare json object to pass to the request handler
			json.add("action", Action.DELETE_ACTIVITY.getId());
			JsonObject activityJson = new JsonObject();
			activityJson.add("name", request.getParameter("name"));
			json.add("activity", activityJson);
						
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name"));
			// End logging
		}
		
		// Requesting termination of an execution
		else if ( action.equals("terminateExecution") ) {
			
			// Convert HTTP parameter into int
			String executionIdS = request.getParameter("executionId");
			int executionId;
			try {
				executionId = Integer.parseInt(executionIdS);
			} catch ( NumberFormatException e ){
				jsonResponse.add("error", "Parameter executionId is not an integer");
				// Send response
				response.setContentType("application/json");
				PrintWriter out = response.getWriter();
				out.print( jsonResponse.toString() );
				out.close();	
				return;
			}
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.TERMINATE_EXECUTION.getId());
			JsonObject executionJson = new JsonObject();
			executionJson.add("id", executionId);
			json.add("execution", executionJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", executionId:"+executionId);
			// End logging
		}
		
		// Requesting a new policy
		else if ( action.equals("newPolicy") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.NEW_POLICY.getId());
			JsonObject policyJson = new JsonObject();
			policyJson.add("name", request.getParameter("name"));
			policyJson.add("rules", request.getParameter("rules"));
			json.add("policy", policyJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name")+", rules:"+request.getParameter("rules"));
			// End logging
		}

		// Requesting delete a policy
		else if ( action.equals("deletePolicy") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.DELETE_POLICY.getId());
			JsonObject policyJson = new JsonObject();
			policyJson.add("name", request.getParameter("name"));
			json.add("policy", policyJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name"));
			// End logging
		}
		

		// Requesting delete a policy
		else if ( action.equals("applyPolicy") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.APPLY_POLICY.getId());
			JsonObject policyJson = new JsonObject();
			policyJson.add("name", request.getParameter("name"));
			json.add("policy", policyJson);
			
			// Logging
			log.i("Message log. From outside. action:"+action+", name:"+request.getParameter("name"));
			// End logging
		}

		// Requesting information from the active policy
		else if ( action.equals("getActivePolicy") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_ACTIVE_POLICY.getId());
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Requesting information from the active policy
		else if ( action.equals("getPolicies") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_POLICIES.getId());
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Requesting information from the active policy
		else if ( action.equals("resetPolicies") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.RESET_POLICIES.getId());
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Request by client to obtain all workers
		else if ( action.equals("getWorkers") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_WORKERS.getId());
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Request by client to obtain the server logs
		else if ( action.equals("logs") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_LOGS.getId());
			json.add("type", request.getParameter("type"));
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Request by client to obtain a summary of all executions
		else if ( action.equals("getAllExecutions") ) {
			
			// Prepare json object to pass to the request handler
			json = new JsonObject();
			json.add("action", Action.GET_ALL_EXECUTIONS.getId());
			
			// Logging
			log.i("Message log. From outside. action:"+action);
			// End logging
		}
		
		// Handle request
		ManagerRequestHandler handler = new ManagerRequestHandler();
		jsonResponse = handler.doManagerRequest(json);

		// Send response
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print( jsonResponse.toString() );
		out.close();	

		return;
	}

}
