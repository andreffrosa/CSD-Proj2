package wallet;

public interface Wallet {

	int createMoney( String who, int amount );

	boolean transfer( String from, String to, int amount ) throws InvalidNumberException;

	int currentAmount( String who );

}
