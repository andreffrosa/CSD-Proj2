package rest.entities;

import java.math.BigInteger;

import utils.Cryptography;

public class AddSumRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String id;
	public BigInteger amount;
	public BigInteger nSquare;

	public AddSumRequest() {
		super();
	}

	public AddSumRequest(String id, BigInteger amount, BigInteger nSquare) {
		super();
		this.id = id;
		this.amount = amount;
		this.nSquare = nSquare;
	}

	public AddSumRequest(String id, BigInteger amount, BigInteger nSquare, long nonce) {
		super(nonce);
		this.id = id;
		this.amount = amount;
		this.nSquare = nSquare;
	}

	public static String computeHash(String id, BigInteger amount, BigInteger nSquare, long nonce) {
		return Cryptography.computeHash(id + amount.toString() + nSquare.toString() + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(id, amount, nSquare, getNonce());
	}
	
}
