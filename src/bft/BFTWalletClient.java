package bft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import bft.BFTWalletRequestType;
import bftsmart.tom.ServiceProxy;
import wallet.Transaction;

public class BFTWalletClient {

	private ServiceProxy serviceProxy;

	public BFTWalletClient(int clientId) {
		serviceProxy = new ServiceProxy(clientId, null, null, new ReplyExtractor(), null);
	}

	public void close() {
		serviceProxy.close();
	}

	public byte[] transfer(Transaction t, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.TRANSFER_MONEY);
			objOut.writeLong(nonce);
			objOut.writeUTF(t.getFrom());
			objOut.writeUTF(t.getTo());
			objOut.writeDouble(t.getAmount());
			objOut.writeUTF(t.getSignature());
			

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception transfering money: " + e.getMessage());
		} 
	}
	
	public byte[] atomicTransfer(List<Transaction> transactions, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.ATOMIC_TRANSFER_MONEY);
			objOut.writeLong(nonce);
			objOut.writeObject(transactions);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception transfering money: " + e.getMessage());
		} 
	}

	public byte[] balance(String who, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CURRENT_BALANCE);
			objOut.writeLong(nonce);
			objOut.writeUTF(who);
			

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception checking money: " + e.getMessage());
		}
	}
	
	public byte[] ledger(long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET_LEDGER);
			objOut.writeLong(nonce);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception checking money: " + e.getMessage());
		}
	}

}