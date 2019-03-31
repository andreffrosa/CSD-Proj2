package rest.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wallet.Transaction;

public class AtomicTransferRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String[] transactions;
	
	public AtomicTransferRequest() {}
	
	public AtomicTransferRequest(List<Transaction> transactions) {
		Gson gson = new GsonBuilder().create();
		
		this.transactions = new String[transactions.size()];
		int i = 0;
		for(Transaction t : transactions) {
			this.transactions[i++] = gson.toJson(t);
		}
	}
	
	public List<Transaction> deserialize() {
		Gson gson = new GsonBuilder().create();
		
		//List<String> temp = new GsonBuilder().create().fromJson(transactions, ArrayList.class);
		List<Transaction> l = new ArrayList<Transaction>(transactions.length);
		
		for(String json : transactions) {
			l.add(gson.fromJson(json, Transaction.class));
		}
		
		return l;
	}
}
