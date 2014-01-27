package com.pod.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eclipsesource.json.JsonObject;
import com.pod.manager.ActivityHandler;
import com.pod.manager.ExecutionHandler;

/**
 * Servlet implementation class FrontServlet
 * This servlet receives all requests that are directed to the root path of the application
 */
@WebServlet("/CatchAllServlet")
public class FrontServlet extends HttpServlet {
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
		
		// Verify that we have the info of the public DNS
		if ( ServerProperties.getDns() == null ) {
			ServerProperties.setDns( request.getServerName() );
		}
		
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
		
		// In case no action parameter was provided
		if ( action == null ) {
			PrintWriter out = response.getWriter();
			out.print( "Welcome to Processing On Demand" );
			out.close();
			return;
		}
		
		// A new execution is requested
		else if ( action.equals("newExecution") ) {
			
			String name = request.getParameter("name");
			String input = request.getParameter("input");
			
			// Logging
				System.out.println("Message log. From outside. action:"+action+", name:"+name+", input:"+input); System.out.println();
			// End logging
			
			ExecutionHandler eh = new ExecutionHandler();
			JsonObject jsonResponse = eh.newExecution (name, input);
			
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();	

			return;
		}
		
		// The status of a previous started execution is requested
		else if ( action.equals("getExecutionStatus") ) {
			
			String executionId = request.getParameter("executionId");
			
			// Logging
				System.out.println("Message log. From outside. action:"+action+", executionId:"+executionId); System.out.println();
			// End logging
				
			ExecutionHandler eh = new ExecutionHandler();
			JsonObject jsonResponse = eh.getExecutionStatus (executionId);
				
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();
		
			return;
		}
		
		// A new activity with new code to process is given
		else if ( action.equals("newActivity") ) {
			
			String name = request.getParameter("name");
			String installationScriptLocation = request.getParameter("installationScriptLocation");
			
			// Logging
				System.out.println("Message log. From outside. action:"+action+", name:"+name+", installationScriptLocation:"+installationScriptLocation); System.out.println();
			// End logging
			
			// Create activity and set up response object
			ActivityHandler ah = new ActivityHandler();
			JsonObject jsonResponse = ah.newActivity(name, installationScriptLocation);
			
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();	

			return;
		}
		
		// The status of the activity is requested with the purpose to know if it's ready for executions
		else if ( action.equals("getActivityStatus") ) {
			
			String name = request.getParameter("name");
			
			// Logging
				System.out.println("Message log. From outside. action:"+action+", name:"+name); System.out.println();
			// End logging
			
			ActivityHandler ah = new ActivityHandler();
			
			// Set up response object
			JsonObject jsonResponse = ah.retrieveActivityStatus(name);
			
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();
			return;
		}
		
		// Requesting to delete an activity from the system
		else if ( action.equals("deleteActivity") ) {
			
			String name = request.getParameter("name");
			
			// Logging
				System.out.println("Message log. From outside. action:"+action+", name:"+name); System.out.println();
			// End logging
			
			ActivityHandler ah = new ActivityHandler();
			JsonObject jsonResponse = ah.deleteActivity(name);
		
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();	
			return;
		}

	}

}
