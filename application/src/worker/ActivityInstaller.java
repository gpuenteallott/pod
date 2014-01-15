package worker;

import java.io.IOException;

import servlet.ServerProperties;

import com.eclipsesource.json.JsonObject;

import interaction.Sender;
import model.Activity;

public class ActivityInstaller implements Runnable {
	
	private Activity activity;
	
	public ActivityInstaller (Activity activity){
		this.activity = activity;
	}

	@Override
	public void run() {
		
		System.out.println("worker installing activity at installer thread");
		// Install activity
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("worker installed activity at installer thread");
		
		// Send message to master when done
		Sender sender = new Sender();
		
		JsonObject message = new JsonObject();
		message.add("for", "master");
		message.add("action", "installActivityReport");
		message.add("activity", activity.toJsonObject());
		message.add("status", "installed");
		
		sender.setMessage(message);
		sender.setDestination( ServerProperties.getMasterDns() );
		try {
			System.out.println("a message is going to be sent from worker to "+ServerProperties.getMasterDns());
			String response = sender.send();
			System.out.println("worker sent installation report: "+response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
