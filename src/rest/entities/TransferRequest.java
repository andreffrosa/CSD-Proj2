package rest.entities;

public class TransferRequest {
	
	public String from;
	public String to;
	public double amount;
	public String signature;
	
	public TransferRequest() {}
	
	public TransferRequest(String from, String to, double amount, String signature) {
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.signature = signature;
	}
}
