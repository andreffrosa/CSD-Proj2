package wallet;

import java.util.List;
import java.util.Map;

public interface Wallet {

	/**
	 * 
	 * */
	boolean transfer( Transaction transaction ); //throws InvalidNumberException;
	
	/**
	 * 
	 * */
	boolean atomicTransfer( List<Transaction> transactions ); // throws InvalidNumberException;

	/**
	 * 
	 * */
	double balance( String who );
	
	/**
	 * 
	 * */
	Map<String, Double> ledger();

}
