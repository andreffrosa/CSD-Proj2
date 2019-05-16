package rest.entities;

import utils.Cryptography;

public class GetSumRequest extends AbstractRestRequest {
	private static final long serialVersionUID = 1L;

	public String id;

	public GetSumRequest() {
	}

	public GetSumRequest(String id) {
		super();
		this.id = id;
	}

	public GetSumRequest(String id, long nonce) {
		super(nonce);
		this.id = id;
	}

	public static String computeHash(String id, long nonce) {
		return Cryptography.computeHash(id + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(id, getNonce());
	}

}
