package bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import bft.reply.BFTWalletResultType;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import rest.entities.AddRequest;
import rest.entities.AddSumRequest;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.CondAddRequest;
import rest.entities.CondSetRequest;
import rest.entities.GetBetweenOrderPreservingRequest;
import rest.entities.GetOrderPreservingRequest;
import rest.entities.GetSumRequest;
import rest.entities.LedgerRequest;
import rest.entities.PutOrderPreservingRequest;
import rest.entities.PutSumRequest;
import rest.entities.TransferRequest;
import utils.Serializor;
import wallet.ByzantineWallet;
import wallet.SimpleWallet;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public class BFTWalletServer extends DefaultRecoverable {

	private static final long CACHED_TIME = 2 * 60 * 1000 * 1000 * 1000; // 2 minutos
	private static final double GC_PROB = 0.3;

	private Wallet wallet;
	private int iterations;
	private ServiceReplica replica;
	private Logger logger;

	private Map<String, Entry<Long, byte[]>> results;

	public BFTWalletServer(int id) {
		this(id, false);
	}

	public BFTWalletServer(int id, boolean byzantine) {
		replica = new ServiceReplica(id, this, this);

		if (byzantine)
			wallet = new ByzantineWallet();
		else
			wallet = new SimpleWallet();

		iterations = 0;

		logger = Logger.getLogger(BFTWalletServer.class.getName());

		results = new HashMap<>();

		System.out.println("publicKey: " + java.util.Base64.getEncoder()
				.encodeToString(replica.getReplicaContext().getStaticConfiguration().getPublicKey(id).getEncoded()));
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Use: java WalletServer <processId>");
			System.exit(-1);
		}
		new BFTWalletServer(Integer.parseInt(args[0]));
	}

	private void garbageCollector() {
		long now = System.nanoTime();
		for (Entry<String, Entry<Long, byte[]>> e : new HashMap<>(results).entrySet()) {
			long timestamp = e.getValue().getKey();
			if (timestamp + CACHED_TIME <= now) {
				results.remove(e.getKey());
			}
		}
	}

	private byte[] chechResults(String hash) {

		// Garbage Collector
		if (Math.random() <= GC_PROB) {
			garbageCollector();
		}

		// verify if the operation was already executed
		Entry<Long, byte[]> e = results.get(hash);
		if (e != null) {
			return e.getValue();
		}

		return null;
	}

	private byte[] executeSingle(byte[] command, MessageContext msgCtxs) {
		byte[] reply = null;
		boolean hasReply = false;
		boolean cached = false;
		byte[] val = null;
		String op_hash = null;

		iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();
			long nonce = objIn.readLong();

			switch (reqType) {
			case TRANSFER_MONEY:
				String from = objIn.readUTF();
				String to = objIn.readUTF();
				double amount = objIn.readDouble();
				String signature = objIn.readUTF();

				op_hash = TransferRequest.computeHash(signature, nonce);

				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {
					String result = "";

					try {
						objOut.writeObject(op_hash);
						boolean status = wallet.transfer(new Transaction(from, to, amount, signature, true));

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(new Boolean(status));
						result = "OK -> " + status;
						// objOut.writeBoolean(result);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());
						// objOut.writeUTF(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidAmountException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_AMOUNT);
						objOut.writeObject(e.getMessage());
						// objOut.writeUTF(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_AMOUNT;
					} catch (InvalidSignatureException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_SIGNATURE);
						objOut.writeObject(e.getMessage());
						// objOut.writeUTF(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_SIGNATURE;
					} catch (NotEnoughMoneyException e) {
						objOut.writeObject(BFTWalletResultType.NOT_ENOUGH_MONEY);
						objOut.writeObject(e.getMessage());
						// objOut.writeUTF(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.NOT_ENOUGH_MONEY;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") transfer(" + from + ", " + to + ", " + amount + ", "
							+ nonce + ", " + signature + ") : " + result);
				}

				break;
			case ATOMIC_TRANSFER_MONEY:
				@SuppressWarnings("unchecked")
				List<Transaction> transactions = (List<Transaction>) objIn.readObject();

				op_hash = AtomicTransferRequest.computeHash(transactions, nonce);

				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						boolean status = wallet.atomicTransfer(transactions);

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(status);
						result = "OK -> " + status;
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidAmountException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_AMOUNT);
						objOut.writeObject(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_AMOUNT;
					} catch (InvalidSignatureException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_SIGNATURE);
						objOut.writeObject(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_SIGNATURE;
					} catch (NotEnoughMoneyException e) {
						objOut.writeObject(BFTWalletResultType.NOT_ENOUGH_MONEY);
						objOut.writeObject(e.getMessage());
						result = "EXCEPTION -> " + BFTWalletResultType.NOT_ENOUGH_MONEY;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") atomicTransfer(...) : " + result);
				}

				break;
			case CURRENT_BALANCE:
				String who = objIn.readUTF();

				op_hash = BalanceRequest.computeHash(who, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					double balance = wallet.balance(who);

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(balance);
					hasReply = true;

					System.out.println("(" + iterations + ") balance(" + who + ") : " + "OK ->" + balance);
				}

				break;
			case GET_LEDGER:
				op_hash = LedgerRequest.computeHash(nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {
					Map<String, Double> ledger = wallet.ledger();

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(ledger);
					hasReply = true;

					System.out.println("(" + iterations + ") ledger() : " + "OK ->" + ledger.size());
				}

				break;
			case PUT_OPI:
				String id = objIn.readUTF();
				Long value = objIn.readLong();

				op_hash = PutOrderPreservingRequest.computeHash(id, value, nonce);

				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					Boolean result = wallet.putOrderPreservingInt(id, value);

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(result);
					hasReply = true;

					System.out.println("(" + iterations + ") putOPI(" + id + ", " + value + ") : " + "OK ->" + result);
				}
				break;
			case GET_OPI:
				id = objIn.readUTF();

				op_hash = GetOrderPreservingRequest.computeHash(id, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						value = wallet.getOrderPreservingInt(id);

						result = "OK -> " + value;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(value);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getOPI(" + id + ") : " + result);
				}
				break;
			case GET_BETWEEN_OPI:
				String k1 = objIn.readUTF();
				String k2 = objIn.readUTF();

				op_hash = GetBetweenOrderPreservingRequest.computeHash(k1, k2, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						List<Entry<String, Long>> values = wallet.getBetween(k1, k2);

						result = "OK -> " + values.size();

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(Serializor.serializeList(values));
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getBetweenOPI(" + k1 + ", " + k2 + ") : " + result);
				}
				break;
			case PUT_SUM:
				id = objIn.readUTF();
				BigInteger bigI = (BigInteger) objIn.readObject();

				op_hash = PutSumRequest.computeHash(id, bigI, nonce);

				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					Boolean result = wallet.putSumInt(id, bigI);

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(result);
					hasReply = true;

					System.out.println("(" + iterations + ") putSum(" + id + ", " + bigI + ") : " + "OK -> " + result);
				}
				break;
			case GET_SUM:
				id = objIn.readUTF();

				op_hash = GetSumRequest.computeHash(id, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						BigInteger ret = wallet.getSumInt(id);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getSum(" + id + ") : " + result);
				}
				break;
			case ADD_SUM:
				id = objIn.readUTF();
				BigInteger big_amount = (BigInteger) objIn.readObject();
				BigInteger nSquare = (BigInteger) objIn.readObject();

				op_hash = AddSumRequest.computeHash(id, big_amount, nSquare, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						BigInteger ret = wallet.add_sumInt(id, big_amount, nSquare);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println(
							"(" + iterations + ") add(" + id + ", " + big_amount + ", " + nSquare + ") : " + result);
				}
				break;
			case DIF:
				id = objIn.readUTF();
				big_amount = (BigInteger) objIn.readObject();
				nSquare = (BigInteger) objIn.readObject();

				op_hash = AddSumRequest.computeHash(id, big_amount, nSquare, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						BigInteger ret = wallet.sub(id, big_amount, nSquare);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println(
							"(" + iterations + ") sub(" + id + ", " + big_amount + ", " + nSquare + ") : " + result);
				}
				break;
			case COND_SET:
				String cond_key = objIn.readUTF();
				String cond_key_type = objIn.readUTF();
				String cond_val = objIn.readUTF();
				String cond_cipheredKey = objIn.readUTF();
				String upd_key = objIn.readUTF();
				String upd_key_type = objIn.readUTF();
				String upd_val = objIn.readUTF();

				op_hash = CondSetRequest.computeHash(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key,
						upd_key_type, upd_val, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						Boolean ret = wallet.cond_set(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key,
								upd_key_type, upd_val);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidTypeException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") cond_set(" + cond_key_type + " " + cond_key + " >= "
							+ cond_val + " (" + cond_cipheredKey + ")" + " then " + upd_key_type + " " + upd_key + " = "
							+ upd_val + ") : " + result);
				}
				break;
			case COND_ADD:
				cond_key = objIn.readUTF();
				cond_key_type = objIn.readUTF();
				cond_val = objIn.readUTF();
				cond_cipheredKey = objIn.readUTF();
				upd_key = objIn.readUTF();
				upd_key_type = objIn.readUTF();
				upd_val = objIn.readUTF();
				String upd_auxArg = objIn.readUTF();

				op_hash = CondAddRequest.computeHash(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key,
						upd_key_type, upd_val, upd_auxArg, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						Boolean ret = wallet.cond_add(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key,
								upd_key_type, upd_val, upd_auxArg);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidTypeException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") cond_add(" + cond_key_type + " " + cond_key + " >= "
							+ cond_val + " (" + cond_cipheredKey + ")" + " then " + upd_key_type + " " + upd_key + " = "
							+ upd_val + " (" + upd_auxArg + ") " + ") : " + result);
				}
				break;
			case ADD:
				String key = objIn.readUTF();
				String key_type = objIn.readUTF();
				String add_value = objIn.readUTF();
				String auxArg = objIn.readUTF();

				op_hash = AddRequest.computeHash(key, key_type, add_value, auxArg, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						String ret = wallet.add(key, key_type, add_value, auxArg);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidTypeException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") add(" + key_type + ", " + key + ", " + val + ", " + auxArg + ") : " + result);
				}
				break;
			case COMPARE:
				key = objIn.readUTF();
				key_type = objIn.readUTF();
				add_value = objIn.readUTF();
				String cipheredKey = objIn.readUTF();

				op_hash = AddRequest.computeHash(key, key_type, add_value, cipheredKey, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						int ret = wallet.compare(key, key_type, add_value, cipheredKey);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidTypeException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") add(" + key_type + ", " + key + ", " + val + ", " + cipheredKey + ") : " + result);
				}
				break;

			}

			if (cached) {
				reply = val;
			} else {
				if (hasReply) {
					objOut.flush();
					byteOut.flush();
					reply = byteOut.toByteArray();
				} else {
					reply = new byte[0];
				}

				results.put(op_hash, new AbstractMap.SimpleEntry<>(System.nanoTime(), reply));
			}

		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Ocurred during wallet operation execution", e);
		}

		return reply;
	}

	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {

		byte[] reply = null;
		boolean hasReply = false;
		byte[] val = null;
		boolean cached = false;
		String op_hash = null;

		// iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();
			long nonce = objIn.readLong();

			switch (reqType) {
			case CURRENT_BALANCE:
				String who = objIn.readUTF();

				op_hash = BalanceRequest.computeHash(who, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					double balance = wallet.balance(who);

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(balance);
					hasReply = true;

					System.out.println("(" + iterations + ") balance(" + who + ") : " + "OK ->" + balance);
				}

				break;
			case GET_LEDGER:
				op_hash = LedgerRequest.computeHash(nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {
					Map<String, Double> ledger = wallet.ledger();

					objOut.writeObject(op_hash);
					objOut.writeObject(BFTWalletResultType.OK);
					objOut.writeObject(ledger);
					hasReply = true;

					System.out.println("(" + iterations + ") ledger() : " + "OK ->" + ledger.size());
				}

				break;
			case GET_OPI:
				String id = objIn.readUTF();

				op_hash = GetOrderPreservingRequest.computeHash(id, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						long value = wallet.getOrderPreservingInt(id);

						result = "OK -> " + value;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(value);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getOPI(" + id + ") : " + result);
				}
				break;
			case GET_BETWEEN_OPI:
				String k1 = objIn.readUTF();
				String k2 = objIn.readUTF();

				op_hash = GetBetweenOrderPreservingRequest.computeHash(k1, k2, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						List<Entry<String, Long>> values = wallet.getBetween(k1, k2);

						result = "OK -> " + values.size();

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(Serializor.serializeList(values));
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getBetweenOPI(" + k1 + ", " + k2 + ") : " + result);
				}
				break;
			case GET_SUM:
				id = objIn.readUTF();

				op_hash = GetSumRequest.computeHash(id, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						BigInteger ret = wallet.getSumInt(id);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") getSum(" + id + ") : " + result);
				}
				break;
			case COMPARE:
				String key = objIn.readUTF();
				String key_type = objIn.readUTF();
				String add_value = objIn.readUTF();
				String cipheredKey = objIn.readUTF();

				op_hash = AddRequest.computeHash(key, key_type, add_value, cipheredKey, nonce);
				val = chechResults(op_hash);
				cached = (val != null);
				if (!cached) {

					String result = "";
					try {
						objOut.writeObject(op_hash);
						int ret = wallet.compare(key, key_type, add_value, cipheredKey);

						result = "OK -> " + ret;

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(ret);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
					} catch (InvalidTypeException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
						objOut.writeObject(e.getMessage());

						result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
					}

					hasReply = true;

					System.out.println("(" + iterations + ") add(" + key_type + ", " + key + ", " + val + ", " + cipheredKey + ") : " + result);
				}
				break;

			default:
				logger.log(Level.WARNING, "in appExecuteUnordered only read operations are supported");
			}

			if (cached) {
				reply = val;
			} else {
				if (hasReply) {
					objOut.flush();
					byteOut.flush();
					reply = byteOut.toByteArray();
				} else {
					reply = new byte[0];
				}

				results.put(op_hash, new AbstractMap.SimpleEntry<>(System.nanoTime(), reply));
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Ocurred during wallet operation execution", e);
		}

		return reply;
	}

	@Override
	public byte[] getSnapshot() {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
			objOut.writeObject(wallet);
			return byteOut.toByteArray();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while taking snapshot", e);
		}
		return new byte[0];
	}

	@Override
	public void installSnapshot(byte[] state) {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			wallet = (SimpleWallet) objIn.readObject();
		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Error while installing snapshot", e);
		}
	}

	@Override
	public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
		byte[][] replies = new byte[commands.length][];
		for (int i = 0; i < commands.length; i++) {
			replies[i] = executeSingle(commands[i], msgCtxs[i]);
		}

		return replies;
	}

}
