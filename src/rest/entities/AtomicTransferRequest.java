package rest.entities;

import java.util.ArrayList;
import java.util.List;

import utils.Cryptography;
import wallet.Transaction;

public class AtomicTransferRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public ArrayList<Transaction> transactions;

	public AtomicTransferRequest() {
	}

	public AtomicTransferRequest(List<Transaction> transactions) {
		this.transactions = new ArrayList<>(transactions);
	}

	public List<Transaction> deserialize() {
		return transactions;
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
		return computeHash(transactions, this.getNonce());
	}

}
