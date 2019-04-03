import java.io.IOException;
import java.util.Map.Entry;
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
		
		/*if(args.length < 3) {
			System.err.println("Usage: <n_threads> <sec_duration> <n_wallets_per_thread>");
		}*/
		
		int n_threads = 1;
		long sec_duration = 60;
		int n_wallets = 1;
		
		if(args.length >= 1)
			n_threads = Integer.parseInt(args[0]);
		
		if(args.length >= 2)
			sec_duration = Long.parseLong(args[1]);
		
		if(args.length == 3)
			n_wallets = Integer.parseInt(args[2]);
		
		System.out.println("n_threads: " + n_threads);
		System.out.println("duration: " + sec_duration + " s");
		System.out.println("n_wallets: " + n_wallets);
		
		System.out.println("Starting evaluation...");
		
		evaluate(n_threads, sec_duration, n_wallets);
	}
	
	private static void evaluate(int n_threads, long sec_duration, int n_wallets) {
		
		ConcurrentMap<String, String> results = new ConcurrentHashMap<>();
		
		Thread[] threads = new Thread[n_threads];
		
		for(int i = 0; i < n_threads; i++) {
			threads[i] = lauchThread(i, sec_duration, n_wallets, results);
		}
		
		for(int i = 0; i < n_threads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		processResults(n_threads, results);
	}
	
	private static void processResults(int n_threads, ConcurrentMap<String, String> results) {
		// TODO
		double total_transfers = 0.0;
		double avg_transfers_second = 0.0;
		double avg_transfer_time = 0.0;
		
		for(Entry<String, String> e : results.entrySet()) {
			String key = e.getKey();
			String result = e.getValue();
			
			if(key.contains("Total Transfers")) {
				total_transfers += Long.parseLong(result);
			} else if(key.contains("Average Transfers per Second")) {
				avg_transfers_second += Double.parseDouble(result);
			} else if(key.contains("Average Transfer Time")) {
				avg_transfer_time += Double.parseDouble(result);
			}
		}
		
		//total_transfers /= n_threads;
		avg_transfers_second /= n_threads;
		avg_transfer_time /= n_threads;
		
		System.out.println("Total Transfers: " + total_transfers + " tx");
		System.out.println("Total Average Transfers: " + total_transfers / n_threads + " tx");
		System.out.println("Average Transfers per Second: " + avg_transfers_second + " tx/s");
		System.out.println("Average Transfer Time: " + avg_transfer_time + " s");
	}
	
	private static Thread lauchThread(int thread_id, long sec_duration, int n_wallets, ConcurrentMap<String, String> results) {
		Thread t = new Thread( () -> {
			System.out.println("Lauching thread " + thread_id + " ...");
			executeTransfers(thread_id, sec_duration, n_wallets, results);
		});
		t.start();
		return t;
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
				
				if(counter % 15 == 0) {
					//long elapsed_seconds = sec_duration*1000*1000*1000 - (finish - System.nanoTime());
					int progress = 100 - (int) Math.round((((double)(finish - System.nanoTime())) / (sec_duration*1000*1000*1000))*100.0);
					System.out.println("("+thread_id +") Progress: " + progress + " %");
				}
			}
		} catch(InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			e.printStackTrace();
		}
		
		System.out.println("("+thread_id +") " + "Total Transfers: " + counter + " tx");
		results.put("("+thread_id +") " + "Total Transfers: ", "" + counter);
		
		double avg_transfers_second = ((double) counter) / sec_duration;
		System.out.println("("+thread_id +") " + "Average Transfers per Second: " + avg_transfers_second + " tx/s");
		results.put("("+thread_id +") " + "Average Transfers per Second: ", "" + avg_transfers_second);
		
		double avg_transfer_time = (total_time/(1000*1000*1000)) / ((double) counter);
		System.out.println("("+thread_id +") " + "Average Transfer Time: " + avg_transfer_time + " s");
		results.put("("+thread_id +") " + "Average Transfer Time: ", "" + avg_transfer_time);
	}

}
