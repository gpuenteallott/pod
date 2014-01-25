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

/**
 * Servlet implementation class FrontServlet
 * This servlet receives all requests that are directed to the path /worker
 * These requests are the ones that the server should interpret as worker
 */
@WebServlet("/WorkerServlet")
public class WorkerServlet extends HttpServlet {
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
		
		doWorkerRequest(request,response);
		
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
}
