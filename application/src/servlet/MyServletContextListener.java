package servlet;

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
public class MyServletContextListener implements ServletContextListener {
	
	Logger logger = Logger.getLogger(MyServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
		logger.info("Context destroyed");
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		logger.info("Context initialized");
		
	}

}
