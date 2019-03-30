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

import bft.BFTReply;
import wallet.Wallet;

public class RESTWalletClient implements Wallet {

	// Constants
	private static final String PATH = "./tls/Client/"; 
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
			
			if(servers.isEmpty()) {
				logger.log(Level.WARNING, String.format("Aborted request! All the servers didn't respond..."));
				return null;
			}
			
			index = (int) Math.floor(Math.random()*servers.size());
			
			try {
				return requestHandler.execute(servers.get(index));
			} catch (ProcessingException e) {
				if( e.getMessage().contains("java.net.ConnectException: Connection refused (Connection refused)") ) {
					logger.log(Level.INFO, String.format("Error contacting server %s .... retry: %d", servers.get(index), current_try));
					servers.remove(index); 
				} else {
					e.printStackTrace();
					return null;
				}
			}
		}

		logger.log(Level.WARNING, String.format("Aborted request! Too many tries..."));
		return null;
	}

	@Override
	public int createMoney(String who, int amount) {

		BFTReply balance = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/create/").queryParam("who", who).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return (BFTReply) response.readEntity(BFTReply.class);
			} else
				throw new RuntimeException("WalletClient Create: " + response.getStatus());
		});

		if( balance.validateSignatures() )
			return balance.getReplyAsInt();
		else
			throw new RuntimeException("Replies are not valid!");
	}

	@Override
	public boolean transfer(String from, String to, int amount) {

		BFTReply status = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/transfer/").queryParam("from", from).queryParam("to", to).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return (BFTReply) response.readEntity(BFTReply.class);
			} else
				throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
		});

		if( status.validateSignatures() )
			return status.getReplyAsBoolean(); 
		else
			throw new RuntimeException("Replies are not valid!");
	}

	@Override
	public int currentAmount(String who) {
		BFTReply balance = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/" + who + "/").request().get();

			if (response.getStatus() == 200) {
				return (BFTReply) response.readEntity(BFTReply.class);
			} else
				throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
		});

		if( balance.validateSignatures() )
			return balance.getReplyAsInt();
		else
			throw new RuntimeException("Replies are not valid!");
	}

}
