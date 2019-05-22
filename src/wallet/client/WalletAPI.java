package wallet.client;

import java.util.List;

import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.UpdOp;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public interface WalletAPI {

	public boolean create(DataType type, String id, int initial_value) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;
	
	public int get(DataType type, String id) throws InvalidAddressException, InvalidTypeException;
	
	public List<String> get(String id_prefix, int lower_value, int higher_value) throws InvalidAddressException, InvalidTypeException;
	
	public boolean set(DataType type, String id, int value) throws InvalidAddressException, InvalidTypeException;
	
	public int sum(DataType type, String id, int value) throws InvalidAddressException, InvalidTypeException;
	
	public boolean compare(DataType type, String id, ConditionalOperation op, int value) throws InvalidAddressException, InvalidTypeException, InvalidOperationException;
	
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, int cond_val, List<UpdOp> ops) throws InvalidAddressException, InvalidTypeException, InvalidOperationException;
	
}
