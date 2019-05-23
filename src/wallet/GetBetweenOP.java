package wallet;

public class GetBetweenOP {

	public DataType type;
	public String id;
	public String low_value;
	public String high_value;
	public String cipheredKey;
	
	public GetBetweenOP(DataType type, String id, String low_value, String high_value, String cipheredKey) {
		this.type = type;
		this.id = id;
		this.low_value = low_value;
		this.high_value = high_value;
		this.cipheredKey = cipheredKey;
	}
	
}
