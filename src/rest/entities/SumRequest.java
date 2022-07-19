package rest.entities;

import utils.Cryptography;
import wallet.DataType;

public class SumRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType type;
	public String id;
	public String amount;
	public String arg;

	public SumRequest() {
		super();
	}

	public SumRequest(DataType type, String id, String amount, String arg) {
		super();
		init(type, id, amount, arg);
	}

	public SumRequest(DataType type, String id, String amount, String arg, long nonce) {
		super(nonce);
		init(type, id, amount, arg);
	}

	private void init(DataType type, String id, String amount, String arg) {
		this.type = type;
		this.id = id;
		this.amount = amount;
		this.arg = arg;
	}

	public static String computeHash(DataType type, String id, String amount, String arg, long nonce) {
		return Cryptography.computeHash(type + id + amount + arg + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(type, id,  amount, arg, getNonce());
	}

}
