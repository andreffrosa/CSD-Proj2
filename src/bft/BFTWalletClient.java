package bft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import bft.BFTWalletRequestType;
import bftsmart.tom.ServiceProxy;

public class BFTWalletClient implements wallet.Wallet {

	ServiceProxy serviceProxy;

	public BFTWalletClient(int clientId) {
		serviceProxy = new ServiceProxy(clientId);
	}

	public void close() {
		serviceProxy.close();
	}

	@Override
	public int createMoney(String who, int amount) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CREATE_MONEY);
			objOut.writeUTF(who);
			objOut.writeInt(amount);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				throw new RuntimeException("Empty response"); 
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
						ObjectInput objIn = new ObjectInputStream(byteIn)) {
					return objIn.readInt();
				}
		} catch (IOException e) {
			System.out.println("Exception creating money: " + e.getMessage());
		}
		throw new RuntimeException("Empty response"); 
	}

	@Override
	public boolean transfer(String from, String to, int amount) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.TRANSFER_MONEY);
			objOut.writeUTF(from);
			objOut.writeUTF(to);
			objOut.writeInt(amount);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				throw new RuntimeException("Empty response"); 
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
						ObjectInput objIn = new ObjectInputStream(byteIn)) {
					return objIn.readBoolean();
				}
		} catch (IOException e) {
			System.out.println("Exception transfering money: " + e.getMessage());
		}
		throw new RuntimeException("Empty response"); 
	}

	@Override
	public int currentAmount(String who) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CURRENT_AMOUNT);
			objOut.writeUTF(who);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				throw new RuntimeException("Empty response"); 
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
					ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return objIn.readInt();
			}
		} catch (IOException e) {
			System.out.println("Exception checking money: " + e.getMessage());
		}
		throw new RuntimeException("Empty response"); 
	}

}