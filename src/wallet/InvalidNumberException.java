package wallet;

public class InvalidNumberException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidNumberException() {
		super();
	}
	
	public InvalidNumberException(String message) {
		super(message);
	}
	
}
