package wallet;

public class Transaction {
	
	private String from;
	private String to;
	private double amount;
	private String signature;
	
	public Transaction(String from, String to, double amount, String privateKey) {
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.signature = sign();
	}

	private String sign() {
		// TODO
		String signature = ""; //from + to + amount
		return signature;
	}
	
	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public double getAmount() {
		return amount;
	}

	public String getSignature() {
		return signature;
	}
	
	boolean isValid() {
		// TODO
		return false;
	}

}
