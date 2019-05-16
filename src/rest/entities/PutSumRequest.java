package rest.entities;

import java.math.BigInteger;

import utils.Cryptography;

public class PutSumRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String id;
	public BigInteger value;

	public PutSumRequest() {
		super();
	}

	public PutSumRequest(String id, BigInteger value) {
		super();
		this.id = id;
		this.value = value;
	}

	public PutSumRequest(String id, BigInteger value, long nonce) {
		super(nonce);
		this.id = id;
		this.value = value;
	}

	public static String computeHash(String id, BigInteger value, long nonce) {
		return Cryptography.computeHash(id + value.toString() + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(id, value, getNonce());
	}
	
}
