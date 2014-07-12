package com.pod.interaction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.eclipsesource.json.JsonObject;
import com.pod.listeners.ServerProperties;
import com.pod.manager.ManagerRequestHandler;
import com.pod.worker.WorkerRequestHandler;

/**
 * This class abstracts the communication details between servers inside the cloud
 * It also abstracts the complexity of communicating with the same machine (but another role) or another one
 */
public class HttpSender {

	Logger logger = Logger.getLogger(HttpSender.class.getName());
	
	private String destinationIP;
	private String destinationRole; // can be "worker" or "manager"
	private JsonObject message;
	
	public HttpSender() {
	}
	
	/**
	 * Send the message to the specified destination, which could be remote or local
	 * @return String response
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String send() throws MalformedURLException, IOException {
		
		if ( "manager".equals(destinationRole) )
			this.message.add("workerId", ServerProperties.getWorkerId());
		
		// Logging
			int i = (int)(Math.random() * 1000);
			logger.info("Message log. Id "+i+". To "+destinationRole+" ("+destinationIP+"/"+destinationRole+")");
			logger.info("Req ("+i+"): "+message);
		// End logging
		
		String response = null;
		
		// If there is no destination, send to this same machine
		if ( destinationIP.equals("") )
			response = sendToMyself();
		
		// Or send to a remote worker
		else
			response = sendToRemote();
		
		// Logging
			logger.info("Res ("+i+"): "+response);
			logger.info("");
		// End logging
			
		return response;
	}
	
	/**
	 * Send message internally, which means, execute the result of receiving the given message
	 * @return String response
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String sendToMyself() {
		
		// Who is this message for?
		if ( "manager".equals(destinationRole) ) 
			return sendToMyMasterSelf();
		
		else
			return sendToMyWorkerSelf();
		
	}

	/**
	 * Execute directly the code that receives the messages at a worker
	 * @return String response from the worker
	 */
	private String sendToMyWorkerSelf() {
		
		WorkerRequestHandler worker = new WorkerRequestHandler();
		return worker.doWorkerRequest(message).toString();
	}

	/**
	 * Execute directly the code that receives the messages at the manager
	 * @return String response from the manager
	 */
	private String sendToMyMasterSelf() {
		
		ManagerRequestHandler master = new ManagerRequestHandler();
		return master.doManagerRequest(message).toString();
		
	}

	/**
	 * Send HTTP POST request with the message to the specified destination
	 * @return String response
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String sendToRemote () throws MalformedURLException, IOException {
		
		String urlParameters = "";
		if ( message != null )
		    urlParameters = "json="+message.toString();
		
		// Prepare destination. eg: http://123-45-67-89/worker
		String destination = destinationIP;
		if ( destinationRole != null ) destination += destinationRole;
		
		// Open connection
		URL obj = new URL(destination);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		// Set HTTP method
		con.setRequestMethod("POST");
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		// Read response
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
	}
	
	public String getDestinationIP() {
		return destinationIP;
	}
	public void setDestinationIP(String destinationIP) {
		this.destinationIP = destinationIP;
	}
	public JsonObject getMessage() {
		return message;
	}
	public void setMessage(JsonObject message) {
		this.message = message;
	}

	public String getDestinationRole() {
		return destinationRole;
	}

	public void setDestinationRole(String destinationRole) {
		this.destinationRole = destinationRole;
	}
}
