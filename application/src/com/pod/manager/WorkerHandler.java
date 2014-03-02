package com.pod.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.pod.dao.WorkerDAO;
import com.pod.listeners.ServerProperties;
import com.pod.model.Worker;

/**
 * This class is in charge of handling workers (deploying, starting, stopping, etc)
 */
public class WorkerHandler {

	private static int numWorkers;
	private static int pendingWorkers;
	
	private static boolean securityGroupCreated;
	
	/**
	 * Constructor or a Worker Handler instance
	 */
	public WorkerHandler(){
	}
	
	public boolean deployWorker(){
		
		pendingWorkers++;
		
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
	
	public void startWorker(){}
	
	public void stopWorker(){}
	
	public void terminateWorker(){}


	private String deployWorkerAction( int workerId ) throws IOException {
		
		// Try to create the client
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider("/main/resources/AwsCredentials.properties");
		AmazonEC2 amazonEC2Client = new AmazonEC2Client(credentialsProvider);
		
		// Attempt to create secutiry group in case it isn't already created
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
		userData = userData.replace("CORE_PUBLIC_DNS=", "CORE_PUBLIC_DNS="+ServerProperties.getMasterDns())
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
			   .withTags(new Tag("Name", ServerProperties.getInstanceType()+"-wkr" ));
			amazonEC2Client.createTags(createTagsRequest);
		 }

		 return instanceId;
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
