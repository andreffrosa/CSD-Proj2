package rest.entities;

import utils.Cryptography;

public class AddRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String key;
	public String key_type;
	public String val;
	public String auxArg;

	public AddRequest() {
		super();
	}

	public AddRequest(String key, String key_type, String val, String auxArg) {
		super();
		init(key, key_type, val, auxArg);
	}

	public AddRequest(String key, String key_type, String val, String auxArg, long nonce) {
		super(nonce);
		init(key, key_type, val, auxArg);
	}
	
	private void init(String key, String key_type, String val, String auxArg) {
		this.key = key;
		this.key_type = key_type;
		this.val = val;
		this.auxArg = auxArg;
	}

	public static String computeHash(String key, String key_type, String val, String auxArg, long nonce) {
		return Cryptography.computeHash(key + key_type + val + auxArg + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(key, key_type, val, auxArg, getNonce());
	}
	
}
