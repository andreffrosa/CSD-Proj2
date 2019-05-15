package rest.entities;

import utils.Cryptography;

public class LedgerRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public LedgerRequest() {
		super();
	}
	
	public LedgerRequest(long nonce) {
		super(nonce);
	}
	
	public static String computeHash(long nonce) {
		return Cryptography.computeHash("" + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(getNonce());
	}
	
}
