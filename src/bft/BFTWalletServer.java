package bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import wallet.SimpleWallet;
import wallet.Wallet;

public class BFTWalletServer extends DefaultRecoverable {

	private Wallet wallet;
	private int iterations;
	@SuppressWarnings("unused")
	private ServiceReplica replica;
	private Logger logger;

	public BFTWalletServer(int id) {
		replica = new ServiceReplica(id, this, this);
		wallet = new SimpleWallet(); // aqui?

		iterations = 0;

		logger = Logger.getLogger(BFTWalletServer.class.getName());
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Use: java WalletServer <processId>");
			System.exit(-1);
		}
		new BFTWalletServer(Integer.parseInt(args[0]));
	}

	private byte[] executeSingle(byte[] command, MessageContext msgCtxs) {
		byte[] reply = null;
		boolean hasReply = false;
		String who, from, to;
		int amount;

		iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();
			switch (reqType) {
			case CREATE_MONEY:
				who = objIn.readUTF();
				amount = objIn.readInt();

				int balance = wallet.createMoney(who, amount);

				objOut.writeInt(balance);
				hasReply = true;

				System.out.println("(" + iterations + ") createmoney(" + who + ", " + amount + ") : " + balance);

				// reply = new Integer() // return null
				break;
			case TRANSFER_MONEY:
				from = objIn.readUTF();
				to = objIn.readUTF();
				amount = objIn.readInt();

				boolean result = wallet.transfer(from, to, amount);

				objOut.writeBoolean(result);
				hasReply = true;

				System.out
				.println("(" + iterations + ") transfer(" + from + ", " + to + ", " + amount + ") : " + result);

				// reply = new byte[]{(byte) (result?1:0)};
				break;
			case CURRENT_AMOUNT:
				who = objIn.readUTF();

				balance = wallet.currentAmount(who);

				objOut.writeInt(balance);
				hasReply = true;

				System.out.println("(" + iterations + ") currentAmount(" + who + ") : " + balance);

				break;
			}

			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
				/*objOut.write(TOMUtil.signMessage(replica.getReplicaContext().getStaticConfiguration().getPrivateKey(), reply));
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();*/
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
		String who;
		int amount;

		// iterations++;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
			BFTWalletRequestType reqType = (BFTWalletRequestType) objIn.readObject();

			switch (reqType) {
			case CURRENT_AMOUNT:
				who = objIn.readUTF();
				amount = wallet.currentAmount(who);
				hasReply = true;

				System.out.println("(" + iterations + ") currentAmount(" + who + ") : " + amount);

				objOut.writeInt(amount);
				break;
			default:
				logger.log(Level.WARNING, "in appExecuteUnordered only read operations are supported");
			}
			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
				/*objOut.write(TOMUtil.signMessage(replica.getReplicaContext().getStaticConfiguration().getPrivateKey(), reply));
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();*/
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
			//if (msgCtxs != null && msgCtxs[i] != null) {
				replies[i] = executeSingle(commands[i], msgCtxs[i]);
			//} else
				//replies[i] = executeSingle(commands[i], null);
		}

		return replies;
	}

}
