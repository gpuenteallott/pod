package worker;

import interaction.Action;
import interaction.Sender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import model.Execution;
import servlet.ServerProperties;

import com.eclipsesource.json.JsonObject;

/**
 * This class is a runnable that performs the executions
 * The execution to handle is given in the constructor
 *
 * After starting the process, a message will be sent to the manager of this worker informing about it
 * When the execution is done, the worker will send a message to the manager with both standard output and standard error
 */
public class ExecutionPerformer implements Runnable {
	
	private Execution execution;
	
	public ExecutionPerformer ( Execution execution ) {
		this.execution = execution;
	}
	
	public void run () {
		
		// Prepare message in case of error
		JsonObject message = null;
		
		File executableFile = new File ("/home/user/app/"+execution.getActivityName()+"/main.sh");
		
		// If the executable file isn't located, it might be because it was deleted or because the activity isn't installed
		if ( !executableFile.exists() ) {
			
			message = new JsonObject();
			
			// We add the execution id to the error response so the manager can identify it
			JsonObject executionJson = new JsonObject().add("id", execution.getId());
			message.add("execution", executionJson);
			
			message.add("status", "error");
			if ( new File ("/home/user/app/"+execution.getActivityName() ).exists() )
				message.add("errorDescription", "The executable file is missing"); // Add error description to the response
			else
				message.add("errorDescription", "The specified activity isn't installed"); // Add error description to the response
			
		}
		// If the executable file is located
		else
			message = execute();
		
		// Set message action
		message.add("action", Action.EXECUTION_REPORT.getId());
		
		// Set the public DNS of the worker. If empty, it will mean this same machine
		Sender sender = new Sender();
		sender.setDestinationIP( ServerProperties.getMasterDns() );
		sender.setDestinationRole("manager");
		sender.setMessage(message);
		
		String response = null;
		try {
			response = sender.send();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		JsonObject jsonResponse = JsonObject.readFrom(response);
		
		// In case there is a new execution to perform
		if ( jsonResponse.get("action").asInt() == Action.NEW_EXECUTION.getId() ) {
			
			Execution newExecution = new Execution (jsonResponse.get("execution").asObject());
			
			// Launch new thread to perform the execution, so this current thread will end
			new Thread ( new ExecutionPerformer(newExecution) ).start();
		}
	}
	
	private JsonObject execute () {
		
		ProcessBuilder processBuilder;
		if ( execution.getInput() != null )
			processBuilder = new ProcessBuilder("/home/user/app/"+execution.getActivityName()+"/main.sh", execution.getInput());
		else
			processBuilder = new ProcessBuilder("/home/user/app/"+execution.getActivityName()+"/main.sh");

		processBuilder.directory(new File ("/home/user/app/"+execution.getActivityName()));

		
		Process process = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String line = null;
		String stdout = "";
		String stderr = "";
		
		// try catch for IO errors in the process
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the output of the process
		is = process.getInputStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		
		// try catch for IO output in the process
		try {
			while ((line = br.readLine()) != null) {
				// Every line of standard output
				System.out.println(line);
				stdout += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the error output of the process
		is = process.getErrorStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		
		// try catch for IO errors in the process
		try {
			while ((line = br.readLine()) != null) {
				// Every line of standard error
				System.err.println(line);
				stderr += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Prepare message to send to manager
		JsonObject message = new JsonObject();
		
		execution.setOutput(stdout);
		execution.setError(stderr);
		
		message.add("execution", execution.toJsonObject());
		message.add("status", "finished");
		
		return message;
	}

	public Execution getExecution() {
		return execution;
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}

}
