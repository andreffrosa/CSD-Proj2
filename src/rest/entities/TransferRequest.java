package rest.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wallet.Transaction;

public class TransferRequest extends AbstractRestRequest {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String transaction;
	
	public TransferRequest() {}
	
	public TransferRequest(Transaction transaction) {
		super();
		Gson gson = new GsonBuilder().create();
		this.transaction = gson.toJson(transaction);
	}
	
	public TransferRequest(Transaction transaction, long nonce) {
		super(nonce);
		Gson gson = new GsonBuilder().create();
		this.transaction = gson.toJson(transaction);
	}

	@Override
	public String serialize() {
		return transaction;
	}

	public Transaction deserialize() {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(transaction, Transaction.class);
	}
}
