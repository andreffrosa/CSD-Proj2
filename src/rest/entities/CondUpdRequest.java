package rest.entities;

import java.util.List;

import utils.Cryptography;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.UpdOp;

public class CondUpdRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public DataType cond_type;
	public String cond_id;
	public ConditionalOperation cond;
	public String cond_val;
	public String cond_cipheredKey;
	public List<UpdOp> ops;

	public CondUpdRequest() {
		super();
	}

	public CondUpdRequest(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cond_cipheredKey, List<UpdOp> ops) {
		super();
		init(cond_type, cond_id, cond, cond_val, cond_cipheredKey, ops);
	}

	public CondUpdRequest(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cond_cipheredKey, List<UpdOp> ops, long nonce) {
		super(nonce);
		init(cond_type, cond_id, cond, cond_val, cond_cipheredKey, ops);
	}

	private void init(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cond_cipheredKey, List<UpdOp> ops) {
		this.cond_type = cond_type;
		this.cond_id = cond_id;
		this.cond = cond;
		this.cond_val = cond_val;
		this.cond_cipheredKey = cond_cipheredKey;
		this.ops = ops;
	}

	public static String computeHash(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cond_cipheredKey, List<UpdOp> ops, long nonce) {
		
		String hash = "";
		for(UpdOp op : ops) {
			hash += op.upd_type + op.upd_id + op.upd_value + op.op + op.auxArg;
		}
		
		return Cryptography.computeHash(cond_type + cond_id + cond + cond_val + cond_cipheredKey + hash + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(cond_type, cond_id, cond, cond_val, cond_cipheredKey, ops, getNonce());
	}

}
