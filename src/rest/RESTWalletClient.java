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

import bft.reply.BFTReply;
import bft.reply.InvalidRepliesException;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.LedgerRequest;
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
	private final int CONNECT_TIMEOUT = 35000;
	private final int READ_TIMEOUT = 30000;

	// Private Variables
	private Client client;
	private Logger logger;
	private String[] servers;

	public RESTWalletClient(String[] servers) {
		this.servers = servers;

		System.setProperty("javax.net.ssl.keyStore", CLIENT_KEYSTORE);
		System.setProperty("javax.net.ssl.keyStorePassword", CLIENT_KEYSTORE_PWD);
		System.setProperty("javax.net.ssl.trustStore", CLIENT_TRUSTSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", CLIENT_TRUSTSTORE_PWD);

		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		client = ClientBuilder.newBuilder().hostnameVerifier((String hostname, SSLSession cts) -> true)
				.withConfig(config).build();

		logger = Logger.getLogger(RESTWalletClient.class.getName());
	}

	private <T> T processRequest(RequestHandler<T> requestHandler) {

		List<String> replicas = new ArrayList<>(Arrays.asList(this.servers));
		int index = -1;

		for (int current_try = 0; current_try < MAX_TRIES; current_try++) {

			if (replicas.isEmpty()) {
				// logger.log(Level.WARNING, String.format("Aborted request! All the servers
				// didn't respond..."));
				// return null;
				throw new RuntimeException("Aborted request! All the servers didn't respond...");
			}

			index = (int) Math.floor(Math.random() * replicas.size());

			try {
				return requestHandler.execute(replicas.get(index));
			} catch (ProcessingException | InvalidRepliesException e) {
				if (e.getMessage().contains("java.net.ConnectException")
						|| e.getMessage().contains("java.net.SocketTimeoutException")
						|| e instanceof InvalidRepliesException) {
					logger.log(Level.INFO, String.format("Error contacting server %s .... retry: %d",
							replicas.get(index), current_try));
					replicas.remove(index);
				} else {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
		}

		throw new RuntimeException("Aborted request! Too many tries...");
		// logger.log(Level.WARNING, String.format("Aborted request! Too many
		// tries..."));
		// return null;
	}

	@Override
	public boolean transfer(Transaction transaction)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		/*
		 * if( transaction.getAmount() < 0 ) throw new
		 * InvalidAmountException("Transfered amount cannot be negative");
		 */

		/*
		 * if( !transaction.isValid() ) return false;
		 */

		// transaction.validate();

		TransferRequest request = new TransferRequest(transaction);

		String op_hash = OperationsHashUtil.transferHash(transaction.getFrom(), transaction.getTo(),
				transaction.getAmount(), transaction.getSignature(), request.getNonce());

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/transfer/").request()
					.post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, op_hash);
			} else
				throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
		});

		return (Boolean) reply.getResult();
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		AtomicTransferRequest request = new AtomicTransferRequest(transactions);

		String op_hash = OperationsHashUtil.atomicTransferHash(transactions, request.getNonce());

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/atomicTransfer/").request()
					.post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, op_hash);
			} else
				throw new RuntimeException("WalletClient atomicTransfer: " + response.getStatus());
		});

		return (Boolean) reply.getResult();
	}

	@Override
	public double balance(String who) {

		BalanceRequest request = new BalanceRequest(who);

		String op_hash = OperationsHashUtil.balanceHash(who, request.getNonce());

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/balance/").request()
					.post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, op_hash);
			} else
				throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
		});

		return (Double) reply.getContent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> ledger() {

		LedgerRequest request = new LedgerRequest();

		String op_hash = OperationsHashUtil.ledgerHash(request.getNonce());

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + "/ledger/").request()
					.post(Entity.entity(request, MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, op_hash);
			} else
				throw new RuntimeException("WalletClient ledger: " + response.getStatus());
		});

		return (Map<String, Double>) reply.getContent();
	}

}
