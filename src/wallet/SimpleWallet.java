package wallet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hlib.hj.mlib.HomoAdd;
import secureModule.SecureModuleRESTClient;
import utils.ConditionParser;
import utils.Cryptography;
import utils.IO;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

public class SimpleWallet implements Wallet {

	private static final int DEFAULT_SIZE = 100;

	private static final String ADMINS_DIRECTORY = "./admins/";

	private Map<String, Double> accounts;
	private Map<String, Long> homo_ope_int_variables;
	private Map<String, BigInteger> homo_add_variables;
	private List<String> admins;
	private SecureModuleRESTClient secureModule;

	public SimpleWallet() {
		accounts = new HashMap<>(DEFAULT_SIZE);

		homo_ope_int_variables = new HashMap<String, Long>(DEFAULT_SIZE);

		homo_add_variables = new HashMap<String, BigInteger>(DEFAULT_SIZE);

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
	public boolean create(DataType type, String id, String initial_value) {

		switch(type) {
		case HOMO_ADD:
			return homo_add_variables.put(id, new BigInteger(initial_value)) == null;
		case HOMO_OPE_INT:
			return homo_ope_int_variables.put(id, Long.parseLong(initial_value)) == null;
		case WALLET:
			return accounts.put(id, (double) Integer.parseInt(initial_value)) == null; // transfer?
		}

		return false;
	}

	@Override
	public String get(DataType type, String id) throws InvalidAddressException {

		Object result = null;

		switch(type) {
		case HOMO_ADD:
			result = homo_add_variables.get(id);
			break;
		case HOMO_OPE_INT:
			result = homo_ope_int_variables.get(id);
			break;
		case WALLET:
			Double aux = accounts.get(id);
			result = aux == null ? null : (Integer) aux.intValue();
			break;
		}

		if(result == null)
			throw new InvalidAddressException(id + " is not registered!");
		else
			return result.toString();
	}

	@Override
	public List<String> getBetween(List<GetBetweenOP> ops) {
		List<String> ids = new ArrayList<>();

		List<GetBetweenOP> homo_add_list = new ArrayList<>();
		Map<String, BigInteger> aux_homo_add_vars = new HashMap<>();
		
		for(GetBetweenOP op : ops) {
			switch(op.type) {
			case HOMO_ADD:
				aux_homo_add_vars.put(op.id, this.homo_add_variables.get(op.id));
				homo_add_list.add(op);
				break;
			case HOMO_OPE_INT:
				long lower_value = Long.parseLong(op.low_value);
				long higher_value = Long.parseLong(op.high_value);

				long encrypted_value = this.homo_ope_int_variables.get(op.id);
				
				if( lower_value <= encrypted_value && encrypted_value <= higher_value ) {
					ids.add(op.id);
				}
				break;
			case WALLET:
				int lower_value_int = Integer.parseInt(op.low_value);
				int higher_value_int = Integer.parseInt(op.high_value);
				
				int value = (int) this.accounts.get(op.id).doubleValue();
				
				if( lower_value_int <= value && value <= higher_value_int ) {
					ids.add(op.id);
				}
				break;
			}
		}
		
		System.out.println("ola");
		
		if(homo_add_list.size() > 0) {
			ids.addAll(secureModule.getBetweenHomoAdd(aux_homo_add_vars, homo_add_list));
		}
		
		System.out.println("adeus");

		return ids;
	}

	@Override
	public boolean compare(DataType cond_type, String cond_key, ConditionalOperation cond, String cond_val, String cipheredKey)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException {

		BigInteger c_val = new BigInteger(cond_val);

		String value = get(cond_type, cond_key);

		switch (cond_type) {
		case WALLET:
			return ConditionParser.evaluate(cond, c_val, Integer.parseInt(value));
		case HOMO_OPE_INT:
			return ConditionParser.evaluate(cond, c_val, Long.parseLong(value));
		case HOMO_ADD:
			return secureModule.compareHomoAdd(c_val, new BigInteger(value), cipheredKey, cond);
		default:
			throw new InvalidTypeException(cond_type + " is not a valid type!");
		}
	}

	@Override
	public boolean set(DataType type, String id, String upd_val) throws InvalidTypeException {

		switch (type) {
		case WALLET:
			accounts.put(id, (double) Integer.parseInt(upd_val));
			return true;
		case HOMO_OPE_INT:
			homo_ope_int_variables.put(id, Long.parseLong(upd_val));
			return true;
		case HOMO_ADD:
			homo_add_variables.put(id, new BigInteger(upd_val));
			return true;
		default:
			throw new InvalidTypeException(type + " is not a valid type!");
		}
	}

	@Override
	public String sum(DataType upd_type, String upd_id, String upd_val, String upd_auxArg) throws InvalidAddressException, InvalidTypeException {

		String value = get(upd_type, upd_id);

		switch (upd_type) {
		case WALLET:			
			Double d_aux = (double)(Integer.parseInt(upd_val) + Integer.parseInt(value));

			accounts.put(upd_id, d_aux);

			return "" + d_aux.intValue();
		case HOMO_OPE_INT:
			Long l_aux = secureModule.addOPI(Long.parseLong(value), Long.parseLong(upd_val), upd_auxArg);
			homo_ope_int_variables.put(upd_id, l_aux);

			return l_aux.toString();
		case HOMO_ADD:
			BigInteger b_aux = HomoAdd.sum(new BigInteger(value), new BigInteger(upd_val), new BigInteger(upd_auxArg));

			homo_add_variables.put(upd_id, b_aux);
			
			return b_aux.toString();
		default:
			throw new InvalidTypeException(upd_type + " is not a valid type!");
		}
	}

	@Override
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, String cond_val,
			String cond_cipheredKey, List<UpdOp> ops)
					throws InvalidAddressException, InvalidTypeException, InvalidOperationException {

		boolean cmp = compare(cond_type, cond_id, cond, cond_val, cond_cipheredKey);
		if (cmp) {
			for(UpdOp op : ops) {
				switch(op.op) {
				case SUM:
					sum(op.upd_type, op.upd_id, op.upd_value, op.auxArg);
					break;
				case SET:
					set(op.upd_type, op.upd_id, op.upd_value);
					break;
				default:
					throw new InvalidOperationException(op.op.name() + " is an invalid operation!");
				}
			}
		}

		return cmp;
	}

}
