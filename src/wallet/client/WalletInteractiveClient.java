package wallet.client;

import java.io.Console;
import java.security.KeyPair;
import java.util.Map;
import java.util.Map.Entry;

import rest.RESTWalletClient;
import utils.Cryptography;
import wallet.InvalidNumberException;
import wallet.Wallet;

public class WalletInteractiveClient {

	private static void help() {
		System.out.println("Available operations: ");
		System.out.println("\t-1 : Exit");
		System.out.println("\t 0 : Help");
		//System.out.println("\t 1 : Create Money");
		System.out.println("\t 1 : Transfer");
		System.out.println("\t 2 : Current Money");
	}

	public static void main(String[] args) {

		Wallet wallet = new RESTWalletClient(new String[] {"https://localhost:8080/", "https://localhost:8081/", "https://localhost:8082/", "https://localhost:8083/"});
		Console console = System.console();
		
		
		// temp
		KeyPair kp = Cryptography.genKeys();
		String privateKey = Cryptography.getPrivateKey(kp);
		String publicKey = Cryptography.getPublicKey(kp);

		System.out.println("privateKey: " + privateKey);
		System.out.println("publicKey: " + publicKey);
		
		boolean exit = false;

		String who;
		double amount, balance;

		help();

		while(!exit) {
			/*System.out.println("Available operations: ");
			System.out.println("-1 - Exit");
			System.out.println(" 0 - Help");
			System.out.println(" 1 - Create Money");
			System.out.println(" 2 - Transfer");
			System.out.println(" 3 - Current Money");*/
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
				/*case 1:
					System.out.println("\tCreate Money");
					who = console.readLine("\twho: ").trim();
					amount = Double.parseDouble(console.readLine("\tamount: ").trim());
					balance = wallet.createMoney(who, amount);
					System.out.println("--> Balance: " + balance);
					break;*/
				case 1:
					System.out.println("\tTransfer");
					String from = console.readLine("\tfrom: ").trim();
					String to = console.readLine("\tto: ").trim();
					amount = Double.parseDouble(console.readLine("\tamount: ").trim());
					String signature = Cryptography.sign((from+to+amount).getBytes(), privateKey);
					System.out.println("\tsignature: " + signature);
					//String signature = console.readLine("\tsignature: ").trim();
					boolean status =  wallet.transfer(from, to, amount, signature);
					System.out.println("--> Status: " + status);
					break;
				case 2:
					System.out.println("\tCurrent Money");
					who = console.readLine("\twho: ").trim();
					balance =  wallet.balance(who);
					System.out.println("--> Balance: " + balance + "€");
					break;
				case 3:
					System.out.println("\tLedger");
					Map<String, Double> ledger =  wallet.ledger();
					for( Entry<String, Double> e : ledger.entrySet() ) {
						System.out.println("\t" + e.getKey() + " : " + e.getValue() + "€");
					}
					//System.out.println("--> Balance: " + balance);
					break;
				default:
					System.out.println("ERROR: " + cmd + " is not a valid operation!");
					break;
				}

			} catch (NumberFormatException e) {
				System.out.println("ERROR: Only numbers are allowed!");
			} catch (InvalidNumberException e) {
				System.out.println(e.getMessage());
			}

			//System.out.println("");
		}
	}

}
