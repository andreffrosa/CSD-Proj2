package rest.entities;

import utils.Cryptography;
import wallet.ConditionalOperation;
import wallet.DataType;

public class CompareRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType cond_type;
	public String cond_id;
	public ConditionalOperation cond;
	public String cond_val;
	public String cipheredKey;

	public CompareRequest() {
		super();
	}

	public CompareRequest(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cipheredKey) {
		super();
		init(cond_type, cond_id, cond, cond_val, cipheredKey);
	}

	public CompareRequest(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cipheredKey, long nonce) {
		super(nonce);
		init(cond_type, cond_id, cond, cond_val, cipheredKey);
	}
	
	private void init(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cipheredKey) {
		this.cond_type = cond_type;
		this.cond_id = cond_id;
		this.cond = cond;
		this.cond_val = cond_val;
		this.cipheredKey = cipheredKey;
	}

	public static String computeHash(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cipheredKey, long nonce) {
		return Cryptography.computeHash(cond_type + cond_id + cond + cond_val + cipheredKey + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(cond_type, cond_id, cond, cond_val, cipheredKey, getNonce());
	}
	
}
