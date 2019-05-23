package wallet.client;

import java.io.Console;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rest.RESTWalletClient;
import utils.Cryptography;
import utils.IO;
import wallet.Transaction;
import wallet.Wallet;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

/**
 * Implements an interactive client used for testing purposes.
 */

public class WalletInteractiveClient {

	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static void help() {
		System.out.println("Available operations: ");
		System.out.println("\t-1 : Exit");
		System.out.println("\t 0 : Help");
		// System.out.println("\t 1 : Create Money");
		System.out.println("\t 1 : Transfer");
		System.out.println("\t 2 : AtomicTransfer");
		System.out.println("\t 3 : Current Money");
		System.out.println("\t 4 : Check Ledger");
	}

	public static void main(String[] args) {

		Wallet wallet = new RESTWalletClient((String[]) IO.loadObject("./servers.json", String[].class));
		Console console = System.console();

		KeyPair kp = Cryptography.genKeys();
		String privateKey = Cryptography.getPrivateKey(kp);
		String publicKey = Cryptography.getPublicKey(kp);

		System.out.println("privateKey: " + privateKey);
		System.out.println("publicKey: " + publicKey);

		boolean exit = false;

		String who;
		double amount, balance;

		help();

		while (!exit) {

			try {
				System.out.println("");

				int cmd = Integer.parseInt(console.readLine("Select an Option: "));

				switch (cmd) {
				case -1:
					exit = true;
					break;
				case 0:
					System.out.println("");
					help();
					break;
				/*
				 * case 1: System.out.println("\tCreate Money"); who =
				 * console.readLine("\twho: ").trim(); amount =
				 * Double.parseDouble(console.readLine("\tamount: ").trim()); balance =
				 * wallet.createMoney(who, amount); System.out.println("--> Balance: " +
				 * balance); break;
				 */
				case 1:
					System.out.println("\tTransfer");
					String from = console.readLine("\tfrom: ").trim();
					String to = console.readLine("\tto: ").trim();
					amount = Double.parseDouble(console.readLine("\tamount: ").trim());
					String key = from.equals(ADMIN_PUB_KEY) ? ADMIN_PRIV_KEY : privateKey;
					Transaction t = new Transaction(from, to, amount, id);
					System.out.println("\tsignature: " + t.getSignature());
					boolean status = wallet.transfer(t);
					System.out.println("--> Status: " + status);
					break;
				case 2:
					System.out.println("\tAtomic Transfer");
					int n_tx = Integer.parseInt(console.readLine("\tnumber of transactions: ").trim());
					List<Transaction> transactions = new ArrayList<>(n_tx);
					for (int i = 0; i < n_tx; i++) {
						from = console.readLine("\tfrom: ").trim();
						to = console.readLine("\tto: ").trim();
						amount = Double.parseDouble(console.readLine("\tamount: ").trim());
						id = from.equals(ADMIN_PUB_KEY) ? ADMIN_PRIV_KEY : privateKey;
						t = new Transaction(from, to, amount, id);
						transactions.add(t);
						System.out.println("\tsignature: " + t.getSignature());
					}
					status = wallet.atomicTransfer(transactions);
					System.out.println("--> Status: " + status);
					break;
				case 3:
					System.out.println("\tCurrent Money");
					who = console.readLine("\twho: ").trim();
					balance = wallet.balance(who);
					System.out.println("--> Balance: " + balance + "€");
					break;
				case 4:
					System.out.println("\tLedger");
					Map<String, Double> ledger = wallet.ledger();
					for (Entry<String, Double> e : ledger.entrySet()) {
						System.out.println("\t" + e.getKey() + " : " + e.getValue() + "€");
					}
					break;
				default:
					System.out.println("ERROR: " + cmd + " is not a valid operation!");
					break;
				}

			} catch (NumberFormatException e) {
				System.out.println("ERROR: Only numbers are allowed in that field!");
			} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException
					| NotEnoughMoneyException e1) {
				System.out.println(e1.getMessage());
			}
		}
	}

}
