import rest.RESTWalletClient;
import wallet.Transaction;
import wallet.Wallet;
import wallet.client.WalletClient;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class Evaluation {
	
	private static final String ADMIN_PUB_KEY = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEQOC5YdvESUZnej0W2N00UC7eUsfeEUYWr6y3bQkZPFN3+bzKZxqVRGOEGe7+3rD5";
	private static final String ADMIN_PRIV_KEY = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBgDXK95Al4rQHdvRSTP8D7GfNYMmPq9z02gCgYIKoZIzj0DAQGhNAMyAARA4Llh28RJRmd6PRbY3TRQLt5Sx94RRhavrLdtCRk8U3f5vMpnGpVEY4QZ7v7esPk=";

	public static void main(String[] args) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		Wallet admin_wallet = new RESTWalletClient(new String[] { "https://localhost:8080/", "https://localhost:8081/",
				"https://localhost:8082/", "https://localhost:8083/" });
		
		WalletClient wallet1 = new WalletClient(); 
		WalletClient wallet2 = new WalletClient();
		
		String addr1 = wallet1.generateNewAddress();
		String addr2 = wallet2.generateNewAddress();
		
		int n = 100;
		
		admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr1, n*1.0, ADMIN_PRIV_KEY));
		
		wallet1.checkReception(addr1);
		
		long total_time = 0L;
		
		System.out.println("Init ...");
		for(int i = 0; i < n; i++) {
			long start = System.nanoTime();
			wallet1.transfer(addr2, 1.0);
			long finish = System.nanoTime();
			total_time += finish - start;
			System.out.println("(" + (i+1) + ") Average Transfer Time: " + ((double) (i+1)) / (total_time/(1000*1000*1000))  + " tx/s");
		}
		
		double avg_transfer_time = ((double) n) / (total_time/(1000*1000*1000));
		System.out.println("Average Transfer Time: " + avg_transfer_time + " tx/s");
	}

}
