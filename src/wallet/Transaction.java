package wallet;

import utils.Cryptography;

public class Transaction {
	
	private String from;
	private String to;
	private double amount;
	private String signature;
	
	public Transaction(String from, String to, double amount, String privateKey) {
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.signature = sign(privateKey);
	}

	private String sign(String privateKey) {
		byte[] data = (from + to + amount).getBytes();
		String signature = Cryptography.sign(data, privateKey);
		return signature;
	}
	
	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public double getAmount() {
		return amount;
	}

	public String getSignature() {
		return signature;
	}
	
	boolean isValid() {
		byte[] data = (from + to + amount).getBytes();
		return Cryptography.validateSignature(data, signature, from);
	}

}
