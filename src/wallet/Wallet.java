package wallet;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	/**
	 * 
	 * */
	boolean putOrderPreservingInt(String id, long n); // precisa de assinatura?
	
	/**
	 * 
	 * */
	long getOrderPreservingInt(String id);
	
	/**
	 * 
	 * */
	List<Entry<String, Long>> getBetween(String k1, String k2);
	
	/**
	 * 
	 * */
	boolean putSumInt(String id, BigInteger n); // precisa de assinatura?
	
	/**
	 * 
	 * */
	BigInteger getSumInt(String id);
	
	/**
	 * 
	 * */
	BigInteger add(String key, BigInteger amount, BigInteger nSquare); // TODO: passar isto?
	
	/**
	 * 
	 * */
	BigInteger sub(String key, BigInteger amount, BigInteger nSquare); // TODO: passar isto?
	
	/**
	 * 
	 * */
	boolean cond_set(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val);
	
	/**
	 * 
	 * */
	boolean cond_add(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val, String upd_auxArg);
	
}
