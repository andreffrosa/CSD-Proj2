package rest.entities;

import utils.Cryptography;

public class CondSetRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String cond_key;
	public String cond_key_type;
	public String cond_val;
	public String cond_cipheredKey;
	public String upd_key;
	public String upd_key_type;
	public String upd_val;

	public CondSetRequest() {
		super();
	}

	public CondSetRequest(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val) {
		super();
		init(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key, upd_key_type, upd_val);
	}

	public CondSetRequest(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val, long nonce) {
		super(nonce);
		init(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key, upd_key_type, upd_val);
	}
	
	private void init(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val) {
		this.cond_key = cond_key;
		this.cond_key_type = cond_key_type;
		this.cond_val = cond_val;
		this.cond_cipheredKey = cond_cipheredKey;
		this.upd_key = upd_key;
		this.upd_key_type = upd_key_type;
		this.upd_val = upd_val;
	}

	public static String computeHash(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val, long nonce) {
		return Cryptography.computeHash(cond_key + cond_key_type + cond_val + cond_cipheredKey + upd_key + upd_key_type + upd_val + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(cond_key, cond_key_type, cond_val, cond_cipheredKey, upd_key, upd_key_type, upd_val, getNonce());
	}
	
}
