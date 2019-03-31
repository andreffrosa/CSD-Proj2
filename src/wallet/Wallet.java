package wallet;

import java.util.Map;

public interface Wallet {

	/**
	 * 
	 * */
	boolean transfer( String from, String to, double amount, String signature ) throws InvalidNumberException;

	/**
	 * 
	 * */
	double balance( String who );
	
	/**
	 * 
	 * */
	Map<String, Double> ledger();

}
