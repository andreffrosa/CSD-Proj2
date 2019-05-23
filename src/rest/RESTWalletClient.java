package rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import bft.reply.BFTReply;
import bft.reply.InvalidRepliesException;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.CompareRequest;
import rest.entities.CondUpdRequest;
import rest.entities.CreateRequest;
import rest.entities.GetBetweenRequest;
import rest.entities.GetRequest;
import rest.entities.LedgerRequest;
import rest.entities.SetRequest;
import rest.entities.SumRequest;
import rest.entities.TransferRequest;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.GetBetweenOP;
import wallet.Transaction;
import wallet.UpdOp;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
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
				e.printStackTrace();

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

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.TRANSFER_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
		});

		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
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
		}

		return (Boolean) reply.getContent();
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		AtomicTransferRequest request = new AtomicTransferRequest(transactions);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location)
					.path(DistributedWallet.PATH + DistributedWallet.ATOMIC_TRANSFER_PATH).request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient atomicTransfer: " + response.getStatus());
		});

		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
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
		}

		return (Boolean) reply.getContent();
	}

	@Override
	public double balance(String who) {

		BalanceRequest request = new BalanceRequest(who);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.BALANCE_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
		});

		return (Double) reply.getContent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> ledger() {

		LedgerRequest request = new LedgerRequest();

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.LEDGER_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient ledger: " + response.getStatus());
		});

		return (Map<String, Double>) reply.getContent();
	}

	@Override
	public boolean create(DataType type, String id, String initial_value) {
		
		CreateRequest request = new CreateRequest(type, id, initial_value);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.CREATE_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient create: " + response.getStatus());
		});

		return (Boolean) reply.getContent();
	}

	@Override
	public String get(DataType type, String id) throws InvalidTypeException, InvalidAddressException {
		GetRequest request = new GetRequest(type, id);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.GET_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient get: " + response.getStatus());
		});

		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
			case INVALID_ADDRESS:
				throw new InvalidAddressException(msg);
			case INVALID_TYPE:
				throw new InvalidTypeException(msg);
			default:
				break;
			}
		}

		return (String) reply.getContent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getBetween(List<GetBetweenOP> ops) {
		GetBetweenRequest request = new GetBetweenRequest(ops);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.GET_BETWEEN_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient getBetween: " + response.getStatus());
		});

		return (List<String>) reply.getContent();
	}

	@Override
	public boolean set(DataType type, String id, String value) throws InvalidTypeException, InvalidAddressException {
		SetRequest request = new SetRequest(type, id, value);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.SET_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient set: " + response.getStatus());
		});
		
		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
			case INVALID_ADDRESS:
				throw new InvalidAddressException(msg);
			case INVALID_TYPE:
				throw new InvalidTypeException(msg);
			default:
				break;
			}
		}

		return (Boolean) reply.getContent();
	}

	@Override
	public String sum(DataType key_type, String key, String amount, String arg) throws InvalidAddressException, InvalidTypeException {
		
		SumRequest request = new SumRequest(key_type, id, amount, arg);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.SUM_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient sum: " + response.getStatus());
		});
		
		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
			case INVALID_ADDRESS:
				throw new InvalidAddressException(msg);
			case INVALID_TYPE:
				throw new InvalidTypeException(msg);
			default:
				break;
			}
		}

		return (String) reply.getContent();
	}

	@Override
	public boolean compare(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val,
			String cipheredKey) throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		
		CompareRequest request = new CompareRequest(cond_type, cond_id, cond, cond_val, cipheredKey);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.COMPARE_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient compare: " + response.getStatus());
		});
		
		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
			case INVALID_ADDRESS:
				throw new InvalidAddressException(msg);
			case INVALID_TYPE:
				throw new InvalidTypeException(msg);
			case INVALID_OPERATION:
				throw new InvalidOperationException(msg);
			default:
				break;
			}
		}

		return (Boolean) reply.getContent();
	}

	@Override
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val,
			String cond_cipheredKey, List<UpdOp> ops)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		
		CondUpdRequest request = new CondUpdRequest(cond_type, cond_id, cond, cond_val, cond_cipheredKey, ops);

		BFTReply reply = processRequest((location) -> {
			Response response = client.target(location).path(DistributedWallet.PATH + DistributedWallet.COND_UPD_PATH)
					.request()
					.post(Entity.entity(new GsonBuilder().create().toJson(request), MediaType.APPLICATION_JSON));

			if (response.getStatus() == 200) {
				byte[] result = (byte[]) response.readEntity(byte[].class);
				return BFTReply.processReply(result, request.getHash());
			} else
				throw new RuntimeException("WalletClient cond_upd: " + response.getStatus());
		});
		
		if (reply.isException()) {
			String msg = (String) reply.getContent();

			switch (reply.getResultType()) {
			case INVALID_ADDRESS:
				throw new InvalidAddressException(msg);
			case INVALID_TYPE:
				throw new InvalidTypeException(msg);
			case INVALID_OPERATION:
				throw new InvalidOperationException(msg);
			default:
				break;
			}
		}

		return (Boolean) reply.getContent();
	}

}
