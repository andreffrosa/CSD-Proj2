package wallet.client;

import java.security.KeyPair;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rest.RESTWalletClient;
import utils.Cryptography;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

/**
 * Handles the generation and management of a set of public and private
 * addresses belonging to the same person.
 */

public class WalletClient {

	private Map<String, String> to_receive_addresses; // PK -> (SK, €)
	private Map<String, Entry<String, Double>> used_addresses; // PK -> (SK, €)

	private Wallet wallet;

	// Constructor
	public WalletClient(String[] servers) {
		to_receive_addresses = new HashMap<>();
		used_addresses = new HashMap<>();

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

}
