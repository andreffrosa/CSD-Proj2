package wallet.exceptions;

public class InvalidAddressException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidAddressException() {
		super();
	}

	public InvalidAddressException(String message) {
		super(message);
	}

}
