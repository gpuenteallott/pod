package main.resources;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logging solution for POD
 * The purpose of this class was being able to customize the log format
 * @author will
 */
public class PodLogger {
	
	private String className;
	private static SimpleDateFormat DT = new SimpleDateFormat("hh:mm:ss");
	
	public PodLogger ( String className ) {
		this.className = className;
	}
	
	public void i ( String message ) {
		System.out.println( "" + DT.format(new Date()) +" "+className+" - "+message );
	}
	
	public void e ( String message ) {
		System.out.println( "" + DT.format(new Date()) +" "+className+" - "+message + " -- ERROR" );
	}

}
