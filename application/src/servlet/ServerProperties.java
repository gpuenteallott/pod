package servlet;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


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
	private static String publicDNS;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
		logger.info("Context destroyed");
		
	}

	/**
	 * Start the context and load this server specific properties
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		try {
			Properties properties = new Properties();
			properties.load( getClass().getResourceAsStream("/Server.properties"));
			role = properties.getProperty("role");
			name = properties.getProperty("name");
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

	public static String getPublicDNS() {
		return publicDNS;
	}

	public static void setPublicDNS(String publicDNS) {
		ServerProperties.publicDNS = publicDNS;
	}

}
