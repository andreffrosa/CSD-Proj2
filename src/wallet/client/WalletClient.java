package wallet.client;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.AbstractMap;
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
import utils.Bytes;
import utils.Cryptography;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

/**
 * Handles the generation and management of a set of public and private
 * addresses belonging to the same person.
 */

public class WalletClient {

	private static final int DEFAULT_SIZE = 10;

	private Map<String, String> to_receive_addresses; // PK -> (SK, €)
	private Map<String, Entry<String, Double>> used_addresses; // PK -> (SK, €)

	private Map<String, Long> opi_variables; // id -> key
	private Map<String, PaillierKey> sumInt_variables; // id -> key

	private Wallet wallet;
	
	private SecretKey secureModule_ks = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, SecureModuleImpl.CIPHER_ALGORITHM);

	// Constructor
	public WalletClient(String[] servers) {
		to_receive_addresses = new HashMap<>(DEFAULT_SIZE);
		used_addresses = new HashMap<>(DEFAULT_SIZE);

		opi_variables = new HashMap<>(DEFAULT_SIZE);
		sumInt_variables = new HashMap<>(DEFAULT_SIZE);

		wallet = new RESTWalletClient(servers);
	}

	public String generateNewAddress() {
		KeyPair kp = Cryptography.genKeys();
		String pubKey = Cryptography.getPublicKey(kp);
		String privKey = Cryptography.getPrivateKey(kp);

		to_receive_addresses.put(pubKey, privKey);

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

		return used_address;
	}

	public boolean createVariable(String type, String id, int value) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		boolean result = false;
		if(type.equals("SumInt")) {
			PaillierKey pk = HomoAdd.generateKey();

			sumInt_variables.put(id, pk);

			BigInteger big1 = new BigInteger("" + value);
			BigInteger big1Code;
			try {
				big1Code = HomoAdd.encrypt(big1, pk);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			result = wallet.putSumInt(id, big1Code);
		} else if(type.equals("OPI")) {
			long key = HomoOpeInt.generateKey();
			HomoOpeInt ope = new HomoOpeInt(key);

			opi_variables.put(id, key);

			long opi = ope.encrypt(value);
			result = wallet.putOrderPreservingInt(id, opi);
		} else if(type.equals("wallet")) {
			result = transfer(id, value);
		}

		return result;
	}

	public int getVariable(String type, String id) throws InvalidAddressException {
		int result = -1;

		if(type.equals("SumInt")) {
			PaillierKey pk = sumInt_variables.get(id);

			if(pk == null) {
				throw new InvalidAddressException(id + " is an invalid address!");
			}

			BigInteger encrypted_result = wallet.getSumInt(id);

			try {
				result = HomoAdd.decrypt(encrypted_result, pk).intValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if(type.equals("OPI")) {
			Long key = opi_variables.get(id);

			if(key == null) {
				throw new InvalidAddressException(id + " is an invalid address!");
			}

			HomoOpeInt ope = new HomoOpeInt(key);

			long encrypted_result= wallet.getOrderPreservingInt(id);

			result = ope.decrypt(encrypted_result);
		} else if(type.equals("wallet")) {
			result = (int) wallet.balance(id);
		}

		return result;
	}

	public boolean cond_set(String cond_key_type, String cond_key, int cond_value, String upd_key_type, String upd_key, int upd_value) throws InvalidAddressException, InvalidTypeException {

		String cipheredKey = encryptKey(cond_key_type, cond_key);

		String encrypted_cond_value = encryptValue(cond_key_type, cond_key, cond_value);
		
		String encrypted_upd_value = encryptValue(upd_key_type, upd_key, upd_value);

		return wallet.cond_set(cond_key, cond_key_type, encrypted_cond_value, cipheredKey, upd_key, upd_key_type, encrypted_upd_value);
	}
	
	public boolean cond_add(String cond_key_type, String cond_key, int cond_value, String upd_key_type, String upd_key, int upd_value) throws InvalidAddressException, InvalidTypeException {

		String cond_cipheredKey = encryptKey(cond_key_type, cond_key);

		String encrypted_cond_value = encryptValue(cond_key_type, cond_key, cond_value);
		String encrypted_upd_value = encryptValue(upd_key_type, upd_key, upd_value);

		String upd_auxArg = getAddAuxArg(upd_key_type, upd_key);

		return wallet.cond_add(cond_key, cond_key_type, encrypted_cond_value, cond_cipheredKey, upd_key, upd_key_type, encrypted_upd_value, upd_auxArg);
	}
	
	private String getAddAuxArg(String key_type, String key) throws InvalidAddressException {
		
		String arg = "";
		
		if(key_type.equals("SumInt")) {
			PaillierKey pk = sumInt_variables.get(key);

			if(pk == null) {
				throw new InvalidAddressException(key + " is an invalid address!");
			}
			
			arg = "" + pk.getNsquare();
		} else if(key_type.equals("OPI")) {
			arg = encryptKey(key_type, key);
		} else if(key_type.equals("wallet")) {
			arg = "";
		}
		
		return arg;
	}
	
	private String encryptKey(String key_type, String key) throws InvalidAddressException {
		String encrypted_key = "";
		
		if(key_type.equals("SumInt")) {
			PaillierKey pk = sumInt_variables.get(key);

			if(pk == null) {
				throw new InvalidAddressException(key + " is an invalid address!");
			}
			
			byte[] rawCipheredKey = Cryptography.encrypt(secureModule_ks, HomoAdd.stringFromKey(pk).getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
			encrypted_key = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		} else if(key_type.equals("OPI")) {
			Long opi_key = opi_variables.get(key);

			if(opi_key == null) {
				throw new InvalidAddressException(key + " is an invalid address!");
			}

			byte[] rawCipheredKey = Cryptography.encrypt(secureModule_ks, Bytes.toBytes(opi_key), SecureModuleImpl.CIPHER_ALGORITHM);
			encrypted_key = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		} else if(key_type.equals("wallet")) {
			encrypted_key = "";
		}
		
		return encrypted_key;
	}
	
	private String encryptValue(String key_type, String key, int value) throws InvalidAddressException {
		String encrypted_value = "";
		
		if(key_type.equals("SumInt")) {
			PaillierKey pk = sumInt_variables.get(key);

			if(pk == null) {
				throw new InvalidAddressException(key + " is an invalid address!");
			}
			
			try {
				encrypted_value = HomoAdd.encrypt(new BigInteger("" + value), pk).toString();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if(key_type.equals("OPI")) {
			Long opi_key = opi_variables.get(key);

			if(opi_key == null) {
				throw new InvalidAddressException(key + " is an invalid address!");
			}

			HomoOpeInt ope = new HomoOpeInt(opi_key);

			encrypted_value = "" + ope.encrypt(value);
		} else if(key_type.equals("wallet")) {
			encrypted_value = "" + value;
		}
		
		return encrypted_value;
	}

	// TODO: falta fazer add e compare
	
}
