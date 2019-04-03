package rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.TransferRequest;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class RESTWalletClient implements Wallet {

	// Constants
	private static final String PATH = "./tls/Client/"; 
	private static final String CLIENT_KEYSTORE = PATH + "client-ks.jks";
	private static final String CLIENT_KEYSTORE_PWD = "CSD1819";
	private static final String CLIENT_TRUSTSTORE = PATH + "client-ts.jks";
	private static final String CLIENT_TRUSTSTORE_PWD = "CSD1819";

	private final int MAX_TRIES = 5;
	private final int CONNECT_TIMEOUT = 20000;
	private final int READ_TIMEOUT = 30000;

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
				if( e.getMessage().contains("java.net.ConnectException")  /*|| e.getMessage().contains("java.net.SocketTimeoutException")*/ ) {
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
	public boolean transfer(Transaction transaction) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		/*if( transaction.getAmount() < 0 )
			throw new InvalidAmountException("Transfered amount cannot be negative");*/

		/*if( !transaction.isValid() )
			return false;*/

		//transaction.validate();

		TransferRequest request = new TransferRequest(transaction);

		byte[] result = processRequest((location) -> {			
			Response response = client.target(location).path(DistributedWallet.PATH + "/transfer/")
					.request().post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return (byte[]) response.readEntity(byte[].class);
			} else
				throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
		});

		BFTReply r = BFTReply.processReply(result);
		if( r != null ) {
			if( r.isException() ) {
				String msg = (String) r.getContent();
				switch(r.getResultType()) {
				case INVALID_ADDRESS:
					throw new InvalidAddressException(msg);
				case INVALID_AMOUNT:
					throw new InvalidAmountException(msg);
				case INVALID_SIGNATURE:
					throw new InvalidSignatureException(msg);
				case NOT_ENOUGH_MONEY:
					throw new NotEnoughMoneyException(msg);
				default:
					break;
				}
			} else {
				return (Boolean) r.getContent();
			}	
		}
		throw new RuntimeException("Replies are not valid!");
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		AtomicTransferRequest request = new AtomicTransferRequest(transactions);

		byte[] result = processRequest((location) -> {			
			Response response = client.target(location).path(DistributedWallet.PATH + "/atomicTransfer/")
					.request().post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return (byte[]) response.readEntity(byte[].class);
			} else
				throw new RuntimeException("WalletClient atomicTransfer: " + response.getStatus());
		});

		BFTReply r = BFTReply.processReply(result);
		if( r != null ) {
				if( r.isException() ) {
					String msg = (String) r.getContent();
					switch(r.getResultType()) {
					case INVALID_ADDRESS:
						throw new InvalidAddressException(msg);
					case INVALID_AMOUNT:
						throw new InvalidAmountException(msg);
					case INVALID_SIGNATURE:
						throw new InvalidSignatureException(msg);
					case NOT_ENOUGH_MONEY:
						throw new NotEnoughMoneyException(msg);
					default:
						break;
					}
				} else {
					return (Boolean) r.getContent();
				}	
			}
		throw new RuntimeException("Replies are not valid!");
	}

	@Override
	public double balance(String who) {

		BalanceRequest request = new BalanceRequest(who);

		byte[] reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/balance/")
					.request().post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				return (byte[]) response.readEntity(byte[].class);
			} else
				throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
		});

		BFTReply r = BFTReply.processReply(reply);
		if( r != null ) {
				return (Double) r.getContent();
		}
		throw new RuntimeException("Replies are not valid!");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> ledger() {

		byte[] reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/ledger/")
					.request().get();

			if (response.getStatus() == 200) {
				return (byte[]) response.readEntity(byte[].class);
			} else
				throw new RuntimeException("WalletClient ledger: " + response.getStatus());
		});

		BFTReply r = BFTReply.processReply(reply);
		if( r != null ) {
				return (Map<String, Double>) r.getContent();
		}
		throw new RuntimeException("Replies are not valid!");
	}

}
