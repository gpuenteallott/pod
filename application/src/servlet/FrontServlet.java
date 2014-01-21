package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import worker.WorkerRequestHandler;

import com.eclipsesource.json.JsonObject;

import master.ActivityHandler;

/**
 * Servlet implementation class CatchAllServlet
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
		
		
		if ( servlet.ServerProperties.getRole().equals("master") ) {
			
			String workerId = request.getParameter("workerId");
			
			if ( workerId == null )
				doMasterFromOutside(request,response);
			else
				doMasterFromWorker(request, response);
		}
		
		else if ( servlet.ServerProperties.getRole().equals("worker") ) {
			doWorkerRequest(request,response);
		}
	}

	/**
	 * This method checks the message that was sent from the outside to this server from a worker, which is working in MASTER mode
	 * @param request
	 * @param response
	 */
	private void doMasterFromWorker(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method checks what message was sent to this server, which is working in WORKER mode
	 * @param request
	 * @param response
	 */
	private void doWorkerRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String jsonRaw = request.getParameter("json");
		
		// In case no action parameter was provided
		if ( jsonRaw == null ) {
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("error", "no message");
			PrintWriter out = response.getWriter();
			out.print(jsonResponse.toString());
			out.close();
			return;
		}
		
		// Create worker instance and attend request internally
		WorkerRequestHandler worker = new WorkerRequestHandler();
		JsonObject jsonResponse = worker.doWorkerRequest(JsonObject.readFrom( jsonRaw ));
		
		// Send response
		PrintWriter out = response.getWriter();
		out.print(jsonResponse.toString());
		out.close();
		return;
		
	}

	/**
	 * This method checks the message that was sent from the outside to this server, which is working in MASTER mode
	 * @param request
	 * @param response
	 */
	private void doMasterFromOutside(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
			
			String activityName = request.getParameter("activityName");
			String input = request.getParameter("input");
			
			return;
		}
		
		// The status of a previous started execution is requested
		else if ( action.equals("getExecutionStatus") ) {
			
			String executionId = request.getParameter("executionId");
			
			return;
		}
		
		// A new activity with new code to process is given
		else if ( action.equals("newActivity") ) {
			
			System.out.println("servlet started");
			
			String name = request.getParameter("name");
			String installationScriptLocation = request.getParameter("installationScriptLocation");
			
			// Create activity and set up response object
			ActivityHandler ah = new ActivityHandler();
			JsonObject jsonResponse = ah.newActivity(name, installationScriptLocation);
			
			// Send response
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.print( jsonResponse.toString() );
			out.close();	

			System.out.println("servlet done");
			return;
		}
		
		// The status of the activity is requested with the purpose to know if it's ready for executions
		else if ( action.equals("getActivityStatus") ) {
			
			String activityName = request.getParameter("name");
			
			ActivityHandler ah = new ActivityHandler();
			
			// Set up response object
			JsonObject jsonResponse = ah.retrieveActivityStatus(activityName);
			
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
