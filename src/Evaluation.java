import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

	private static final String[] servers = new String[] { "https://localhost:8080/", "https://localhost:8081/",
			"https://localhost:8082/", "https://localhost:8083/" };

	public static void main(String[] args) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		/*Wallet admin_wallet = new RESTWalletClient(servers);

		WalletClient wallet1 = new WalletClient(servers); 
		WalletClient wallet2 = new WalletClient(servers);

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
		System.out.println("Average Transfer Time: " + avg_transfer_time + " tx/s");*/
		
		System.out.println("Starting evaluation...");
		evaluate(1, 10, 1);
	}
	
	private static void evaluate(int n_threads, long sec_duration, int n_wallets) {
		
		ConcurrentMap<String, String> results = new ConcurrentHashMap<>();
		
		for(int i = 0; i < n_threads; i++) {
			lauchThread(i, sec_duration, n_wallets, results);
		}
		
		processResults(n_threads, results);
	}
	
	private static void processResults(int n_threads, ConcurrentMap<String, String> results) {
		// TODO
	}
	
	private static void lauchThread(int thread_id, long sec_duration, int n_wallets, ConcurrentMap<String, String> results) {
		new Thread( () -> {
			System.out.println("Laucing thread " + thread_id + " ...");
			executeTransfers(thread_id, sec_duration, n_wallets, results);
		}).start();
	}

	private static void executeTransfers(int thread_id, long sec_duration, int n_wallets, ConcurrentMap<String, String> results ) {

		Wallet admin_wallet = new RESTWalletClient(servers);
		
		WalletClient[] wallets = new WalletClient[n_wallets];
		for(int i = 0; i < n_wallets; i++) {
			wallets[i] = new WalletClient(servers);
		}
		
		String[] addresses = new String[n_wallets];
		for(int i = 0; i < n_wallets; i++) {
			addresses[i] = wallets[i].generateNewAddress();
		}

		int counter = 0;
		long total_time = 0L;
		
		try {
			long finish = System.nanoTime() + sec_duration*1000*1000*1000;
			while( System.nanoTime() < finish ) {
				for(int i = 0; i < n_wallets; i++) {
					//String addr = wallets[i].generateNewAddress();
					long start = System.nanoTime();
					admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addresses[i], 1.0, ADMIN_PRIV_KEY));
					total_time += System.nanoTime() - start;
					counter++;
				}
			}
		} catch(InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			e.printStackTrace();
		}
		
		double avg_transfers_second = ((double) counter) / (sec_duration*1000*1000*1000);
		System.out.println("("+thread_id +") " + "Average Transfers per Second: " + avg_transfers_second + " tx/s");
		results.put("("+thread_id +") " + "Average Transfers per Second: ", "" + avg_transfers_second);
		
		double avg_transfer_time = (total_time/(1000*1000*1000)) / ((double) counter);
		System.out.println("("+thread_id +") " + "Average Transfer Time: " + avg_transfer_time + " s");
		results.put("("+thread_id +") " + "Average Transfer Time: ", "" + avg_transfer_time);
	}

}
