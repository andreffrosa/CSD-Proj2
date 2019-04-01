import java.util.Map;
import java.util.Map.Entry;

import rest.RESTWalletClient;
import wallet.Transaction;
import wallet.Wallet;
import wallet.client.WalletClient;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class Test {
	
	private static final String ADMIN_PUB_KEY = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEQOC5YdvESUZnej0W2N00UC7eUsfeEUYWr6y3bQkZPFN3+bzKZxqVRGOEGe7+3rD5";
	private static final String ADMIN_PRIV_KEY = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBgDXK95Al4rQHdvRSTP8D7GfNYMmPq9z02gCgYIKoZIzj0DAQGhNAMyAARA4Llh28RJRmd6PRbY3TRQLt5Sx94RRhavrLdtCRk8U3f5vMpnGpVEY4QZ7v7esPk=";

	public static void main(String[] args) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		//testTransferences();
		
		testExceptions();
		
		System.out.println("All tests passed!");
	}
	
	private static void testExceptions() {
		WalletClient wallet1 = new WalletClient(); 
		WalletClient wallet2 = new WalletClient();
		
		String addr1 = wallet1.generateNewAddress();
		String addr2 = wallet2.generateNewAddress();
		
		Wallet admin_wallet = new RESTWalletClient(new String[] { "https://localhost:8080/", "https://localhost:8081/",
				"https://localhost:8082/", "https://localhost:8083/" });
		
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
		Wallet admin_wallet = new RESTWalletClient(new String[] { "https://localhost:8080/", "https://localhost:8081/",
				"https://localhost:8082/", "https://localhost:8083/" });
		
		WalletClient wallet1 = new WalletClient(); 
		WalletClient wallet2 = new WalletClient();
		
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
