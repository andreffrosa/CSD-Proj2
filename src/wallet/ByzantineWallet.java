package wallet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
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
	public boolean putOrderPreservingInt(String id, long n) {
		return false;
	}

	@Override
	public long getOrderPreservingInt(String id) {
		return (long) Math.random()*Long.MAX_VALUE;
	}

	@Override
	public List<Entry<String, Long>> getBetween(String k1, String k2) {
		return new LinkedList<>();
	}

	@Override
	public boolean putSumInt(String id, BigInteger n) {
		return Math.random() > 0.5;
	}

	@Override
	public BigInteger getSumInt(String id) {
		return BigInteger.ONE;
	}

	@Override
	public BigInteger add(String key, BigInteger amount, BigInteger nSquare) {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger sub(String key, BigInteger amount, BigInteger nSquare) {
		return BigInteger.ZERO;
	}

	@Override
	public boolean cond_set(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey,
			String upd_key, String upd_key_type, String upd_val) {
		return Math.random() > 0.5;
	}

	@Override
	public boolean cond_add(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey,
			String upd_key, String upd_key_type, String upd_val, String upd_auxArg) {
		return Math.random() > 0.5;
	}

}
