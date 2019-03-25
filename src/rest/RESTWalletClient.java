package rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.net.ssl.SSLSession;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import wallet.Wallet;

public class RESTWalletClient implements Wallet {

	// Constants
	private static final String PATH = "./tls/Client/"; // "/home/sd"
	private static final String CLIENT_KEYSTORE = PATH + "client-ks.jks";
	private static final String CLIENT_KEYSTORE_PWD = "CSD1819";
	private static final String CLIENT_TRUSTSTORE = PATH + "client-ts.jks";
	private static final String CLIENT_TRUSTSTORE_PWD = "CSD1819";

	private final int MAX_TRIES = 5;
	private final int CONNECT_TIMEOUT = 15000;
	private final int READ_TIMEOUT = 10000;

	// Private Variables
	private Client client;
	private Logger logger;
	private String[] servers;

	public RESTWalletClient(String[] servers) { 
		this.servers = servers;

		System.setProperty("javax.net.ssl.keyStore",  CLIENT_KEYSTORE);
		System.setProperty("javax.net.ssl.keyStorePassword", CLIENT_KEYSTORE_PWD);
		System.setProperty("javax.net.ssl.trustStore", CLIENT_TRUSTSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", CLIENT_TRUSTSTORE_PWD);

		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		client = ClientBuilder.newBuilder().hostnameVerifier((String hostname, SSLSession cts) -> true).withConfig(config).build();

		logger = Logger.getLogger(RESTWalletClient.class.getName());
	}

	private <T> T processRequest(RequestHandler<T> requestHandler) {
	
		List<String> servers = new ArrayList<>(Arrays.asList(this.servers));
		int index = -1;
		
		for (int current_try = 0; current_try < MAX_TRIES; current_try++) {
			try {
				index = (int) Math.round(Math.random()*servers.size());
				//logger.log(Level.INFO, String.format("Contacting server %s .... retry: %d", servers.get(index), current_try));
				return requestHandler.execute(servers.get(index));
			} catch (ProcessingException pe) {
				pe.printStackTrace();
				logger.log(Level.INFO, String.format("Error contacting server %s .... retry: %d", servers.get(index), current_try));
				servers.remove(index);
			}
		}

		logger.log(Level.WARNING, String.format("Aborted request! Too many tries..."));
		return null;
	}

	// TODO: verificar se a passagem de parametros está correta: amount é não negativa e assim?

	// TODO: Colocar as repetições de pedidos como em SD

	@Override
	public int createMoney(String who, int amount) {

		Integer balance = processRequest((location) -> {
			Response response = client.target(location).path(Wallet.PATH + "/create/").queryParam("who", who).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));
			
			if (response.getStatus() == 200) {
				return response.readEntity(Integer.class);
			} else
				throw new RuntimeException("WalletClient Create: " + response.getStatus());
		});

		return balance != null ? balance : 0;
	}

	@Override
	public boolean transfer(String from, String to, int amount) {
		Boolean status = processRequest((location) -> {
			Response response = client.target(location).path(Wallet.PATH + "/transfer/").queryParam("from", from).queryParam("to", to).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));
			
			if (response.getStatus() == 200) {
				return response.readEntity(Boolean.class);
			} else
				throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
		});

		return status != null ? status : false;
	}

	@Override
	public int currentAmount(String who) {
		Integer balance = processRequest((location) -> {
			Response response = client.target(location).path(Wallet.PATH + "/" + who + "/").request().get();
			
			if (response.getStatus() == 200) {
				return response.readEntity(Integer.class);
			} else
				throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
		});

		return balance != null ? balance : 0;
	}

}
