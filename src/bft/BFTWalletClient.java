package bft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;

import bft.reply.ReplyExtractor;
import bftsmart.tom.ServiceProxy;
import utils.Serializor;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.GetBetweenOP;
import wallet.Transaction;
import wallet.UpdOp;

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

	public byte[] create(DataType type, String id, String initial_value, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.CREATE);
			objOut.writeLong(nonce);
			objOut.writeUTF(type.toString());
			objOut.writeUTF(id);
			objOut.writeUTF(initial_value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception compare: " + e.getMessage());
		}
	}
	
	public byte[] get(DataType type, String id, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET);
			objOut.writeLong(nonce);
			objOut.writeUTF(type.toString());
			objOut.writeUTF(id);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception get: " + e.getMessage());
		}
	}

	public byte[] getBetween(List<GetBetweenOP> ops, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.GET_BETWEEN);
			objOut.writeLong(nonce);
			objOut.writeObject(Serializor.serializeList(ops));

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Exception getBetween: " + e.getMessage());
		}
	}

	public byte[] set(DataType type, String id, String value, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.SET);
			objOut.writeLong(nonce);
			objOut.writeUTF(type.toString());
			objOut.writeUTF(id);
			objOut.writeUTF(value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception set: " + e.getMessage());
		}
	}

	public byte[] sum(DataType type, String id, String amount, String arg, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.SUM);
			objOut.writeLong(nonce);
			objOut.writeUTF(type.toString());
			objOut.writeUTF(id);
			objOut.writeUTF(amount);
			objOut.writeUTF(arg);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception sum: " + e.getMessage());
		}
	}

	public byte[] compare(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String ciphered_key, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.COMPARE);
			objOut.writeLong(nonce);
			objOut.writeUTF(cond_type.toString());
			objOut.writeUTF(cond_id);
			objOut.writeUTF(cond.toString());
			objOut.writeUTF(cond_val);
			objOut.writeUTF(ciphered_key);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception compare: " + e.getMessage());
		}
	}

	public byte[] cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val,
			String cond_cipheredKey, List<UpdOp> ops, long nonce) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(BFTWalletRequestType.COND_UPD);
			objOut.writeLong(nonce);
			objOut.writeUTF(cond_type.toString());
			objOut.writeUTF(cond_id);
			objOut.writeUTF(cond.toString());
			objOut.writeUTF(cond_val);
			objOut.writeUTF(cond_cipheredKey);
			objOut.writeUTF(Serializor.serializeList(ops));

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			
			return reply;
		} catch (IOException e) {
			throw new RuntimeException("Exception cond_upd: " + e.getMessage());
		}
	}
	
}