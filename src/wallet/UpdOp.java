package wallet;

public class UpdOp {

	public UpdOperation op;
	public DataType upd_type;
	public String upd_id;
	public int upd_value_unciphered;
	public String upd_value;
	public String auxArg;
	
	public UpdOp(UpdOperation op, DataType type, String upd_id, int upd_value_unciphered) {
		this.op = op;
		this.upd_type = type;
		this.upd_id = upd_id;
		this.upd_value_unciphered = upd_value_unciphered;
		this.upd_value = null;
		this.auxArg = null;
	}
	
}
