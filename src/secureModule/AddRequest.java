package secureModule;

public class AddRequest {

	public long opi;
	public int amount;
	public String cipheredKey;
	
	public AddRequest(long opi, int amount, String cipheredKey) {
		this.opi = opi;
		this.amount = amount;
		this.cipheredKey = cipheredKey;
	}

}
