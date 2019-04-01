package wallet;

import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public interface Wallet {

	/**
	 * 
	 * */
	boolean transfer( Transaction transaction ) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;
	
	/**
	 * 
	 * */
	boolean atomicTransfer( List<Transaction> transactions ) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	/**
	 * 
	 * */
	double balance( String who );
	
	/**
	 * 
	 * */
	Map<String, Double> ledger();

}
