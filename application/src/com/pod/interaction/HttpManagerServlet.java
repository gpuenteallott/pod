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
import com.pod.manager.ManagerRequestHandler;

/**
 * Servlet implementation class FrontServlet
 * This servlet receives all requests that are directed to the path /manager
 * These requests are the ones that the server should interpret as manager
 */
@WebServlet("/ManagerServlet")
public class HttpManagerServlet extends HttpServlet {

	public static PodLogger log = new PodLogger("HttpManagerServlet");
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
	
		doManagerFromWorker(request, response);
		
	}


	/**
	 * This method checks the message that was sent from the outside to this server from a worker, which is working in MANAGER mode
	 * @param request
	 * @param response
	 */
	private void doManagerFromWorker(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
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
		
		// Logging
		log.i("Message log. For Manager. "+jsonRaw); System.out.println();
		// End logging
		
		// Create worker instance and attend request internally
		ManagerRequestHandler worker = new ManagerRequestHandler();
		JsonObject jsonResponse = worker.doManagerRequest(JsonObject.readFrom( jsonRaw ));
		
		// Logging
		log.i("Message log. Response from Manager. "+jsonResponse); System.out.println();
		// End logging
		
		// Send response
		PrintWriter out = response.getWriter();
		out.print(jsonResponse.toString());
		out.close();
		return;
		
	}
}
