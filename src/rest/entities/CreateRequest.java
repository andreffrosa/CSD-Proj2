package rest.entities;

import utils.Cryptography;
import wallet.DataType;

public class CreateRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType type; 
	public String id;
	public String initial_value;

	public CreateRequest() {
		super();
	}

	public CreateRequest(DataType type, String id, String initial_value) {
		super();
		init(type, id, initial_value);
	}

	public CreateRequest(DataType type, String id, String initial_value, long nonce) {
		super(nonce);
		init(type, id, initial_value);
	}
	
	private void init(DataType type, String id, String initial_value) {
		this.type = type; 
		this.id = id;
		this.initial_value = initial_value;
	}

	public static String computeHash(DataType type, String id, String initial_value, long nonce) {
		return Cryptography.computeHash(type + id + initial_value + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(type, id, initial_value, getNonce());
	}
	
}
