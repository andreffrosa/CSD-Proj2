package wallet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hlib.hj.mlib.HomoAdd;
import utils.Cryptography;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class SimpleWallet implements Wallet {

	private static final int DEFAULT_SIZE = 100;
	
	private static final String ADMINS_DIRECTORY = "./admins/";

	private Map<String, Double> accounts;
	private Map<String, Long> orderPreservingVariables;
	private Map<String, BigInteger> sumVariables;
	private List<String> admins;

	public SimpleWallet() {
		accounts = new HashMap<>( DEFAULT_SIZE );
		
		orderPreservingVariables = new HashMap<String, Long>( DEFAULT_SIZE );
		
		sumVariables = new HashMap<String, BigInteger>( DEFAULT_SIZE );
		
		admins = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey");
	}

	private double createMoney(String who, double amount) {
		Double old = accounts.get(who);
		if (old == null) {
			if (amount > 0.0)
				accounts.put(who, amount);

			return amount;
		} else {
			if (amount > 0.0)
				accounts.put(who, old + amount);

			return old + amount;
		}
	}

	private boolean isAdmin(String address) {
		return admins.contains(address);
	}

	@Override
	public boolean transfer(Transaction transaction)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {

		Double from_balance = accounts.get(transaction.getFrom());

		// If the signature is valid
		if (transaction.validate()) {
			// If the transaction came from an admin
			if (isAdmin(transaction.getFrom())) {
				createMoney(transaction.getTo(), transaction.getAmount());
				return true;
			} else {
				if (from_balance != null) {
					// If there's enough money in the account
					if (from_balance.doubleValue() >= transaction.getAmount()) {
						double new_balance = from_balance.doubleValue() - transaction.getAmount();

						if (new_balance > 0)
							accounts.put(transaction.getFrom(), new_balance);
						else
							accounts.remove(transaction.getFrom());

						createMoney(transaction.getTo(), transaction.getAmount());
						return true;
					} else
						throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
				} else
					throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
			}
		}

		return false;
	}

	@Override
	public double balance(String who) {
		Double balance = accounts.get(who);

		if (balance == null)
			return 0.0;

		return balance.doubleValue();
	}

	@Override
	public Map<String, Double> ledger() {
		return new HashMap<>(accounts);
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {

		Map<String, Double> temp = new HashMap<>(accounts); // Backup

		boolean result = true;
		try {
			for (Transaction t : transactions) {

				if (this.transfer(t)) {
					accounts = temp; // Rollback
					break;
				}
			}
		} catch (Exception e) {
			accounts = temp; // Rollback
			throw e;
		}

		return result;
	}

	@Override
	public boolean putOrderPreservingInt(String id, long n) {
		return orderPreservingVariables.putIfAbsent(id, n) == null;
	}

	@Override
	public long getOrderPreservingInt(String id) {
		// TODO: o que fazer quando não existe? Excepção?
		Long n = orderPreservingVariables.get(id);
		if( n != null ) {
			return n;
		} else {
			return 0L;
		}
	}

	@Override
	public List<Entry<String, Long>> getBetween(String k1, String k2) {
		List<Entry<String, Long>> result = new LinkedList<>();

		// TODO: O que fazer quando um dos extremos não existe?
		Long v1 = orderPreservingVariables.get(k1);
		Long v2 = orderPreservingVariables.get(k2);
		
		for(Entry<String, Long> e : orderPreservingVariables.entrySet()) {
			if( v1 <= e.getValue() && e.getValue() <= v2) {
				result.add(e);
			}
		}
		
		return result;
	}

	@Override
	public boolean putSumInt(String id, BigInteger n) {
		return sumVariables.putIfAbsent(id, n) == null;
	}

	@Override
	public BigInteger getSumInt(String id) {
		// TODO: o que fazer quando não existe? Excepção?
		BigInteger n = sumVariables.get(id);
		if( n != null ) {
			return n;
		} else {
			return BigInteger.ZERO;
		}
	}

	@Override
	public BigInteger add(String key, BigInteger amount, BigInteger nSquare) {
		BigInteger value = HomoAdd.sum(getSumInt(key), amount, nSquare);
		sumVariables.put(key, value);
		return value;
	}

	@Override
	public BigInteger sub(String key, BigInteger amount, BigInteger nSquare) {
		BigInteger value = HomoAdd.dif(getSumInt(key), amount, nSquare);
		sumVariables.put(key, value);
		return value;
	}

}
