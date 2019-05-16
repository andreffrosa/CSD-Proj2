package rest.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import utils.Cryptography;
import wallet.Transaction;

public class TransferRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	//public String transaction;
	public Transaction transaction;

	public TransferRequest() {
	}

	public TransferRequest(Transaction transaction) {
		super();
		//Gson gson = new GsonBuilder().create();
		//this.transaction = gson.toJson(transaction);
		this.transaction = transaction;
	}

	public TransferRequest(Transaction transaction, long nonce) {
		super(nonce);
		//Gson gson = new GsonBuilder().create();
		//this.transaction = gson.toJson(transaction);
		this.transaction = transaction;
	}

	public Transaction deserialize() {
		//Gson gson = new GsonBuilder().create();
		//return gson.fromJson(transaction, Transaction.class);
		return transaction;
	}
	
	public static String computeHash(String signature, long nonce) {
		return Cryptography.computeHash(signature + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(transaction.getSignature(), getNonce());
	}
	
}
