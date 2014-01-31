package com.pod.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.eclipsesource.json.JsonObject;
import com.pod.interaction.Action;
import com.pod.interaction.HttpSender;
import com.pod.listeners.ServerProperties;
import com.pod.model.Activity;
import com.pod.model.Execution;

/**
 * This class is a runnable that performs the (un)installation of activities
 * The activity to handle is given in the constructor
 * If the purpose of the runnable is performing an uninstallation, it must be specified through the uninstall flag
 * 
 * After the (un)installation, a message will be sent to the manager of this worker informing about the result
 */
public class ActivityInstaller implements Runnable {
	
	private Activity activity;
	private boolean uninstall;
	private String errorDescription;
	
	public ActivityInstaller (Activity activity){
		this.activity = activity;
		this.errorDescription = "";
	}
	
	public ActivityInstaller (Activity activity, boolean uninstall){
		
		this.activity = activity;
		this.uninstall = uninstall;
		this.errorDescription = "";
	}
	
	public void setUninstall (boolean uninstall) {
		this.uninstall = uninstall;
	}

	@Override
	public void run() {
		
		ServerProperties.setWorking(true);
		
		// Logging
			if ( ! uninstall ) System.out.println("Worker: installing activity "+activity.getName());
			else System.out.println("Worker: (un)installing activity "+activity.getName());
		// End logging
		
		// Install or uninstall activity
		boolean success = false;
		if ( !uninstall )
			success = install();
		else 
			success = uninstall();
			
		
		// If the installation was unsuccessful, we delete every files that were possibly downloaded in order be as if it didn't happen
		if ( !success && !uninstall ) {
			delete( new File ("/home/user/app/"+activity.getName()) );
		}
		
		// Logging
			if ( ! uninstall ) System.out.println("Worker: done installing activity "+activity.getName());
			else System.out.println("Worker: done uninstalling activity "+activity.getName());
		// End logging
		
		// Send message to manager when done
		HttpSender sender = new HttpSender();
		
		JsonObject message = new JsonObject();
		message.add("action", Action.INSTALL_ACTIVITY_REPORT.getId() );
		message.add("activity", activity.toSmallJsonObject());
		
		// Add status depending on flags
		if ( !success ) {
			message.add("status", "error");
			message.add("errorDescription", errorDescription); // Add error description to the response
		}
		else if ( success && !uninstall )
			message.add("status", "installed");
		else if ( success && uninstall )
			message.add("status", "uninstalled");
		
		
		// Now we check if there are pending INSTALLATIONS
		// In case there aren't, we do nothing
		// In case there are, we start processing it and send the manager a flag indicating that we don't want a new execution right away
		ActivityInstallationQueue aiqueue = new ActivityInstallationQueue();
		if ( !aiqueue.isEmpty() ) {
			
			message.add("executionChaining", false);
			
			// Start installer execution in a new thread
			new Thread ( new ActivityInstaller(aiqueue.pull()) ).start();
		}
		
		
		sender.setMessage(message);
		sender.setDestinationIP( ServerProperties.getMasterDns() );
		sender.setDestinationRole("manager");
		String response = "";
		try {
			response = sender.send();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JsonObject jsonResponse = JsonObject.readFrom(response);
		
		// In case there is a new execution to perform
		if ( jsonResponse.get("action").asInt() == Action.PERFORM_EXECUTION.getId() ) {
			
			Execution newExecution = new Execution (jsonResponse.get("execution").asObject());
			
			// Launch new thread to perform the execution, so this current thread will end
			new Thread ( new ExecutionPerformer(newExecution) ).start();
		}
		
		
		// If there is no other execution to perform, we change the status of the worker
		// This might happen because truly there are no pending executions, or because the executionChaining parameter was sent
		else {
			ServerProperties.setWorking(false);
		}
	}
	
	/**
	 * Performs the installation using the activity information that must have been set previously
	 * @return true if the installation was completed, or false otherwise
	 */
	private boolean install () {
		
		// Download the installation script
		String commands = null;
		try {
			commands = downloadTextFromUrl( activity.getInstallationScriptLocation() );
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			errorDescription = "Unable to install, the given installation script URL is malformed";
			return false;
		} catch (IOException e2) {
			e2.printStackTrace();
			errorDescription = "Unable to install, couldn't download installation script";
			return false;
		}
		
		// Prepare commands
		// This way, the process builder accepts all this file as an argument for the main command bash
		
		commands = commands.replace("\r", "")             // Remove carriage return
						   .replaceAll("^null", "")       // Remove possible initial null
						   .replace("\n", ";")            // Replace \n for ; 
						   .replaceAll("#[^;]*", "")      // Remove comments
						   .replaceAll(";{2,}", ";")      // Replace sequences of two or more semicolons for a single one
						   .trim()                        // Remove initial and last spaces
						   .replaceAll("^;", "");         // Remove potential initial semicolon
		

		// Create directory for app
		File appDirectory = new File ("/home/user/app/"+activity.getName());
		
		if ( !appDirectory.mkdirs() ) {
			System.err.println("Directory for app couldn't be created: " + appDirectory.getPath());
			errorDescription = "Unable to install, coudln't create space for the app in the file system";
			return false;
		}
		
		// Variables
		ProcessBuilder processBuilder = new ProcessBuilder("bash","-c",commands);
		processBuilder.directory(appDirectory);
		
		Process process = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		@SuppressWarnings("unused")
		String line = null;
		
		// try catch for IO errors in the process
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
			errorDescription = "Unable to install, the installation script provided caused an IO exception";
			return false;
		}
		
		// Get the output of the process
		is = process.getInputStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		
		// try catch for IO output in the process
		try {
			while ((line = br.readLine()) != null) {
				// Every line of standard output
				//System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorDescription = "Unable to install, couldn't read the standard output of the installation script";
			return false;
		}
		
		// Get the error output of the process
		is = process.getErrorStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		
		// try catch for IO errors in the process
		try {
			while ((line = br.readLine()) != null) {
				// Every line of standard error
				//System.err.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorDescription = "Unable to install, couldn't read the error output of the installation script";
			return false;
		}
		
		// Now we must verify if the code in the folder contains the execution script
		// This script is called main.sh
		
		File executionScript = new File( appDirectory.getPath() + "/main.sh");
		
		if ( !executionScript.exists() ) {
			errorDescription = "Unable to install, execution script main.sh not found in the project root directory";
			return false;
		}
		else if ( !executionScript.isFile() ) {
			errorDescription = "Unable to install, execution script main.sh provided is invalid";
			return false;
		}
		
		// We try to make the file executable
		if ( !executionScript.setExecutable(true) ) {
			errorDescription = "Unable to install, problem with main.sh file permissions";
			return false;
		}
		
		// The executables might have been created in a Windows system
		// In that case, the newlines are represented by \r\n instead of \n
		// This is a problem for executing scripts in the system
		// So we must change that for main.sh in case it comes from Windows
		try {
			new ProcessBuilder("dos2unix", appDirectory.getPath() + "/main.sh").start();
		} catch (IOException e) {
			errorDescription = "Unable to install, problem using dos2unix to modify encoding of main executable";
			e.printStackTrace();
			return false;
		}
		
		// At this point, everything went well!
		return true;
	}
	
	/**
	 * Deletes all files about this activity in this worker
	 * It the app wasn't installed in the worker, returns true
	 * @return true if by the end of the execution of this method, the activity isn't installed
	 */
	private boolean uninstall() {
		
		// If the app isn't installed, we return true to the uninstallation request
		if ( !new File ("/home/user/app/"+activity.getName()).exists() )
			return true;
		
		boolean deleted = delete( new File ("/home/user/app/"+activity.getName()) );
		if ( !deleted )
			errorDescription = "Unable to uninstall, error deleting project files";
		return deleted;
	}
	
	/**
	 * Method to download a text file and place the contents into a string
	 * http://stackoverflow.com/questions/5152450/downloading-a-text-file-from-a-website
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String downloadTextFromUrl( String url ) throws MalformedURLException, IOException
	{  
	    String strFileContents = null;

        String webURL = url;
        URL urlObject = new URL(webURL);

        /* Open a connection to that URL. */
        URLConnection ucon = urlObject.openConnection();

        /*
         * Define InputStreams to read from the URLConnection.
         */
        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        //create a byte array
        byte[] contents = new byte[1024];

        int bytesRead=0;

        while( (bytesRead = bis.read(contents)) != -1){

            strFileContents += new String(contents, 0, bytesRead);
        }

	    return strFileContents;        
	}
	
	private boolean delete(File f) {
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      delete(c);
	  }
	  return f.delete();
	}
}
