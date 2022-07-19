package wallet.client;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import rest.RESTWalletClient;
import secureModule.SecureModuleImpl;
import utils.Cryptography;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.GetBetweenOP;
import wallet.Transaction;
import wallet.UpdOp;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

/**
 * Handles the generation and management of a set of public and private
 * addresses belonging to the same person.
 */

public class WalletClient implements WalletAPI {

	private static final int DEFAULT_SIZE = 10;

	private Map<String, String> to_receive_addresses; // PK -> (SK, €)
	private Map<String, Entry<String, Double>> used_addresses; // PK -> (SK, €)

	private List<String> wallets;
	
	private Map<String, Long> homo_ope_int_variables; // id -> key
	private Map<String, PaillierKey> homo_add_variables; // id -> key

	private Wallet wallet;

	private SecretKey secureModule_ks = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, SecureModuleImpl.CIPHER_ALGORITHM);

	// Constructor
	public WalletClient(String[] servers) {
		to_receive_addresses = new HashMap<>(DEFAULT_SIZE);
		used_addresses = new HashMap<>(DEFAULT_SIZE);

		wallets = new ArrayList<>(DEFAULT_SIZE);
		
		homo_ope_int_variables = new HashMap<>(DEFAULT_SIZE);
		homo_add_variables = new HashMap<>(DEFAULT_SIZE);

		wallet = new RESTWalletClient(servers);
	}

	public String generateNewAddress() {
		KeyPair kp = Cryptography.genKeys();
		String pubKey = Cryptography.getPublicKey(kp);
		String privKey = Cryptography.getPrivateKey(kp);

		to_receive_addresses.put(pubKey, privKey);
		
		wallets.add(pubKey);

		return pubKey;
	}

	public String getPrivKey(String pubKey) {
		String privKey = to_receive_addresses.get(pubKey);
		if (privKey == null)
			privKey = used_addresses.get(pubKey).getKey();

		return privKey;
	}

	public double checkReception(String pubKey) {
		double balance = wallet.balance(pubKey);

		if (balance > 0.0) {
			String privKey = to_receive_addresses.remove(pubKey);
			if (privKey != null)
				used_addresses.put(pubKey, new AbstractMap.SimpleEntry<String, Double>(privKey, balance));
		}

		return balance;
	}

	public boolean transfer(String to, double amount)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		// Group enough money from multiple addresses
		List<Transaction> transactions = new LinkedList<>();

		Map<String, Entry<String, Double>> temp = new HashMap<>(used_addresses);

		double current_amount = 0;
		for (Entry<String, Entry<String, Double>> e : used_addresses.entrySet()) {
			if (current_amount < amount) {
				String publicKey = e.getKey();
				String privateKey = e.getValue().getKey();
				double balance = e.getValue().getValue();

				double temp_amount = Math.min(amount - current_amount, balance);

				transactions.add(new Transaction(publicKey, to, temp_amount, privateKey));

				double account_new_balance = balance - temp_amount;
				if (account_new_balance > 0)
					temp.put(publicKey, new AbstractMap.SimpleEntry<String, Double>(privateKey, account_new_balance));
				else
					temp.remove(publicKey);

				current_amount += temp_amount;
			} else
				break;
		}

		if (current_amount < amount) {
			throw new NotEnoughMoneyException("All of your wallet's addresses have not enough money");
		}

		boolean status = wallet.atomicTransfer(transactions);

		if (status)
			used_addresses = temp;

		return status;
	}

	public boolean transferFrom(String pubKey, String to, double amount)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		Entry<String, Double> e = used_addresses.get(pubKey);

		if (e != null) {
			String privKey = e.getKey();
			double balance = e.getValue();

			if (balance >= amount) {
				Transaction t = new Transaction(pubKey, to, amount, privKey);

				if (wallet.transfer(t)) {
					double new_balance = balance - amount;
					if (new_balance > 0.0) {
						used_addresses.put(pubKey, new AbstractMap.SimpleEntry<String, Double>(privKey, new_balance));
					} else {
						used_addresses.remove(pubKey);
					}

					return true;
				}
			}
		}

		return false;
	}

	public double getBalance() {
		double balance = 0;
		for (Entry<String, Entry<String, Double>> e : used_addresses.entrySet()) {
			balance += e.getValue().getValue();
		}

		Map<String, String> temp = new HashMap<>(to_receive_addresses);
		for (Entry<String, String> e : temp.entrySet()) {
			double amount = this.checkReception(e.getKey());
			balance += amount;
		}

		return balance;
	}

	public Map<String, Entry<String, Double>> getAllAddresses() {
		Map<String, Entry<String, Double>> addresses = new HashMap<>(
				used_addresses.size() + to_receive_addresses.size());

		addresses.putAll(used_addresses);

		for (Entry<String, String> e : to_receive_addresses.entrySet()) {
			addresses.put(e.getKey(), new AbstractMap.SimpleEntry<String, Double>(e.getValue(), 0.0));
		}

		return addresses;
	}

	public boolean addKeys(String pubKey, String privKey, boolean checkBalance) {

		double balance = checkBalance ? wallet.balance(pubKey) : 0.0;

		boolean used_address = balance > 0.0;

		if (used_address) {
			used_addresses.put(pubKey, new AbstractMap.SimpleEntry<String, Double>(privKey, balance));
		} else {
			to_receive_addresses.put(pubKey, privKey);
		}
		
		wallets.add(pubKey);

		return used_address;
	}

	@Override
	public boolean create(DataType type, String id, int initial_value) throws InvalidAddressException {

		String encrypted_value = "";

		switch(type) {
		case HOMO_ADD:
			PaillierKey pk = HomoAdd.generateKey();

			homo_add_variables.put(id, pk);

			BigInteger big1 = new BigInteger("" + initial_value);
			BigInteger big1Code;
			try {
				big1Code = HomoAdd.encrypt(big1, pk);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			encrypted_value = big1Code.toString();
			break;
		case HOMO_OPE_INT:
			long key = HomoOpeInt.generateKey();
			HomoOpeInt ope = new HomoOpeInt(key);

			homo_ope_int_variables.put(id, key);

			encrypted_value = ((Long)ope.encrypt(initial_value)).toString();
			break;
		case WALLET:
			encrypted_value = Integer.toString(initial_value);
			wallets.add(id);
			break;
		}

		return wallet.create(type, id, encrypted_value);
	}

	@Override
	public int get(DataType type, String id) throws InvalidAddressException, InvalidTypeException {
		String encrypted_result = wallet.get(type, id);
		return decryptValue(type, getKey(type, id), encrypted_result);
	}

	public List<String> get(String id_prefix, int lower_value, int higher_value) throws InvalidAddressException, InvalidTypeException {

		List<GetBetweenOP> ops = new LinkedList<>();

		DataType type = DataType.HOMO_OPE_INT;
		for(Entry<String,Long> e : this.homo_ope_int_variables.entrySet()) {
			String current_id = e.getKey();
			if(current_id.startsWith(id_prefix)) {
				
				String key = getKey(type, current_id);
				ops.add(new GetBetweenOP(type, current_id, encryptValue(type, key, lower_value), encryptValue(type, key, higher_value), encryptForSecureModule(type, key)));
			}
		}

		type = DataType.HOMO_ADD;
		String lower = encryptForSecureModule(type, ""+lower_value);
		String higher = encryptForSecureModule(type, ""+higher_value);
		for(Entry<String,PaillierKey> e : this.homo_add_variables.entrySet()) {
			String current_id = e.getKey();
			if(current_id.startsWith(id_prefix)) {
				String key = getKey(type, current_id);
				//ops.add(new GetBetweenOP(type, current_id, encryptValue(type, key, lower_value), encryptValue(type, key, higher_value), encryptForSecureModule(type, key)));
				ops.add(new GetBetweenOP(type, current_id, lower, higher, encryptForSecureModule(type, key)));
			}
		}

		type = DataType.WALLET;
		for(String current_id : wallets) {
			if(current_id.startsWith(id_prefix)) {
				ops.add(new GetBetweenOP(type, current_id, "" + lower_value, "" + higher_value, ""));
			}
		}

		return wallet.getBetween(ops);
	}

	@Override
	public boolean set(DataType type, String id, int value) throws InvalidAddressException, InvalidTypeException {
		String encrypted_value = encryptValue(type, getKey(type, id), value);
		return wallet.set(type, id, encrypted_value);
	}

	public int sum(DataType type, String id, int value) throws InvalidAddressException, InvalidTypeException {
		String key = getKey(type, id);

		String result = wallet.sum(type, id, encryptValue(type, key, value), getAuxArg(type, id));

		return decryptValue(type, key, result);
	}

	@Override
	public boolean compare(DataType type, String id, ConditionalOperation op, int value) throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		String key = getKey(type, id);

		return wallet.compare(type, id, op, encryptValue(type, key, value), encryptForSecureModule(type, key));
	}

	@Override
	public boolean cond_upd(DataType cond_type, String cond_id, ConditionalOperation cond, int cond_val, List<UpdOp> ops) throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		
		String cond_key = getKey(cond_type, cond_id);
		
		for(UpdOp op : ops) {
			op.auxArg = getAuxArg(op.upd_type, op.upd_id);
			
			op.upd_value = encryptValue(op.upd_type, getKey(op.upd_type, op.upd_id), op.upd_value_unciphered);
			
			op.upd_value_unciphered = 0;
		}
		
		return wallet.cond_upd(cond_type, cond_id, cond, encryptValue(cond_type, cond_key, cond_val), encryptForSecureModule(cond_type, cond_key), ops);
	}

	private String getAuxArg(DataType type, String id) throws InvalidAddressException {

		switch(type) {
		case HOMO_ADD:
			PaillierKey pk = homo_add_variables.get(id);

			if(pk == null) {
				throw new InvalidAddressException(id + " is an invalid address!");
			}

			return pk.getNsquare().toString();
		case HOMO_OPE_INT:
			return encryptForSecureModule(type, getKey(type, id));
		case WALLET:
			return "";
		}

		return null;
	}
	
	private String encryptForSecureModule(DataType type, String value) throws InvalidAddressException {

		byte[] rawCipheredKey;
		switch(type) {
		case HOMO_ADD:
			rawCipheredKey = Cryptography.encrypt(secureModule_ks, value.getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
			return java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		case HOMO_OPE_INT:
			rawCipheredKey = Cryptography.encrypt(secureModule_ks, value.getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
			return java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		case WALLET:
			return "";
		}

		return null;
	}

	private String getKey(DataType type, String id) throws InvalidAddressException {

		switch(type) {
		case HOMO_ADD:
			PaillierKey pk = homo_add_variables.get(id);

			if(pk == null) {
				throw new InvalidAddressException(id + " is an invalid address!");
			}
			
			return HomoAdd.stringFromKey(pk);
		case HOMO_OPE_INT:
			Long opi_key = homo_ope_int_variables.get(id);

			if(opi_key == null) {
				throw new InvalidAddressException(id + " is an invalid address!");
			}

			return opi_key.toString();
		case WALLET:
			return "";
		}

		return null;
	}

	private String encryptValue(DataType type, String key, int value) throws InvalidAddressException, InvalidTypeException {

		switch(type) {
		case HOMO_ADD:
			PaillierKey pk = HomoAdd.keyFromString(key);

			try {
				return HomoAdd.encrypt(new BigInteger("" + value), pk).toString();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		case HOMO_OPE_INT:
			Long opi_key = Long.parseLong(key);

			HomoOpeInt ope = new HomoOpeInt(opi_key);

			return "" + ope.encrypt(value);
		case WALLET:
			return "" + value;

		}

		throw new InvalidTypeException(type.toString() + " is an invalid type!");
	}

	private int decryptValue(DataType type, String key, String encrypted_value) throws InvalidAddressException, InvalidTypeException {

		switch(type) {
		case HOMO_ADD:
			PaillierKey pk = HomoAdd.keyFromString(key);

			try {
				return HomoAdd.decrypt(new BigInteger("" + encrypted_value), pk).intValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		case HOMO_OPE_INT:
			Long opi_key = Long.parseLong(key);

			HomoOpeInt ope = new HomoOpeInt(opi_key);

			return (int) ope.decrypt(Long.parseLong(encrypted_value));
		case WALLET:
			return Integer.parseInt(encrypted_value);
		}

		throw new InvalidTypeException(type.toString() + " is an invalid type!");
	}

}
