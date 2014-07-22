package com.pod.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.pod.dao.ActivityDAO;
import com.pod.dao.PolicyDAO;
import com.pod.dao.WorkerDAO;
import com.pod.interaction.Action;
import com.pod.listeners.ServerProperties;
import com.pod.model.Activity;
import com.pod.model.Policy;
import com.pod.model.Worker;

/**
 * This class is in charge of handling workers (deploying, starting, stopping, etc)
 */
public class WorkerHandler {
	
	private static boolean securityGroupCreated;
	
	/**
	 * Constructor or a Worker Handler instance
	 */
	public WorkerHandler(){
	}
	
	public int getTotalWorkers() {
		return new WorkerDAO().list().length; // the list method already retrieves only non terminated workers
	}
	public int getReadyWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"ready"} ).length;
	}
	public int getPendingWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"pending"} ).length;
	}
	public int getLaunchingWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"launching"} ).length;
	}
	public int getWorkingWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"working"} ).length;
	}
	public int getStoppedWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"stopped"} ).length;
	}
	public int getTerminatedWorkers() {
		return new WorkerDAO().selectByStatus( new String[] {"terminated"} ).length;
	}

	
	public JsonObject getWorkers() {
		
		JsonObject response = new JsonObject();
		
		JsonObject countJson = new JsonObject();
		countJson.add("ready", getReadyWorkers() );
		countJson.add("pending", getPendingWorkers());
		countJson.add("launching", getLaunchingWorkers() );
		countJson.add("working", getWorkingWorkers() );
		countJson.add("stopped", getStoppedWorkers() );
		countJson.add("terminated", getTerminatedWorkers() );
		countJson.add("totalWorkers", getTotalWorkers());
		response.add("count", countJson);	
		
		JsonArray workersJson = new JsonArray();
		for ( Worker w : new WorkerDAO().list() ) {
			workersJson.add(w.toJsonObject());
		}
		
		return response.add("workers", workersJson);
	}
	
	public boolean deployWorker(){
		
		// Create worker object
		Worker worker = new Worker();
		worker.setStatus("launching");
		
		// Save the new worker in the database with launching status
		// Status will be changed when the worker is pending and when it makes the first contact
		WorkerDAO wdao = new WorkerDAO();
		int workerId = wdao.insert(worker);
		
		// In case an error happened with the database
		if ( workerId < 0 ) 
			return false;
		
		// Deploy the worker and get the id that was assigned by the cloud provide
		String instanceId;
		try {
			
			instanceId = deployWorkerAction( workerId );
			
		} catch (IOException e) {
			return false;
		}
		
		if ( "".equals(instanceId) ) {
			
			return false;
		}
		
		// Update worker in database
		worker.setId(workerId);
		worker.setStatus("pending");
		wdao.update(worker);
		
		return true;
	}
	
	public void startWorker(int id){}
	
	public void stopWorker(int id){}
	
	public JsonObject handleTerminationRequest ( JsonObject json ) {
		
		if ( json.get("workerId") == null )
			return new JsonObject().add("error", "no worker id specified");
		
		int workerId = json.get("workerId").asInt();
		
		WorkerDAO wdao = new WorkerDAO();
		Worker worker = wdao.select(workerId);
		
		if ( worker == null )
			return new JsonObject().add("error", "no worker found with that id");
		
		if ( worker.isManager() )
			return new JsonObject().add("error", "can't terminate manager");
		
		worker.setStatus("terminated");
		wdao.update(worker);
		
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add( worker.getInstanceId() );
		
		terminateWorkerAction ( instanceIds );
		
		return new JsonObject().add("action", Action.ACK.getId());
	}
	
	/**
	 * At the moment, this method terminates the workers, interrupting whatever they were doing
	 * For a future iteration, the correct process would be to try to notify the worker first, so the worker does it
	 * @param workersToTerminate
	 */
	public void terminateWorkers ( int workersToTerminate ){
		
		System.out.println("Proceeding to terminate "+workersToTerminate+" workers");
		
		List<String> instanceIds = new ArrayList<String>();

		WorkerDAO wdao = new WorkerDAO();
		
		Worker[] workers = wdao.list();
		
		int flaggedWorkers = 0;
		for ( Worker worker : workers ) {
			
			if ( flaggedWorkers >= workersToTerminate )
				break;
			
			if ( !worker.isManager() && !worker.getStatus().equals("terminated") ) {
				worker.setStatus("terminated");
				wdao.update(worker);
				instanceIds.add(worker.getInstanceId());
				flaggedWorkers++;
			}
			
		}
		
		System.out.println(workersToTerminate+" have been successfully flagged");

		if ( !instanceIds.isEmpty() )
			terminateWorkerAction( instanceIds );
	}
	
	
	public void terminateWorkerAction ( List<String> instanceIds ) {
		
		// Try to create the client
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider("/main/resources/AwsCredentials.properties");
		AmazonEC2 amazonEC2Client = new AmazonEC2Client(credentialsProvider);
		
		TerminateInstancesRequest tir = new TerminateInstancesRequest ( instanceIds );
		amazonEC2Client.terminateInstances(tir);
	}


	private String deployWorkerAction( int workerId ) throws IOException {
		
		// Try to create the client
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider("/main/resources/AwsCredentials.properties");
		AmazonEC2 amazonEC2Client = new AmazonEC2Client(credentialsProvider);
		
		// Attempt to create security group in case it isn't already created
		if ( !securityGroupCreated ){
			// This strange OR operation is to ensure that the boolean will stay true if set first one thread
			securityGroupCreated = createSecurityGroup( amazonEC2Client , ServerProperties.getName()+"-wkr-grp" ) || securityGroupCreated;
		}
		
		// Read user data file
		// http://www.mkyong.com/java/how-to-read-file-from-java-bufferedreader-example/
		String userData = "";
		BufferedReader br = new BufferedReader(new InputStreamReader( getClass().getResourceAsStream("/main/resources/worker_setup.sh") ));

		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			userData += sCurrentLine +"\n";
		}
		br.close();

		// Set variables for the worker setup script
		userData = userData.replace("NAME=", "NAME="+ServerProperties.getName())
						   .replace("MANAGER_LOCAL_IP=", "MANAGER_LOCAL_IP="+ServerProperties.getManagerLocalIp())
						   .replace("WORKER_ID=", "WORKER_ID=" + workerId)
		                   .replace("REPO_URL=", "REPO_URL="+ServerProperties.getRepoURL())
		                   .replace("KEYPAIR=", "KEYPAIR="+ServerProperties.getKeypair())
		                   .replace("SECURITY_GROUP=", "SECURITY_GROUP="+ServerProperties.getSecutiryGroup());
		
		
		// Run instance
		RunInstancesRequest runInstancesRequest = 
				  new RunInstancesRequest();

		runInstancesRequest.withImageId( ServerProperties.getAMI() )
		                     .withInstanceType( ServerProperties.getInstanceType() )
		                     .withMinCount(1)
		                     .withMaxCount(1)
		                     .withKeyName( ServerProperties.getKeypair() )
		                     .withSecurityGroups( ServerProperties.getName()+"-wkr-grp" )
		                     .withUserData(new String(Base64.encodeBase64(userData.getBytes("UTF-8"))));



		 RunInstancesResult runInstancesResult = amazonEC2Client.runInstances(runInstancesRequest);
		
		 
		 // Tag instance
		 List<Instance> instances = runInstancesResult.getReservation().getInstances();
		 String instanceId = "";
		 for (Instance instance : instances) {
			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			instanceId = instance.getInstanceId();
			createTagsRequest.withResources(instanceId)
			   .withTags(new Tag("Name", ServerProperties.getName()+"-worker" ));
			amazonEC2Client.createTags(createTagsRequest);
		 }

		 return instanceId;
	}

	
	public JsonObject workerDeployed (JsonObject json) {
		
		int workerId = json.get("workerId").asInt();
		
		Worker worker = new Worker();
		worker.setId(workerId);
		worker.setStatus("ready");
		worker.setLocalIp( json.get("localIp").asString() );
		worker.setPublicIp( json.get("publicIp").asString() );
		worker.setInstanceId( json.get("instanceId").asString() );
		
		WorkerDAO wdao = new WorkerDAO();
		wdao.update( worker );
		
		// Now that the manager knows that the worker exists, we must notify the installations to the worker
		// Right now, we send one message per activity to the worker
		ActivityDAO adao = new ActivityDAO();
		for ( Activity activity : adao.list() )
			new Thread ( new ActivityInstallationNotifier(activity, Action.INSTALL_ACTIVITY ).setSpecificWorker(worker) ).start();
		
		return new JsonObject().add("action", Action.ACK.getId());
	}
	
	/** 
	 * This method attempts to create a security group with the specified name 
	 * @param amazonEC2Client
	 * @param securityGroupName
	 * @return false in case there was a problem creating it, or true if success
	 */
	private synchronized boolean createSecurityGroup( AmazonEC2 amazonEC2Client, String securityGroupName ) {
		
		try {
			// Try to create the security group
			CreateSecurityGroupRequest createSecurityGroupRequest = 
					new CreateSecurityGroupRequest();
			createSecurityGroupRequest.withGroupName(securityGroupName)
				.withDescription("Security group for workers in POD. Name is "+securityGroupName);

			amazonEC2Client.createSecurityGroup(createSecurityGroupRequest);

			// Authorize post 22
			IpPermission ipPermission = 
					new IpPermission();

			ipPermission.withIpRanges("0.0.0.0/0")
				            .withIpProtocol("tcp")
				            .withFromPort(22)
				            .withToPort(22);
			AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
					new AuthorizeSecurityGroupIngressRequest();

			authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
				                                    .withIpPermissions(ipPermission);

			amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

			// Authorize post 80
			ipPermission = 
					new IpPermission();

			ipPermission.withIpRanges("0.0.0.0/0")
				            .withIpProtocol("tcp")
				            .withFromPort(80)
				            .withToPort(80);
			authorizeSecurityGroupIngressRequest =
					new AuthorizeSecurityGroupIngressRequest();

			authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
				                                    .withIpPermissions(ipPermission);

			amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

			// Authorize post 8080
			ipPermission = 
					new IpPermission();

			ipPermission.withIpRanges("0.0.0.0/0")
				            .withIpProtocol("tcp")
				            .withFromPort(8080)
				            .withToPort(8080);
			authorizeSecurityGroupIngressRequest =
					new AuthorizeSecurityGroupIngressRequest();

			authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
				                                    .withIpPermissions(ipPermission);

			amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
			
			return true;
		}
		// Exception if the group already existed
		catch (Exception e) {
			return false;
		}
		
	}

}
