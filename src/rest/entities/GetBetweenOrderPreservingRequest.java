package rest.entities;

import utils.Cryptography;

public class GetBetweenOrderPreservingRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String k1;
	public String k2;

	public GetBetweenOrderPreservingRequest() {
	}

	public GetBetweenOrderPreservingRequest(String k1, String k2) {
		super();
		this.k1 = k1;
		this.k2 = k2;
	}

	public GetBetweenOrderPreservingRequest(String k1, String k2, long nonce) {
		super(nonce);
		this.k1 = k1;
		this.k2 = k2;
	}

	public static String computeHash(String k1, String k2, long nonce) {
		return Cryptography.computeHash(k1 + k2 + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(k1, k2, getNonce());
	}
}
