package ch.exense.step.examples.helper;

import step.client.StepClient;

public class StepClientCustom extends StepClient {
	
	CustomRemoteClient remoteClient;
	
	public StepClientCustom(String controllerUrl, String username, String password) {
		super();
		remoteClient = new CustomRemoteClient(credentials);
	}
	
	public String getCookies() {
		return remoteClient.getCookies().get("sessionid").toString();
	}
	
}
