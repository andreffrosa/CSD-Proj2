package secureModule;

public class AddRequest {

	public long opi;
	public long amount;
	public String cipheredKey;
	
	public AddRequest(long opi, long amount, String cipheredKey) {
		this.opi = opi;
		this.amount = amount;
		this.cipheredKey = cipheredKey;
	}

}
