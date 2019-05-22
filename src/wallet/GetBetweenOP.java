package wallet;

public class GetBetweenOP {

	public DataType type;
	public String prefix;
	public String low_value;
	public String high_value;
	public String cipheredKey;
	
	public GetBetweenOP(DataType type, String prefix, String low_value, String high_value, String cipheredKey) {
		this.type = type;
		this.prefix = prefix;
		this.low_value = low_value;
		this.high_value = high_value;
		this.cipheredKey = cipheredKey;
	}
	
}
