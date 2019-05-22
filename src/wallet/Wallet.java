package wallet;

import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
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
	public boolean transfer( Transaction transaction ) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException;
	
	/**
	 * @throws NotEnoughMoneyException 
	 * @throws InvalidAmountException 
	 * @throws InvalidSignatureException 
	 * @throws InvalidAddressException 
	 * 
	 * */
	public boolean atomicTransfer( List<Transaction> transactions ) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException;

	/**
	 * 
	 * */
	public double balance( String who );
	
	/**
	 * 
	 * */
	public Map<String, Double> ledger();

	/**
	 * 
	 * */
	public boolean create(DataType type, String id, String initial_value);
	
	/**
	 * @throws InvalidAddressException 
	 * 
	 * */
	public String get(DataType type, String id) throws InvalidAddressException;
	
	/**
	 * 
	 * */
	List<String> getBetween(List<GetBetweenOP> ops);
	
	/**
	 * @throws InvalidTypeException 
	 * 
	 * */
	public boolean set(DataType type, String id, String value) throws InvalidTypeException;
	
	/**
	 * @throws InvalidAddressException 
	 * @throws InvalidTypeException 
	 * 
	 * */
	String sum(DataType key_type, String key, String amount, String arg) throws InvalidAddressException, InvalidTypeException;
	
	/**
	 * @throws InvalidAddressException 
	 * @throws InvalidTypeException 
	 * @throws InvalidOperationException 
	 * 
	 * */
	public boolean compare(DataType cond_type, String cond_key, ConditionalOperation cond, String cond_val, String cipheredKey)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException;
		
	/**
	 * @throws InvalidTypeException 
	 * @throws InvalidAddressException 
	 * @throws InvalidOperationException 
	 * 
	 * */
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val, String cond_cipheredKey, List<UpdOp> ops) throws InvalidAddressException, InvalidTypeException, InvalidOperationException;
	
}
