package wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public class ByzantineWallet implements Wallet {

	public ByzantineWallet() {
	}

	@Override
	public boolean transfer(Transaction transaction)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {
		return Math.random() > 0.5;
	}

	@Override
	public double balance(String who) {
		return Math.random() * Double.MAX_VALUE;
	}

	@Override
	public Map<String, Double> ledger() {
		return new HashMap<>();
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {
		return Math.random() > 0.5;
	}

	@Override
	public boolean create(DataType type, String id, String initial_value) {
		return Math.random() > 0.5;
	}

	@Override
	public String get(DataType type, String id) throws InvalidAddressException {
		return Integer.toString((int) (Math.random()*Integer.MAX_VALUE));
	}

	public List<String> getBetween(List<GetBetweenOP> ops) {
		return new ArrayList<String>();
	}

	@Override
	public boolean set(DataType type, String id, String value) throws InvalidTypeException {
		return Math.random() > 0.5;
	}

	@Override
	public String sum(DataType key_type, String key, String amount, String arg)
			throws InvalidAddressException, InvalidTypeException {
		return Integer.toString((int) (Math.random()*Integer.MAX_VALUE));
	}

	@Override
	public boolean compare(DataType cond_type, String cond_key, ConditionalOperation cond, String cond_val,
			String cipheredKey) throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		return Math.random() > 0.5;
	}

	@Override
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val,
			String cond_cipheredKey, List<UpdOp> ops)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		return Math.random() > 0.5;
	}

}
