package rest.entities;

import utils.Cryptography;
import wallet.DataType;

public class GetRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType type; 
	public String id;

	public GetRequest() {
		super();
	}

	public GetRequest(DataType type, String id) {
		super();
		init(type, id);
	}

	public GetRequest(DataType type, String id, long nonce) {
		super(nonce);
		init(type, id);
	}
	
	private void init(DataType type, String id) {
		this.type = type; 
		this.id = id;
	}

	public static String computeHash(DataType type, String id, long nonce) {
		return Cryptography.computeHash(type + id + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(type, id, getNonce());
	}
	
}
