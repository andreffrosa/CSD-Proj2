package rest;

import java.util.List;

import utils.Cryptography;
import wallet.Transaction;

public class OperationsHashUtil {

	public static String transferHash(String from, String to, double amount, String signature, long nonce) {
		return Cryptography.computeHash(signature + nonce);
	}
	
	public static String atomicTransferHash(List<Transaction> transactions, long nonce) {
		String txs_hash = "";
		for(Transaction tx : transactions) {
			txs_hash += tx.getDigestString();
		}

		return Cryptography.computeHash(txs_hash + nonce);
	}
	
	public static String balanceHash(String who, long nonce) {
		return Cryptography.computeHash(who + nonce);
	}
	
	public static String ledgerHash(long nonce) {
		return Cryptography.computeHash("" + nonce);
	}
	
}
