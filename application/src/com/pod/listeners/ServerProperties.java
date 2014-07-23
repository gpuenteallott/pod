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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import main.resources.PodLogger;

import com.eclipsesource.json.JsonObject;
import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.manager.SystemSchedulerTask;
import com.pod.model.Policy;
import com.pod.model.Worker;
import com.pod.worker.WorkerUpdaterTask;


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

	public static PodLogger log = new PodLogger("ServerProperties");
	
	// This is the default location for the server properties, except in the case of the manager
	private static final String PROPERTIES_FILE_PATH = "/home/pod/server.properties";
	
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private static final int PERIODIC_CHECKS_INTERVAL = 2*60 * 1000; // every 2 min
	private static final int WORKER_PERIODIC_UPDATES_INTERVAL = 2*60*1000; // every 2 mins
	public static int DEFAULT_TIME_TO_TERMINATE = 45*60*1000; // 45 mins
	public static int DEFAULT_TERMINATION_TIME = 45*60*1000; // 45 mins
	public static int DEFAULT_ERROR_TIMEOUT = 5*60*1000; // 5 mins
	
	private static String role;
	private static String name;
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
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.i("Context destroyed");
	}

	/**
	 * Start the context and load this server specific properties
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		log.i("Initializing Context");
		
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
				
				log.i("Performing Manager setup");
				
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
				
				if ( !thisIsARedeploy ) {

					workerId = wdao.insert(worker);
					
					Policy defaultPolicy = new Policy("default");
					defaultPolicy.setRule("fixedWorkers", "1");
					defaultPolicy.setActive(true);
					
					PolicyDAO pdao = new PolicyDAO();
					pdao.insert(defaultPolicy);
					pdao.setActive(defaultPolicy);
					
					// delete contents that might have been left there (this is not a redeploy preserrving database)
					if ( new File("/home/pod/app").exists() )
						deleteContents(new File("/home/pod/app"));
					
				}
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
				
				// Set up the timer for periodic tasks
				Timer time = new Timer();
				SystemSchedulerTask sct = new SystemSchedulerTask();
				time.schedule(sct, PERIODIC_CHECKS_INTERVAL, PERIODIC_CHECKS_INTERVAL);

				
			}
			// If this is a worker, read the property from the properties file
			else {
				
				log.i("Performing Worker setup");
				
				managerLocalIp = properties.getProperty("managerLocalIp");
				workerId = Integer.parseInt( properties.getProperty("workerId") );
			
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
				
				// Set up the timer for periodic tasks
				Timer time = new Timer();
				WorkerUpdaterTask sct = new WorkerUpdaterTask();
				time.schedule(sct, WORKER_PERIODIC_UPDATES_INTERVAL, WORKER_PERIODIC_UPDATES_INTERVAL);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.i("Context initialized. Name="+name +" Role="+role);
		
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
