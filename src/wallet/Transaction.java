package wallet;

import java.io.Serializable;

import utils.Cryptography;

public class Transaction implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String from;
	private String to;
	private double amount;
	private String signature;
	
	public Transaction(String from, String to, double amount, String privKey) {
		this(from, to, amount, privKey, false);
	}
	
	public Transaction(String from, String to, double amount, String privKeyOrSignature, boolean signed) {
		this.from = from;
		this.to = to;
		this.amount = amount;
		
		if(signed)
			this.signature = privKeyOrSignature; //signature
		else
			this.signature = sign(privKeyOrSignature); // privKey
	}

	private String sign(String privateKey) {
		byte[] data = getDigest();
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
	
	public byte[] getDigest() {
		return (from + to + amount).getBytes();
	}
	
	public boolean isValid() {
		byte[] data = getDigest();
		boolean validSignature = Cryptography.validateSignature(data, signature, from);
		boolean validAmount = this.amount > 0.0;
		return validSignature && validAmount;	
	}

}
