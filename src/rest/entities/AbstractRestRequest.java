package rest.entities;

import java.io.Serializable;

import utils.Cryptography;

public abstract class AbstractRestRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long nonce;
	
	public AbstractRestRequest() {
		this.nonce = Cryptography.getNonce();
	}
	
	public AbstractRestRequest(long nonce) {
		this.nonce = nonce;
	}
	
	public long getNonce() {
		return this.nonce;
	}
	
	public abstract String serialize();

}
