package rest.entities;

import utils.Cryptography;

public class BalanceRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String who;

	public BalanceRequest() {
	}

	public BalanceRequest(String who) {
		super();
		this.who = who;
	}
	
	public BalanceRequest(String who, long nonce) {
		super(nonce);
		this.who = who;
	}
	
	public static String computeHash(String who, long nonce) {
		return Cryptography.computeHash(who + nonce);
	}

	@Override
	public String getHash() {
		return computeHash(who, this.getNonce());
	}
	
}
