package wallet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import hlib.hj.mlib.HomoAdd;
import secureModule.SecureModuleRESTClient;
import utils.Cryptography;
import utils.IO;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public class SimpleWallet implements Wallet {

	private static final int DEFAULT_SIZE = 100;

	private static final String ADMINS_DIRECTORY = "./admins/";

	private Map<String, Double> accounts;
	private Map<String, Long> orderPreservingVariables;
	private Map<String, BigInteger> sumVariables;
	private List<String> admins;
	private SecureModuleRESTClient secureModule;

	public SimpleWallet() {
		accounts = new HashMap<>(DEFAULT_SIZE);

		orderPreservingVariables = new HashMap<String, Long>(DEFAULT_SIZE);

		sumVariables = new HashMap<String, BigInteger>(DEFAULT_SIZE);

		admins = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey");

		secureModule = new SecureModuleRESTClient(
				((String[]) IO.loadObject("./secure_module.json", String[].class))[0]);
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
		// return orderPreservingVariables.putIfAbsent(id, n) == null;
		return orderPreservingVariables.put(id, n) == null;
	}

	@Override
	public long getOrderPreservingInt(String id) throws InvalidAddressException {

		Long n = orderPreservingVariables.get(id);
		if (n != null) {
			return n;
		} else {
			throw new InvalidAddressException(id + " is not registered!");
		}
	}

	@Override
	public List<Entry<String, Long>> getBetween(String k1, String k2) throws InvalidAddressException {
		List<Entry<String, Long>> result = new LinkedList<>();

		Long v1 = orderPreservingVariables.get(k1);
		Long v2 = orderPreservingVariables.get(k2);

		if (v1 == null) {
			throw new InvalidAddressException(v1 + " is not registered!");
		}
		if (v2 == null) {
			throw new InvalidAddressException(v2 + " is not registered!");
		}

		for (Entry<String, Long> e : orderPreservingVariables.entrySet()) {
			if (v1 <= e.getValue() && e.getValue() <= v2) {
				result.add(e);
			}
		}

		return result;
	}

	@Override
	public boolean putSumInt(String id, BigInteger n) {
		// return sumVariables.putIfAbsent(id, n) == null;
		return sumVariables.put(id, n) == null;
	}

	@Override
	public BigInteger getSumInt(String id) throws InvalidAddressException {

		BigInteger n = sumVariables.get(id);
		if (n != null) {
			return n;
		} else {
			throw new InvalidAddressException(id + " is not registered!");
		}
	}

	@Override
	public BigInteger add_sumInt(String key, BigInteger amount, BigInteger nSquare) throws InvalidAddressException {
		BigInteger value = HomoAdd.sum(getSumInt(key), amount, nSquare);
		sumVariables.put(key, value);
		return value;
	}

	@Override
	public BigInteger sub(String key, BigInteger amount, BigInteger nSquare) throws InvalidAddressException {
		BigInteger value = HomoAdd.dif(getSumInt(key), amount, nSquare);
		sumVariables.put(key, value);
		return value;
	}

	@Override
	public int compare(String cond_key, String cond_key_type, String cond_val, String cipheredKey)
			throws InvalidAddressException, InvalidTypeException {
		BigInteger c_val = new BigInteger(cond_val);
		BigInteger aux;

		switch (cond_key_type) {
		case "wallet":
			aux = new BigInteger("" + (int) balance(cond_key));
			return aux.compareTo(c_val);
		case "OPI":
			aux = new BigInteger("" + getOrderPreservingInt(cond_key));
			return aux.compareTo(c_val);
		case "SumInt":
			BigInteger v1 = getSumInt(cond_key);
			return secureModule.compareSumInt(v1, c_val, cipheredKey);
		default:
			throw new InvalidTypeException(cond_key_type + " is not a valid type!");
		}
	}

	private void set(String upd_key, String upd_key_type, String upd_val) throws InvalidTypeException {

		switch (upd_key_type) {
		case "wallet":
			double temp1 = (double) Integer.parseInt(upd_val);
			accounts.put(upd_key, temp1);
			break;
		case "OPI":
			long temp2 = Long.parseLong(upd_val);
			putOrderPreservingInt(upd_key, temp2);
			break;
		case "SumInt":
			BigInteger temp3 = new BigInteger(upd_val);
			putSumInt(upd_key, temp3);
			break;
		default:
			throw new InvalidTypeException(upd_key_type + " is not a valid type!");
		}
	}

	@Override
	public String add(String upd_key, String upd_key_type, String upd_val, String upd_auxArg)
			throws InvalidAddressException, InvalidTypeException {

		String result = "";
		
		switch (upd_key_type) {
		case "wallet":
			double v1 = (double) Integer.parseInt(upd_val);
			double v2 = balance(upd_key);
			
			accounts.put(upd_key, v1 + v2);
			
			result = "" + v1 + v2;
			break;
		case "OPI":
			long amount = Long.parseLong(upd_val);
			long l = getOrderPreservingInt(upd_key);
			long value = secureModule.addOPI(l, amount, upd_auxArg);
			putOrderPreservingInt(upd_key, value);
			
			result = "" + value;
			break;
		case "SumInt":
			BigInteger amount2 = new BigInteger(upd_val);
			BigInteger nSquare = new BigInteger(upd_auxArg);

			BigInteger big = add_sumInt(upd_key, amount2, nSquare);
			
			result = big.toString();
			break;
		default:
			throw new InvalidTypeException(upd_key_type + " is not a valid type!");
		}
		
		return result;
	}

	@Override
	public boolean cond_set(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey,
			String upd_key, String upd_key_type, String upd_val) throws InvalidAddressException, InvalidTypeException {

		if (compare(cond_key, cond_key_type, cond_val, cond_cipheredKey) >= 0) {
			set(upd_key, upd_key_type, upd_val);
			return true;
		}

		return false;
	}

	@Override
	public boolean cond_add(String cond_key, String cond_key_type, String cond_val, String cond_cipheredKey,
			String upd_key, String upd_key_type, String upd_val, String upd_auxArg)
			throws InvalidAddressException, InvalidTypeException {

		if (compare(cond_key, cond_key_type, cond_val, cond_cipheredKey) >= 0) {
			add(upd_key, upd_key_type, upd_val, upd_auxArg);
			return true;
		}

		return false;
	}

}
