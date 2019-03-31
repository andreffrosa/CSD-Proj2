package wallet;

import java.util.List;
import java.util.Map;

public interface Wallet {

	/**
	 * 
	 * */
	boolean transfer( String from, String to, double amount, String signature ) throws InvalidNumberException;
	
	/**
	 * 
	 * */
	boolean atomicTransfer( List<Transaction> transactions ) throws InvalidNumberException;

	/**
	 * 
	 * */
	double balance( String who );
	
	/**
	 * 
	 * */
	Map<String, Double> ledger();

}
