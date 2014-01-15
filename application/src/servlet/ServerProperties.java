package servlet;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import dao.ActivityDAO;
import dao.WorkerDAO;
import model.Worker;


/**
 * To make the listener work correctly, this line had to be commented in the server.xml file:
 * 
 * <Context docBase="ROOT" path="/ROOT" reloadable="true" source="org.eclipse.jst.jee.server:ROOT"/>
 * 
 * The reason is that otherwise the context was initialized twice.
 *
 */
public class ServerProperties implements ServletContextListener {
	
	Logger logger = Logger.getLogger(ServerProperties.class.getName());
	
	private static String role;
	private static String name;
	private static String dns;
	private static String workerId;
	private static String masterDns;

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
		
		// Load properties
		try {
			Properties properties = new Properties();
			properties.load( getClass().getResourceAsStream("/Server.properties"));
			role = properties.getProperty("role");
			name = properties.getProperty("name");
			
			if ( role.equals("master") ) {
				
				// If this is the master, we put ourself in the workers list
				Worker worker = new Worker();
				worker.setDns("");
				worker.setStatus("ready");
				
				WorkerDAO wdao = new WorkerDAO();
				wdao.insert(worker);
				
				// Also, if this is the master, put the masterDns value to "", so the Sender class detects it
				masterDns = "";
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

	public static String getWorkerId() {
		return workerId;
	}

	public static void setWorkerId(String workerId) {
		ServerProperties.workerId = workerId;
	}

	public static String getMasterDns() {
		return masterDns;
	}

	public static void setMasterDns(String masterDns) {
		ServerProperties.masterDns = masterDns;
	}

}
