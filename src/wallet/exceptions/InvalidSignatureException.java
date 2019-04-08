package wallet.exceptions;

public class InvalidSignatureException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidSignatureException() {
		super();
	}

	public InvalidSignatureException(String message) {
		super(message);
	}

}
