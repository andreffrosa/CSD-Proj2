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
import rest.DistributedWallet;

public class BFTWalletClient implements DistributedWallet {

	private ServiceProxy serviceProxy;

	public BFTWalletClient(int clientId) {
		serviceProxy = new ServiceProxy(clientId, null, null, new ReplyExtractor(), null);
	}

	public void close() {
		serviceProxy.close();
	}

	private BFTReply processReply( byte[] reply ) throws IOException  {
		
		if (reply.length == 0) {
			//throw new RuntimeException("Empty response"); 
			return null;
		}

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {

			int replies = objIn.readInt();

			byte[][] signatures = new byte[replies][];
			int[] ids =  new int[replies];
			byte[] ans = (byte[]) objIn.readObject();

			for(int i = 0; i < replies; i++) {
				signatures[i] = (byte[]) objIn.readObject();
				ids[i] = (int) objIn.readInt();
			}

			return new BFTReply(replies, ans, signatures, ids);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//throw new RuntimeException("ClassNotFoundException"); 
			return null;
		}
	}

	@Override
	public BFTReply createMoney(String who, int amount) {
		try {

			try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
					ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

				objOut.writeObject(BFTWalletRequestType.CREATE_MONEY);
				objOut.writeUTF(who);
				objOut.writeInt(amount);

				objOut.flush();
				byteOut.flush();

				byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray()); 

				return processReply(reply);
			} catch (IOException e) {
				throw new RuntimeException("Exception creating money: " + e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public BFTReply transfer(String from, String to, int amount) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.TRANSFER_MONEY);
			objOut.writeUTF(from);
			objOut.writeUTF(to);
			objOut.writeInt(amount);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return processReply(reply);
		} catch (IOException e) {
			throw new RuntimeException("Exception transfering money: " + e.getMessage());
		} 
	}

	@Override
	public BFTReply currentAmount(String who) { // Enviar directamente a partir do server ou fazer desta forma?
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CURRENT_AMOUNT);
			objOut.writeUTF(who);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return processReply(reply);
		} catch (IOException e) {
			throw new RuntimeException("Exception checking money: " + e.getMessage());
		}
	}

}