package rest.entities;

import java.util.List;

import utils.Cryptography;
import wallet.GetBetweenOP;

public class GetBetweenRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public List<GetBetweenOP> ops;

	public GetBetweenRequest() {
		super();
	}

	public GetBetweenRequest(List<GetBetweenOP> ops) {
		super();
		this.ops = ops;
	}

	public GetBetweenRequest(List<GetBetweenOP> ops, long nonce) {
		super(nonce);
		this.ops = ops;
	}
	
	public static String computeHash(List<GetBetweenOP> ops, long nonce) {
		
		String hash = "";
		for(GetBetweenOP op : ops) {
			hash += op.type + op.id + op.low_value + op.high_value + op.cipheredKey;
		}
		
		return Cryptography.computeHash(hash+ nonce);
	}

	@Override
	public String getHash() {
		return computeHash(ops, getNonce());
	}
	
}
