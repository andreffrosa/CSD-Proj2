package secureModule;

import java.math.BigInteger;

import javax.net.ssl.SSLSession;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.google.gson.GsonBuilder;

import bft.reply.InvalidRepliesException;
import rest.RequestHandler;

public class SecureModuleRESTClient {

	// Constants
		private static final String PATH = "./tls/SecureModuleClient/";
		private static final String CLIENT_KEYSTORE = PATH + "client-ks.jks";
		private static final String CLIENT_KEYSTORE_PWD = "CSD1819";
		private static final String CLIENT_TRUSTSTORE = PATH + "client-ts.jks";
		private static final String CLIENT_TRUSTSTORE_PWD = "CSD1819";

		private final int MAX_TRIES = 5;
		private final int CONNECT_TIMEOUT = 35000;
		private final int READ_TIMEOUT = 30000;

		// Private Variables
		private Client client;
		private String server;

		public SecureModuleRESTClient(String server) {
			this.server = server;

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

		private <T> T processRequest(RequestHandler<T> requestHandler) {
			
			for (int current_try = 0; current_try < MAX_TRIES; current_try++) {

				try {
					return requestHandler.execute(server);
				} catch (ProcessingException e) {
					e.printStackTrace();
				} catch (InvalidRepliesException e) {
				}
			}

			throw new RuntimeException("Aborted request! Too many tries...");
		}
	
	public long addOPI(long opi, int amount, String cipheredKey) {
		
		String request = new GsonBuilder().create().toJson(new AddRequest(opi, amount, cipheredKey));
		
		return processRequest((location) -> {
			Response response = client.target(location).path(SecureModule.PATH + SecureModule.ADD_OPI_PATH)
					.request()
					.post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return response.readEntity(long.class);
			} else
				throw new RuntimeException("SecureModule addOPI: " + response.getStatus());
		});
	}

	public int compareSumInt(BigInteger v1, BigInteger v2, String cipheredKey) {
		
		return processRequest((location) -> {
			Response response = client.target(location).path(SecureModule.PATH + SecureModule.COMPARE_SUM_PATH)
					.request()
					.post(Entity.entity(new CompareRequest(v1, v2, cipheredKey), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return response.readEntity(int.class);
			} else
				throw new RuntimeException("SecureModule compareSumInt: " + response.getStatus());
		});
	}

}
