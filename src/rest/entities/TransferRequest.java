package rest.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wallet.Transaction;

public class TransferRequest {
	
	public String transaction;
	
	public TransferRequest() {}
	
	public TransferRequest(Transaction transaction) {
		Gson gson = new GsonBuilder().create();
		this.transaction = gson.toJson(transaction);
	}
	
	public Transaction deserialize() {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(transaction, Transaction.class);
	}
}
