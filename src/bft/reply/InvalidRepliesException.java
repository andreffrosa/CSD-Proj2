package bft.reply;

public class InvalidRepliesException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidRepliesException() {
		super();
	}

	public InvalidRepliesException(String message) {
		super(message);
	}

}
