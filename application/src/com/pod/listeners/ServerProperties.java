package com.pod.listeners;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.eclipsesource.json.JsonObject;
import com.pod.dao.ActivityDAO;
import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.model.Activity;
import com.pod.model.Worker;
import com.pod.worker.ActivityInstallationQueue;
import com.pod.worker.ActivityInstaller;


/**
 * This class has the server attributes that are required by the app in general
 * Also, it implements a listener so it can load these attributes and set everything up when the context is initialized
 * 
 * In case the app is located in /ROOT
 * To make the listener work correctly, this line had to be commented in the server.xml file:
 * <Context docBase="ROOT" path="/ROOT" reloadable="true" source="org.eclipse.jst.jee.server:ROOT"/>
 * The reason is that otherwise the context was initialized twice.
 */
public class ServerProperties implements ServletContextListener {
	
	Logger logger = Logger.getLogger(ServerProperties.class.getName());
	
	// This is the default location for the server properties, except in the case of the manager
	private static final String PROPERTIES_FILE_PATH = "/home/user/server.properties";
	
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private static String role;
	private static String name;
	private static String dns;
	private static String securityGroup;
	private static String keypair;
	private static int workerId;
	private static String managerDns;
	private static String repoURL;
	private static String ami;
	private static String instanceType;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("Context destroyed");
	}

	/**
	 * Start the context and load this server specific properties
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		logger.info("Initializing Context");

		// Reset the database
		new WorkerDAO().deleteAll();
		new ActivityDAO().deleteAll();
		new PolicyDAO().deleteAll();
		// Reset the app directory
		File appDirectory = new File ("/home/user/app");
		deleteContents(appDirectory);
		
		// Load properties
		try {
			// Load the file in the usual path of the file in the project (case of the manager)
			File serverProperties = new File(PROPERTIES_FILE_PATH);
			InputStream is = serverProperties.exists() ? new FileInputStream(serverProperties) : getClass().getResourceAsStream("/main/resources/server.properties");
			
			Properties properties = new Properties();
			properties.load(is);
			
			role = properties.getProperty("role");
			name = properties.getProperty("name");
			securityGroup = properties.getProperty("securityGroup");
			keypair = properties.getProperty("keypair");
			repoURL = properties.getProperty("repoURL");
			ami = properties.getProperty("ami");
			instanceType = properties.getProperty("instanceType");
	
			
			if ( role.equals("manager") ) {
				
				// If this is the manager, we put ourself in the workers list
				Worker worker = new Worker();
				worker.setDns("");
				worker.setStatus("ready");
				
				WorkerDAO wdao = new WorkerDAO();
				workerId = wdao.insert(worker);
				
				// Also, if this is the manager, put the masterDns value to "", so the Sender class detects it
				managerDns = "";
				
				// Obtain our own public IP address
			    String s;
			    URL u = new URL("http://bot.whatismyipaddress.com/");
		    	BufferedReader in = new BufferedReader( new InputStreamReader(u.openStream()) );
		    	while ((s = in.readLine()) != null) {
		            dns = s;
		        }
		    	in.close();
			    
			    logger.info("DNS "+dns);
				
				
			}
			// If this is a worker, read the property from the properties file
			else {
				managerDns = properties.getProperty("managerDns");
				
				// Obtain our own public IP address
			    String s;
			    URL u = new URL("http://bot.whatismyipaddress.com/");
		    	BufferedReader in = new BufferedReader( new InputStreamReader(u.openStream()) );
		    	while ((s = in.readLine()) != null) {
		            dns = s;
		        }
		    	in.close();
			
				// We must contact the master here, so they know we've launched
				// Send message to manager when done
				
				HttpSender sender = new HttpSender();
				
				// The workerId is automatically added to the message
				JsonObject message = new JsonObject();
				message.add("action", Action.WORKER_DEPLOYED.getId() );
				message.add("dns", dns);
				

				sender.setMessage(message);
				sender.setDestinationIP( ServerProperties.getManagerDns() );
				sender.setDestinationRole("manager");
				String response = "";
				try {
					response = sender.send();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				JsonObject jsonResponse = JsonObject.readFrom(response);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Context initialized. Name="+name +" Role="+role);
		
	}

	public static String getRole() {
		return role;
	}

	public static void setRole(String role) {
		ServerProperties.role = role;
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		ServerProperties.name = name;
	}

	public static String getDns() {
		return dns;
	}

	public static void setDns(String dns) {
		ServerProperties.dns = dns;
	}

	public static int getWorkerId() {
		return workerId;
	}

	public static void setWorkerId(int workerId) {
		ServerProperties.workerId = workerId;
	}

	public static String getManagerDns() {
		return managerDns;
	}

	public static void setManagerDns(String masterDns) {
		ServerProperties.managerDns = masterDns;
	}
	
	public static String getKeypair(){
		return keypair;
	}
	
	public static String getSecutiryGroup(){
		return securityGroup;
	}
	
	public static String getRepoURL(){
		return repoURL;
	}
	public static String getAMI(){
		return ami;
	}
	public static String getInstanceType(){
		return instanceType;
	}
	

	private boolean delete(File f) {
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      delete(c);
	  }
	  return f.delete();
	}
	private void deleteContents(File f) {
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      delete(c);
	  }
	}

}
