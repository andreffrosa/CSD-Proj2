package bft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.List;

import bft.reply.ReplyExtractor;
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
			throw new RuntimeException("Exception ledger: " + e.getMessage());
		}
	}
	
	byte[] putOrderPreservingInt(String id, long value, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.PUT_OPI);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);
			objOut.writeLong(value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception putting OPInt: " + e.getMessage());
		}
	}
	
	byte[] getOrderPreservingInt(String id, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET_OPI);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception getting OPInt: " + e.getMessage());
		}
	}
	
	byte[] getBetween(String k1, String k2, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET_BETWEEN_OPI);
			objOut.writeLong(nonce);
			objOut.writeUTF(k1);
			objOut.writeUTF(k2);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception getting between OPInt: " + e.getMessage());
		}
	}
	
	byte[] putSumInt(String id, BigInteger value, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.PUT_SUM);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);
			objOut.writeObject(value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception putting SumInt: " + e.getMessage());
		}
	}

	byte[] getSumInt(String id, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET_SUM);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception getting SumInt: " + e.getMessage());
		}
	}
	
	byte[] add(String id, BigInteger amount, BigInteger nSquare, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.ADD);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);
			objOut.writeObject(amount);
			objOut.writeObject(nSquare);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception putting add: " + e.getMessage());
		}
	}
	
	byte[] dif(String id, BigInteger amount, BigInteger nSquare, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.DIF);
			objOut.writeLong(nonce);
			objOut.writeUTF(id);
			objOut.writeObject(amount);
			objOut.writeObject(nSquare);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception putting add: " + e.getMessage());
		}
	}
	
}