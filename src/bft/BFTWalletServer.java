package bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
import utils.Serializor;
import wallet.ByzantineWallet;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.GetBetweenOP;
import wallet.SimpleWallet;
import wallet.Transaction;
import wallet.UpdOp;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
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
				cached = transfer(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case ATOMIC_TRANSFER_MONEY:
				cached = atomicTransfer(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case CURRENT_BALANCE:
				cached = balance(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET_LEDGER:
				cached = balance(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case CREATE:
				cached = create(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET:
				cached = get(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET_BETWEEN:
				cached = getBetween(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case SET:
				cached = set(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case SUM:
				cached = sum(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case COMPARE:
				cached = compare(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case COND_UPD:
				cached = cond_upd(objIn, nonce, byteOut, objOut);
				hasReply = true;
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
				cached = balance(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET_LEDGER:
				cached = ledger(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET:
				cached = get(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case GET_BETWEEN:
				cached = getBetween(objIn, nonce, byteOut, objOut);
				hasReply = true;
				break;
			case COMPARE:
				cached = compare(objIn, nonce, byteOut, objOut);
				hasReply = true;
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

	private boolean transfer(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		String from = objIn.readUTF();
		String to = objIn.readUTF();
		double amount = objIn.readDouble();
		String signature = objIn.readUTF();

		String op_hash = TransferRequest.computeHash(signature, nonce);

		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);
		if (!cached) {
			String result = "";
			try {
				objOut.writeObject(op_hash);
				boolean status = wallet.transfer(new Transaction(from, to, amount, signature, true));

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(new Boolean(status));

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

			System.out.println("(" + iterations + ") transfer(" + from + ", " + to + ", " + amount + ", "
					+ nonce + ", " + signature + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean atomicTransfer(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		@SuppressWarnings("unchecked")
		List<Transaction> transactions = (List<Transaction>) objIn.readObject();

		String op_hash = AtomicTransferRequest.computeHash(transactions, nonce);

		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);
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

			System.out.println("(" + iterations + ") atomicTransfer(...) : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean balance(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {
		String who = objIn.readUTF();

		String op_hash = BalanceRequest.computeHash(who, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);
		if (!cached) {
			double balance = wallet.balance(who);

			objOut.writeObject(op_hash);
			objOut.writeObject(BFTWalletResultType.OK);
			objOut.writeObject(balance);

			System.out.println("(" + iterations + ") balance(" + who + ") : " + "OK ->" + balance);

			return false;
		} else {
			return true;
		}
	}

	private boolean ledger(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {
		String op_hash = LedgerRequest.computeHash(nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);
		if (!cached) {
			Map<String, Double> ledger = wallet.ledger();

			objOut.writeObject(op_hash);
			objOut.writeObject(BFTWalletResultType.OK);
			objOut.writeObject(ledger);

			System.out.println("(" + iterations + ") ledger() : " + "OK ->" + ledger.size());
			return false;
		} else {
			return true;
		}
	}

	private boolean create(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException {
		DataType type = DataType.valueOf(objIn.readUTF());
		String id = objIn.readUTF();
		String initial_value = objIn.readUTF();

		String op_hash = CreateRequest.computeHash(type, id, initial_value, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);
			boolean ret = wallet.create(type, id, initial_value);

			String result = "OK -> " + ret;

			objOut.writeObject(BFTWalletResultType.OK);
			objOut.writeObject(ret);

			System.out.println("(" + iterations + ") create(" + type + ", " + id + ", " + initial_value + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean get(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException {

		DataType type = DataType.valueOf(objIn.readUTF());
		String id = objIn.readUTF();

		String op_hash = GetRequest.computeHash(type, id, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);
			String result;
			try {
				String ret = wallet.get(type, id);

				result = "OK -> " + ret;

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ret);
			} catch(InvalidAddressException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
			} catch(InvalidTypeException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
			}

			System.out.println("(" + iterations + ") get(" + type + ", " + id + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean getBetween(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		String json = (String) objIn.readObject();
		List<GetBetweenOP> ops = Serializor.deserializeList(json, GetBetweenOP.class);

		String op_hash = GetBetweenRequest.computeHash(ops, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);

			List<String> ret = wallet.getBetween(ops);

			String result = "OK -> " + ret.size();

			objOut.writeObject(BFTWalletResultType.OK);
			objOut.writeObject(ret);

			System.out.println("(" + iterations + ") getBetween(" + ops.size() + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean set(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		DataType type = DataType.valueOf(objIn.readUTF());
		String id = objIn.readUTF();
		String value = objIn.readUTF();

		String op_hash = SetRequest.computeHash(type, id, value, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);

			String result;
			try {
				Boolean ret = wallet.set(type, id, value);

				result = "OK -> " + ret;

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ret);
			} catch (InvalidTypeException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
			} catch (InvalidAddressException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
			}

			System.out.println("(" + iterations + ") set(" + type + ", " + id + ", " + value + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean sum(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {
		DataType type = DataType.valueOf(objIn.readUTF());
		String id = objIn.readUTF();
		String amount = objIn.readUTF();
		String arg = objIn.readUTF();

		String op_hash = SumRequest.computeHash(type, id, amount, arg, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);

			String result;
			try {
				String ret = wallet.sum(type, id, amount, arg);

				result = "OK -> " + ret;

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ret);
			} catch (InvalidTypeException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
			} catch (InvalidAddressException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
			}

			System.out.println("(" + iterations + ") set(" + type + ", " + id + ", " + amount + ", " + arg + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean compare(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		DataType cond_type = DataType.valueOf(objIn.readUTF());
		String cond_id = objIn.readUTF();
		ConditionalOperation cond = ConditionalOperation.valueOf(objIn.readUTF());
		String cond_val = objIn.readUTF();
		String cipheredKey = objIn.readUTF();

		String op_hash = CompareRequest.computeHash(cond_type, cond_id, cond, cond_val, cipheredKey, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);

			String result;
			try {
				boolean ret = wallet.compare(cond_type, cond_id, cond, cond_val, cipheredKey);

				result = "OK -> " + ret;

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ret);
			} catch (InvalidTypeException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
			} catch (InvalidAddressException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
			} catch (InvalidOperationException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_OPERATION);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_OPERATION;
			}

			System.out.println("(" + iterations + ") compare(" + cond_type + ", " + cond_id + ", " + cond + ", " + cond_val + ", " + cipheredKey + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

	private boolean cond_upd(ObjectInput objIn, long nonce, ByteArrayOutputStream byteOut, ObjectOutput objOut) throws IOException, ClassNotFoundException {

		DataType cond_type = DataType.valueOf(objIn.readUTF());
		String cond_id = objIn.readUTF();
		ConditionalOperation cond = ConditionalOperation.valueOf(objIn.readUTF());
		String cond_val = objIn.readUTF();
		String cipheredKey = objIn.readUTF();
		
		String json = objIn.readUTF();
		List<UpdOp> ops = Serializor.deserializeList(json, UpdOp.class);

		String op_hash = CondUpdRequest.computeHash(cond_type, cond_id, cond, cond_val, cipheredKey, ops, nonce);
		byte[] val = chechResults(op_hash);
		boolean cached = (val != null);

		if (!cached) {
			objOut.writeObject(op_hash);

			String result;
			try {
				boolean ret = wallet.cond_upd(cond_type, cond_id, cond, cond_val, cipheredKey, ops);

				result = "OK -> " + ret;

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ret);
			} catch (InvalidTypeException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_TYPE);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_TYPE;
			} catch (InvalidAddressException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_ADDRESS;
			} catch (InvalidOperationException e) {
				objOut.writeObject(BFTWalletResultType.INVALID_OPERATION);
				objOut.writeObject(e.getMessage());

				result = "EXCEPTION -> " + BFTWalletResultType.INVALID_OPERATION;
			}

			System.out.println("(" + iterations + ") cond_upd(" + cond_type + ", " + cond_id + ", " + cond + ", " + cond_val + ", " + cipheredKey + ") : " + result);

			return false;
		} else {
			return true;
		}
	}

}
