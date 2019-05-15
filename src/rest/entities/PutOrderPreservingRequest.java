package rest.entities;

import utils.Cryptography;

public class PutOrderPreservingRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String id;
	public long value;

	public PutOrderPreservingRequest() {
		super();
	}

	public PutOrderPreservingRequest(String id, long value) {
		super();
		this.id = id;
		this.value = value;
	}

	public PutOrderPreservingRequest(String id, long value, long nonce) {
		super(nonce);
		this.id = id;
		this.value = value;
	}

	public static String computeHash(String id, long value, long nonce) {
		return Cryptography.computeHash(id + value + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(id, value, getNonce());
	}
	
}
