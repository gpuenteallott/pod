package com.pod.listeners;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.pod.dao.ActivityDAO;
import com.pod.dao.WorkerDAO;
import com.pod.manager.ExecutionMapCleaner;
import com.pod.model.Worker;


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
	
	private static String role;
	private static String name;
	private static String dns;
	private static int workerId;
	private static String masterDns;
	private static int executionMapExpirationTimeout;
	private static int executionMapChunk;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
		logger.info("Context destroyed");
		
	}

	/**
	 * Start the context and load this server specific properties
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		// Reset the database
		new WorkerDAO().deleteAll();
		new ActivityDAO().deleteAll();
		// Reset the app directory
		File appDirectory = new File ("/home/user/app");
		deleteContents(appDirectory);
		
		// Load properties
		try {
			Properties properties = new Properties();
			properties.load( getClass().getResourceAsStream("/Server.properties"));
			role = properties.getProperty("role");
			name = properties.getProperty("name");
			
			if ( role.equals("manager") ) {
				
				// If this is the manager, we put ourself in the workers list
				Worker worker = new Worker();
				worker.setDns("");
				worker.setStatus("ready");
				
				WorkerDAO wdao = new WorkerDAO();
				workerId = wdao.insert(worker);
				
				// Also, if this is the manager, put the masterDns value to "", so the Sender class detects it
				masterDns = "";
				
				executionMapExpirationTimeout = Integer.parseInt(properties.getProperty("executionMapExpirationTimeout"))*60000;
				executionMapChunk = Integer.parseInt(properties.getProperty("executionMapChunk"));
				
				if ( !ExecutionMapCleaner.isRunning() ) {
					Timer timer = new Timer();
			        timer.schedule(new ExecutionMapCleaner(),
			        		Integer.parseInt(properties.getProperty("executionMapCheckPeriod"))*60000,   //initial delay
			        		Integer.parseInt(properties.getProperty("executionMapCheckPeriod"))*60000);  //subsequent rate
				}
		        
			}
			// If this is a worker, read the property from the properties file
			else
				masterDns = properties.getProperty("masterDns");
			
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

	public static String getMasterDns() {
		return masterDns;
	}

	public static void setMasterDns(String masterDns) {
		ServerProperties.masterDns = masterDns;
	}
	
	public static int getExecutionMapExpirationTimeout () {
		return executionMapExpirationTimeout;
	}
	
	public static int getExecutionMapChunk () {
		return executionMapChunk;
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
