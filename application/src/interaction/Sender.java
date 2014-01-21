package interaction;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import master.MasterRequestHandler;
import servlet.ServerProperties;
import worker.WorkerRequestHandler;

import com.eclipsesource.json.JsonObject;

/**
 * This class abstracts the communication details between servers inside the cloud
 * It also abstracts the complexity of communicating with the same machine (but another role) or another one
 */
public class Sender {
	
	private String destination;
	private String origin;
	private JsonObject message;
	
	public Sender() {
		this.origin = ServerProperties.getDns();
	}
	
	/**
	 * Send the message to the specified destination, which could be remote or local
	 * @return String response
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String send() throws MalformedURLException, IOException {
		
		// Set origin
		this.message.add("origin", origin);
		
		if ( this.message.get("for") != null && this.message.get("for").asString().equals("master") )
			this.message.add("workerId", ServerProperties.getWorkerId());
		
		// If there is no destination, send to this same machine
		if ( destination.equals("") )
			return sendToMyself();
		
		// Or send to a remote worker
		else
			return sendToRemote();
		
	}
	
	/**
	 * Send message internally, which means, execute the result of receiving the given message
	 * @return String response
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String sendToMyself() {
		
		// Who is this message for?
		if ( message.get("for") != null && message.get("for").asString().equals("master") ) 
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
	 * Execute directly the code that receives the messages at the master
	 * @return String response from the master
	 */
	private String sendToMyMasterSelf() {
		
		MasterRequestHandler master = new MasterRequestHandler();
		return master.doMasterRequest(message).toString();
		
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
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public JsonObject getMessage() {
		return message;
	}
	public void setMessage(JsonObject message) {
		this.message = message;
	}
}
