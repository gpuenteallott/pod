package com.pod.listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.eclipsesource.json.JsonObject;
import com.pod.dao.ActivityDAO;
import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.model.Worker;
import com.pod.worker.StatusCheckerTask;


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
	private static final String PROPERTIES_FILE_PATH = "/home/pod/server.properties";
	
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private static final int PERIODIC_CHECKS_INTERVAL = 60 * 1000;
	public static int DEFAULT_TIME_TO_TERMINATE = 45*60*1000; // 45 mins
	
	private static String role;
	private static String name;
	private static String dns;
	private static String securityGroup;
	private static String keypair;
	private static int workerId;
	private static String managerLocalIp;
	private static String repoURL;
	private static String ami;
	private static String instanceType;
	private static String localIp;
	private static String publicIp;
	private static String instanceId;
	private static int terminationTime;
	
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
		
		// Load properties
		try {
			// Load the file in the usual path of the file in the project (case of the manager)
			File serverProperties = new File(PROPERTIES_FILE_PATH);
			InputStream is = new FileInputStream(serverProperties);
			
			Properties properties = new Properties();
			properties.load(is);
			
			role = properties.getProperty("role");
			name = properties.getProperty("name");
			securityGroup = properties.getProperty("securityGroup");
			keypair = properties.getProperty("keypair");
			repoURL = properties.getProperty("repoURL");
			ami = properties.getProperty("ami");
			instanceType = properties.getProperty("instanceType");
			localIp = properties.getProperty("localIp");
			publicIp = properties.getProperty("publicIp");
			instanceId = properties.getProperty("instanceId");
			
			if ( role.equals("manager") ) {
				
				logger.info("Performing Manager setup");
				
				// Reset the database
				//new WorkerDAO().deleteAll();
				//new ActivityDAO().deleteAll();
				//new PolicyDAO().deleteAll();
				
				// If this is the manager, we put ourself in the workers list
				Worker worker = new Worker();
				worker.setLocalIp(localIp);
				worker.setPublicIp(publicIp);
				worker.setInstanceId(instanceId);
				worker.setManager(true);
				worker.setStatus("ready");
				
				// Verify if there is a manager already in the database
				WorkerDAO wdao = new WorkerDAO();
				Worker [] workers = wdao.list();
				boolean thisIsARedeploy = false;
				for ( Worker w : workers )
					if ( w.isManager() ) {
						thisIsARedeploy = true;
						workerId = w.getId();
						break;
					}
				
				if ( !thisIsARedeploy )
					workerId = wdao.insert(worker);
				// We should as the workers what is their state, but for now we're going to set them to ready
				else {
					for ( Worker w : workers ) {
						if ( !w.getStatus().equals("ready") ) {
							w.setStatus("ready");
							wdao.update(w);
						}
					}
				}
				
				managerLocalIp = localIp;
				
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
				
				logger.info("Performing Worker setup");
				
				managerLocalIp = properties.getProperty("managerLocalIp");
				workerId = Integer.parseInt( properties.getProperty("workerId") );
				terminationTime = Integer.parseInt( properties.getProperty("terminationTime") );
				
				// Set up the timer for periodic tasks
				Timer time = new Timer();
				StatusCheckerTask sct = new StatusCheckerTask();
				sct.setLastTimeWorking( new Date());
				time.schedule(sct, PERIODIC_CHECKS_INTERVAL, PERIODIC_CHECKS_INTERVAL);
			
				// We must contact the master here, so they know we've launched
				HttpSender sender = new HttpSender();
				
				// The workerId is automatically added to the message
				JsonObject message = new JsonObject();
				message.add("action", Action.WORKER_DEPLOYED.getId() );
				message.add("localIp", localIp);
				message.add("publicIp", publicIp);
				message.add("instanceId", instanceId);
				

				sender.setMessage(message);
				sender.setDestinationIP( ServerProperties.getManagerLocalIp() );
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

	public static String getManagerLocalIp() {
		return managerLocalIp;
	}

	public static void setManagerLocalIp(String managerLocalIp) {
		ServerProperties.managerLocalIp = managerLocalIp;
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
	public static String getLocalIp(){
		return localIp;
	}
	public static String getPublicIp(){
		return publicIp;
	}
	public static String getInstanceId(){
		return instanceId;
	}
	public static int getTerminationTime() {
		return terminationTime;
	}
	public static void setTerminationTime( int newterminationTime ) {
		terminationTime = newterminationTime;
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
