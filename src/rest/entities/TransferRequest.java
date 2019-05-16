package rest.entities;

import utils.Cryptography;
import wallet.Transaction;

public class TransferRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public Transaction transaction;

	public TransferRequest() {
	}

	public TransferRequest(Transaction transaction) {
		super();
		this.transaction = transaction;
	}

	public TransferRequest(Transaction transaction, long nonce) {
		super(nonce);
		this.transaction = transaction;
	}

	public Transaction deserialize() {
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
