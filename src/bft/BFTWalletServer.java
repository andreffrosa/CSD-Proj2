package bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import wallet.ByzantineWallet;
import wallet.SimpleWallet;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class BFTWalletServer extends DefaultRecoverable {

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

		if(byzantine)
			wallet = new ByzantineWallet();
		else
			wallet = new SimpleWallet();

		iterations = 0;

		logger = Logger.getLogger(BFTWalletServer.class.getName());

		results = new HashMap<>();

		System.out.println("publicKey: " + java.util.Base64.getEncoder().encodeToString(replica.getReplicaContext().getStaticConfiguration().getPublicKey(id).getEncoded()));
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Use: java WalletServer <processId>");
			System.exit(-1);
		}
		new BFTWalletServer(Integer.parseInt(args[0]));
	}

	private byte[] chechResults(String hash) {
		// verify if the operation was already executed
		Entry<Long, byte[]> e = results.get(hash);
		if(e != null) {
			return e.getValue();
		}

		return null;
	}

	private byte[] executeSingle(byte[] command, MessageContext msgCtxs) {
		byte[] reply = null;
		boolean hasReply = false;
		boolean cached = false;
		byte[] val = null;

		iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();
			switch (reqType) {
			case TRANSFER_MONEY:
				String from = objIn.readUTF();
				String to = objIn.readUTF();
				double amount = objIn.readDouble();
				String signature = objIn.readUTF();
				long nonce = objIn.readLong();

				Transaction t = new Transaction(from, to, amount, signature, true, nonce);

				val = chechResults(t.getDigestString());
				cached = (val != null);
				if(!cached) {
					boolean result = false;

					try {
						result = wallet.transfer(t);

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeObject(new Boolean(result));
						//objOut.writeBoolean(result);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeObject(e.getMessage());
						//objOut.writeUTF(e.getMessage());
					} catch (InvalidAmountException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_AMOUNT);
						objOut.writeObject(e.getMessage());
						//objOut.writeUTF(e.getMessage());
					} catch (InvalidSignatureException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_SIGNATURE);
						objOut.writeObject(e.getMessage());
						//objOut.writeUTF(e.getMessage());
					} catch (NotEnoughMoneyException e) {
						objOut.writeObject(BFTWalletResultType.NOT_ENOUGH_MONEY);
						objOut.writeObject(e.getMessage());
						//objOut.writeUTF(e.getMessage());
					}

					hasReply = true;

					System.out.println("(" + iterations + ") transfer(" + from + ", " + to + ", " + amount + ", " + nonce + ", " + signature + ") : " + result);
				} 

				break;
			case ATOMIC_TRANSFER_MONEY:
				@SuppressWarnings("unchecked") List<Transaction> transactions = (List<Transaction>) objIn.readObject();

				String hash = "";
				for(Transaction tx : transactions) {
					hash += tx.getDigestString();
				}

				val = chechResults(hash);
				cached = (val != null);
				if(!cached) {

					boolean result = false;
					try {
						result = wallet.atomicTransfer(transactions);

						objOut.writeObject(BFTWalletResultType.OK);
						objOut.writeBoolean(result);
					} catch (InvalidAddressException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_ADDRESS);
						objOut.writeUTF(e.getMessage());
					} catch (InvalidAmountException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_AMOUNT);
						objOut.writeUTF(e.getMessage());
					} catch (InvalidSignatureException e) {
						objOut.writeObject(BFTWalletResultType.INVALID_SIGNATURE);
						objOut.writeUTF(e.getMessage());
					} catch (NotEnoughMoneyException e) {
						objOut.writeObject(BFTWalletResultType.NOT_ENOUGH_MONEY);
						objOut.writeUTF(e.getMessage());
					}

					hasReply = true;

					System.out.println("(" + iterations + ") atomicTransfer(...) : " + result);
				}

				break;
			case CURRENT_BALANCE:
				String who = objIn.readUTF();

				double balance = wallet.balance(who);

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeDouble(balance);
				hasReply = true;

				System.out.println("(" + iterations + ") balance(" + who + ") : " + balance);

				break;
			case GET_LEDGER:
				Map<String, Double> ledger = wallet.ledger();

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ledger);
				hasReply = true;

				System.out.println("(" + iterations + ") ledger() : " + ledger.size());

				break;
			}

			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
			} else if(cached) {
				reply = val;
			} else {
				reply = new byte[0];
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

		// iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();

			switch (reqType) {
			case CURRENT_BALANCE:
				String who = objIn.readUTF();

				double balance = wallet.balance(who);

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeDouble(balance);
				hasReply = true;

				System.out.println("(" + iterations + ") currentAmount(" + who + ") : " + balance);

				break;
			case GET_LEDGER:
				Map<String, Double> ledger = wallet.ledger();

				objOut.writeObject(BFTWalletResultType.OK);
				objOut.writeObject(ledger);
				hasReply = true;

				System.out.println("(" + iterations + ") ledger() : " + ledger.size());

				break;
			default:
				logger.log(Level.WARNING, "in appExecuteUnordered only read operations are supported");
			}
			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
			} else {
				reply = new byte[0];
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
