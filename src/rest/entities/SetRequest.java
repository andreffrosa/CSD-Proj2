package rest.entities;

import utils.Cryptography;
import wallet.DataType;

public class SetRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType type;
	public String id;
	public String value;

	public SetRequest() {
		super();
	}

	public SetRequest(DataType type, String id, String value) {
		super();
		init(type, id, value);
	}

	public SetRequest(DataType type, String id, String value, long nonce) {
		super(nonce);
		init(type, id, value);
	}

	private void init(DataType type, String id, String value) {
		this.type = type; 
		this.id = id;
		this.value = value;
	}


	public static String computeHash(DataType type, String id, String value, long nonce) {
		return Cryptography.computeHash(type + id + value + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(type, id, value, getNonce());
	}

}
