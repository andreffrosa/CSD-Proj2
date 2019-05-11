package replicaManager;

import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class ReplicaManagerRESTClient {
	
	// Constants
	private static final String PATH = "./tls/ReplicaManagerClient/";
	private static final String CLIENT_KEYSTORE = PATH + "client-ks.jks";
	private static final String CLIENT_KEYSTORE_PWD = "CSD1819";
	private static final String CLIENT_TRUSTSTORE = PATH + "client-ts.jks";
	private static final String CLIENT_TRUSTSTORE_PWD = "CSD1819";

	private final int CONNECT_TIMEOUT = 35000;
	private final int READ_TIMEOUT = 30000;

	// Private Variables
	private Client client;
	private String server_location;
	private String password;

	public ReplicaManagerRESTClient(String password, String server_location) {
		this.server_location = server_location;
		this.password = password;

		System.setProperty("javax.net.ssl.keyStore", CLIENT_KEYSTORE);
		System.setProperty("javax.net.ssl.keyStorePassword", CLIENT_KEYSTORE_PWD);
		System.setProperty("javax.net.ssl.trustStore", CLIENT_TRUSTSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", CLIENT_TRUSTSTORE_PWD);

		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		client = ClientBuilder.newBuilder().hostnameVerifier((String hostname, SSLSession cts) -> true)
				.withConfig(config).build();
	}

	public void launch(String fileName, byte[] hash, String className, String[] args) throws InvalidPasswordException, HashMisMatchException {

		LaunchRequest request = new LaunchRequest(password, fileName, hash, className, args);

		Response response = client.target(server_location).path(ReplicaManagerService.PATH + "/launch/").request()
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));

		if (response.getStatus() == 200 || response.getStatus() == 204) {
			return;
		} else if(response.getStatus() == 401) {
			String message = response.readEntity(String.class);
			throw new InvalidPasswordException(message);
		} else if(response.getStatus() == 412) {
			String message = response.readEntity(String.class);
			throw new HashMisMatchException(message);
		} else {
			throw new RuntimeException("ReplicaManagerRESTClient launch: " + response.getStatus() + "\n"+ response.toString());
		}
	}

	public void stop() throws InvalidPasswordException {
		Response response = client.target(server_location).path(ReplicaManagerService.PATH + "/stop/").request()
				.post(Entity.entity(password, MediaType.APPLICATION_JSON));

		if (response.getStatus() == 200 || response.getStatus() == 204) {
			return;
		} else if(response.getStatus() == 401) {
			String message = response.readEntity(String.class);
			throw new InvalidPasswordException(message);
		} else
			throw new RuntimeException("ReplicaManagerRESTClient stop: " + response.getStatus());
	}

}
