package wallet;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public interface Wallet {

	/**
	 * @throws NotEnoughMoneyException 
	 * @throws InvalidAmountException 
	 * @throws InvalidSignatureException 
	 * @throws InvalidAddressException 
	 * 
	 * */
	boolean transfer( Transaction transaction ) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException;
	
	/**
	 * @throws NotEnoughMoneyException 
	 * @throws InvalidAmountException 
	 * @throws InvalidSignatureException 
	 * @throws InvalidAddressException 
	 * 
	 * */
	boolean atomicTransfer( List<Transaction> transactions ) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException;

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
	boolean putOrderPreservingInt(String id, long n);
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	long getOrderPreservingInt(String id) throws InvalidAddressException;
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	List<Entry<String, Long>> getBetween(String k1, String k2) throws InvalidAddressException;
	
	/**
	 * 
	 * */
	boolean putSumInt(String id, BigInteger n);
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	BigInteger getSumInt(String id) throws InvalidAddressException;
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	BigInteger add_sumInt(String key, BigInteger amount, BigInteger nSquare) throws InvalidAddressException;
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	BigInteger sub(String key, BigInteger amount, BigInteger nSquare) throws InvalidAddressException;
	
	/**
	 * @throws InvalidAddressException 
	 * @throws InvalidTypeException 
	 * 
	 * */
	String add(String key_type, String key, String amount, String arg) throws InvalidAddressException, InvalidTypeException;
	
	/**
	 * @throws InvalidAddressException 
	 * @throws InvalidTypeException 
	 * 
	 * */
	int compare(String key_type, String key, String value, String cipheredKey) throws InvalidAddressException, InvalidTypeException;
	
	/**
	 * @throws InvalidTypeException 
	 * @throws InvalidAddressException 
	 * 
	 * */
	boolean cond_set(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val) throws InvalidAddressException, InvalidTypeException;
	
	/**
	 * @throws InvalidTypeException 
	 * @throws InvalidAddressException 
	 * 
	 * */
	boolean cond_add(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey, String upd_key, String upd_key_type, String upd_val, String upd_auxArg) throws InvalidAddressException, InvalidTypeException;
	
}
