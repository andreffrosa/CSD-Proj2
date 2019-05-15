package rest.entities;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import utils.Cryptography;
import wallet.Transaction;

public class AtomicTransferRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String[] transactions;

	public AtomicTransferRequest() {
	}

	public AtomicTransferRequest(List<Transaction> transactions) {
		Gson gson = new GsonBuilder().create();

		this.transactions = new String[transactions.size()];
		int i = 0;
		for (Transaction t : transactions) {
			this.transactions[i++] = gson.toJson(t);
		}
	}

	public List<Transaction> deserialize() {
		Gson gson = new GsonBuilder().create();

		List<Transaction> l = new ArrayList<Transaction>(transactions.length);

		for (String json : transactions) {
			l.add(gson.fromJson(json, Transaction.class));
		}

		return l;
	}

	public static String computeHash(List<Transaction> transactions, long nonce) {
		String txs_hash = "";
		for (Transaction tx : transactions) {
			txs_hash += tx.getDigestString();
		}

		return Cryptography.computeHash(txs_hash + nonce);
	}
	
	@Override
	public String getHash() {
		List<Transaction> transactions =  deserialize();
		return computeHash(transactions, this.getNonce());
	}

}
