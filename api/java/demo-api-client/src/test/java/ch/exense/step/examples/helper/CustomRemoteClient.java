package ch.exense.step.examples.helper;

import java.util.Map;

import javax.ws.rs.core.NewCookie;

import step.client.AbstractRemoteClient;
import step.client.credentials.ControllerCredentials;

public class CustomRemoteClient extends AbstractRemoteClient {

	public CustomRemoteClient(ControllerCredentials credentials){
		super(credentials);
	}
	
	public CustomRemoteClient(){
		super();
	}
	
	public Map<String, NewCookie> getCookies() {
		return cookies;
	}
}
