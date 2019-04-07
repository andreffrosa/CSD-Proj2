package test;
import java.util.Map;
import java.util.Map.Entry;

import rest.RESTWalletClient;
import utils.Cryptography;
import utils.IO;
import wallet.Transaction;
import wallet.Wallet;
import wallet.client.WalletClient;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class Test {
	
	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static final String[] servers = (String[]) IO.loadObject("./servers.json", String[].class);
	
	public static void main(String[] args) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		testTransferences();
		
		testExceptions();
		
		System.out.println("All tests passed!");
	}
	
	private static void testExceptions() {
		WalletClient wallet1 = new WalletClient(servers); 
		WalletClient wallet2 = new WalletClient(servers);
		
		String addr1 = wallet1.generateNewAddress();
		String addr2 = wallet2.generateNewAddress();
		
		Wallet admin_wallet = new RESTWalletClient(servers);
		
		try {
			boolean status = admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, -100.0, ADMIN_PRIV_KEY));
			System.out.println(status);
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Invalid Amount : " + e.getMessage());
		}
		
		admin_wallet.ledger();
		
		try {
			admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, 0, ADMIN_PRIV_KEY));
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Invalid Amount : " + e.getMessage());
		}
		try {
			admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, "goku", 100.0, ADMIN_PRIV_KEY));
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Invalid Adress : " + e.getMessage());
		}
		
		try {
			admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, 100.0, ADMIN_PRIV_KEY));
			admin_wallet.transfer(new Transaction(addr1, addr2, 1000.0, wallet1.getPrivKey(addr1)));
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Not enough money : " + e.getMessage());
		}
		
		try {
			admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, 100.0, ADMIN_PRIV_KEY));
			wallet1.checkReception(addr1);
			wallet1.transfer(addr2, 1000.0);
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Not enough money : " + e.getMessage());
		}
		
		try {
			admin_wallet.transfer(new Transaction(addr1, addr2, 100.0, "ola", true));
			//wallet1.checkReception(addr1);
			//wallet1.transfer(addr2, 1000.0);
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Invalid Signature: " + e.getMessage());
		}
		
		//admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr2, 100.0, ADMIN_PRIV_KEY));
	}

	private static void testTransferences() throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		Wallet admin_wallet = new RESTWalletClient(servers);
		
		WalletClient wallet1 = new WalletClient(servers); 
		WalletClient wallet2 = new WalletClient(servers);
		
		String addr1 = wallet1.generateNewAddress();
		String addr2 = wallet2.generateNewAddress();
		
		admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, 100.0, ADMIN_PRIV_KEY));
		admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr2, 100.0, ADMIN_PRIV_KEY));
		
		double balance = wallet1.checkReception(addr1);
		System.out.println("Wallet1: " + balance + "€");
		
		balance = wallet2.checkReception(addr2);
		System.out.println("Wallet2: " + balance + "€");
		
		String addr3 = wallet1.generateNewAddress();
		String addr4 = wallet2.generateNewAddress();
		
		admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr3, 50.0, ADMIN_PRIV_KEY));
		admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr4, 50.0, ADMIN_PRIV_KEY));
		
		balance = wallet1.getBalance();
		System.out.println("Wallet1: " + balance + "€");
		
		balance = wallet2.getBalance();
		System.out.println("Wallet2: " + balance + "€");
		
		String addr5 = wallet1.generateNewAddress();
		
		boolean status = wallet2.transfer(addr5, 125.0);
		System.out.println("Transfer: " + status);
		
		balance = wallet1.getBalance();
		System.out.println("Wallet1: " + balance + "€");
		
		balance = wallet2.getBalance();
		System.out.println("Wallet2: " + balance + "€");
		
		System.out.println("Wallet1 addresses'");
		Map<String, Entry<String, Double>> addresses =  wallet1.getAllAddresses();
		for( Entry<String, Entry<String, Double>> e : addresses.entrySet() ) {
			System.out.println("\t" + e.getKey() + " : " + e.getValue().getKey() + " : " + e.getValue().getValue()  + "€");
		}
		
		System.out.println("Wallet2 addresses'");
		addresses =  wallet2.getAllAddresses();
		for( Entry<String, Entry<String, Double>> e : addresses.entrySet() ) {
			System.out.println("\t" + e.getKey() + " : " + e.getValue().getKey() + " : " + e.getValue().getValue()  + "€");
		}
		
		System.out.println("Ledger");
		Map<String, Double> ledger =  admin_wallet.ledger();
		for( Entry<String, Double> e : ledger.entrySet() ) {
			System.out.println("\t" + e.getKey() + " : " + e.getValue() + "€");
		}
	}

}
