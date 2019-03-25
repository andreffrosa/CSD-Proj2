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

	private ServiceProxy serviceProxy;

	public BFTWalletClient(int clientId) {
		serviceProxy = new ServiceProxy(clientId, null, null, new ReplyExtractor(), null);
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
			throw new RuntimeException("Exception creating money: " + e.getMessage());
		}
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
			throw new RuntimeException("Exception transfering money: " + e.getMessage());
		} 
	}

	@Override
	public int currentAmount(String who) { // Enviar directamente a partir do server ou fazer desta forma?
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CURRENT_AMOUNT);
			objOut.writeUTF(who);

			objOut.flush();
			byteOut.flush();

			//byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			if (reply.length == 0)
				throw new RuntimeException("Empty response"); 
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
					ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return objIn.readInt();
			}
		} catch (IOException e) {
			throw new RuntimeException("Exception checking money: " + e.getMessage());
		}
	}

}