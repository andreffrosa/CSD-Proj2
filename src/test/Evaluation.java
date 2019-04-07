package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

public class Evaluation {

	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static final String[] servers = (String[]) IO.loadObject("./servers.json", String[].class);
	
	private static final Wallet admin_wallet = new RESTWalletClient(servers);

	private static final long s_to_ns = 1000*1000*1000;

	private static int n_runs = 1;
	private static int n_threads = 6;
	private static long sec_duration = 60;
	private static int n_wallets = 1;
	private static double initial_transfer_ratio = 0.0;
	private static int n_exps = 3;
	//private static double transfer_ratio_step = 0.25;
	private static double transfer_ratio_step = (1.0 - initial_transfer_ratio) / ((double) (n_exps - 1));

	private static int progress_fraction = 20;
	
	private static String output_folder = "./tmp/";

	public static void main(String[] args) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {
		
		parseArgs(args);
		
		System.out.println("n_exps: " + n_exps);
		System.out.println("n_runs: " + n_runs);
		System.out.println("n_threads: " + n_threads);
		System.out.println("n_wallets: " + n_wallets);
		
		System.out.println("duration: " + sec_duration + " s");
		
		System.out.println("initial_transfer_ratio: " + initial_transfer_ratio);
		System.out.println("transfer_ratio_step: " + transfer_ratio_step);
		
		System.out.println("progres_fraction: " + progress_fraction);

		System.out.println("output_folder: " + output_folder);
		
		try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
		admin_wallet.ledger();

		System.out.println("Starting evaluation...");

		evaluate();
	}

	private static void parseArgs(String[] args) {
		String usage = "Usage: Evaluation [options] \n"
				 + "Options: \n"
				 + "\t -help : dsiplay this menu \n"
				 + "\t -t <n_threads> : maximum number of threads \n"
				 + "\t -d <sec_duration> : duration of each run, in seconds \n"
				 + "\t -w <n_wallets> : number of wallets of each thread \n"
				 + "\t -e <n_exps> : number of experiments (different transacions ratios) \n"
				 + "\t -r <n_runs> : number of runs of each experiment to average the results \n" 
				 + "\t -i <initial_transfer_ratio> : initial transfers-balances ratio \n"
		 		 + "\t -p <progress_fraction> : interval of progress to display the curretn progress\n" 
		 		 + "\t -o <output_folder> : output folder for the results' files \n"
		 		 + "\n";
		
		try {
		for(int i = 0; i < args.length; i++) {
			
			switch(args[i]) {
				case "-t": n_threads = Integer.parseInt(args[i+1]);	break;
				case "-d": sec_duration = Long.parseLong(args[i+1]); break;
				case "-w": n_wallets = Integer.parseInt(args[i+1]); break;
				case "-e": n_exps = Integer.parseInt(args[i+1]); break;
				case "-r": n_runs = Integer.parseInt(args[i+1]); break;
				case "-i": initial_transfer_ratio = Double.parseDouble(args[i+1]); break;
				case "-p": progress_fraction = Integer.parseInt(args[i+1]); break;
				case "-o": output_folder = args[i+1]; break;
				case "-help" : 
					System.out.println(usage);
					System.exit(0);
			}
		}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(usage);
		}
	}

	private static void evaluate() {

		// exp, threads, value
		Map<String, String[]> throughput = new HashMap<>(n_exps*4);
		Map<String, String[]> latency = new HashMap<>(n_exps*4);

		for(int exp = 1; exp <= n_exps; exp++) {
			double current_transfer_ratio = initial_transfer_ratio + (exp-1)*transfer_ratio_step;
			
			String[] current_exp_throughput = new String[n_threads];
			String[] current_exp_latency = new String[n_threads];
			
			System.out.println("\n\t Exp " + exp + "/" + n_exps
							 + "\n\t current_transfer_ratio: " + current_transfer_ratio
							 );

			//Map<String, String> exp_results = new HashMap<>(n_runs*4);

			for(int t = 1; t <= n_threads; t++) {
				Map<String, String> exp_results = new HashMap<>(n_runs*4);
				
				Map<String, String> run_results = new HashMap<>(n_runs*4);
				for(int r = 1; r <= n_runs; r++) {
					ConcurrentMap<String, String> thread_results = new ConcurrentHashMap<>(t*4);

					Thread[] threads = new Thread[t];

					for(int i = 0; i < t; i++) {
						threads[i] = lauchThread(i, r, exp, t, current_transfer_ratio, thread_results);
					}

					for(int i = 0; i < t; i++) {
						try {
							threads[i].join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					processThreadsResults(r, exp, t, thread_results, run_results);
				}
				
				processRunResults(exp, n_runs, run_results, exp_results);
				
				//double total_transfers = 0.0;
				double total_avg_transfers = 0.0;
				double avg_transfers_second = 0.0;
				double avg_transfer_time = 0.0;

				//double total_balances = 0.0;
				double total_avg_balances = 0.0;
				double avg_balances_second = 0.0;
				double avg_balance_time = 0.0;
				
				//total_transfers = Double.parseDouble(exp_results.get("{" + exp + "} Total Transfers"));
				total_avg_transfers = Double.parseDouble(exp_results.get("{" + exp + "} Total Average Transfers"));
				avg_transfers_second = Double.parseDouble(exp_results.get("{" + exp + "} Average Transfers per Second"));
				avg_transfer_time = Double.parseDouble(exp_results.get("{" + exp + "} Average Transfer Time"));

				//total_balances = Double.parseDouble(exp_results.get("{" + exp + "} Total Balances"));
				total_avg_balances = Double.parseDouble(exp_results.get("{" + exp + "} Total Average Balances"));
				avg_balances_second = Double.parseDouble(exp_results.get("{" + exp + "} Average Balances per Second"));
				avg_balance_time = Double.parseDouble(exp_results.get("{" + exp + "} Average Balance Time"));
				
				avg_transfer_time = (Double.isNaN(avg_transfer_time)) ? 0.0 : avg_transfer_time;
				avg_balance_time = (Double.isNaN(avg_balance_time)) ? 0.0 : avg_balance_time;
				
				// avg_ops_sec = %transfers*avg_transfer_sec + %balances*avg_balances_sec
				double transfer_ratio = total_avg_transfers / (total_avg_transfers + total_avg_balances);
				//current_exp_throughput[t-1] = "" + (ratio_transfer*avg_transfers_second + (1.0 - ratio_transfer)*avg_balances_second);
				
				current_exp_throughput[t-1] = "" + (avg_transfers_second + avg_balances_second);
				current_exp_latency[t-1] = "" + (transfer_ratio*avg_transfer_time + (1.0 - transfer_ratio)*avg_balance_time);
			}
			
			String label = "T-" + (int)Math.round(current_transfer_ratio*100);
			throughput.put(label, current_exp_throughput);
			latency.put(label, current_exp_latency);
		}
		
		storeResults(throughput, output_folder + "throughput.csv");
		storeResults(latency, output_folder + "latency.csv");
		
		System.out.println("\nThroughput");
		for( Entry<String, String[]> e : throughput.entrySet() ) {
			String[] v = e.getValue();
			String values = "[" + v[0];
			for(int i = 1; i < v.length; i++) {
				values += ", " + v[i];
			} 
			values += "]";
			System.out.println(e.getKey() + ": " + values);
		}
		System.out.println("");
		
		System.out.println("\nlatency");
		for( Entry<String, String[]> e : latency.entrySet() ) {
			String[] v = e.getValue();
			String values = "[" + v[0];
			for(int i = 1; i < v.length; i++) {
				values += ", " + v[i];
			} 
			values += "]";
			System.out.println(e.getKey() + ": " + values);
		}

		// temp
		/*for( Entry<String, String> e : results.entrySet() ) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}*/
	}

	private static Thread lauchThread(int thread_id, int run, int exp, int n_threads, double current_transfer_ratio, ConcurrentMap<String, String> results) {
		Thread t = new Thread( () -> {
			System.out.println("Lauching thread " + thread_id + " ...");
			executeOperation(thread_id, run, exp, n_threads, current_transfer_ratio, results);
		});
		t.start();
		return t;
	}

	private static void executeOperation(int thread_id, int run, int exp, int n_threads, double current_transfer_ratio, ConcurrentMap<String, String> results ) {

		WalletClient[] wallets = new WalletClient[n_wallets];
		for(int i = 0; i < n_wallets; i++) {
			wallets[i] = new WalletClient(servers);
		}

		String[] addresses = new String[n_wallets];
		for(int i = 0; i < n_wallets; i++) {
			addresses[i] = wallets[i].generateNewAddress();
		}

		int transaction_counter = 0;
		int balance_counter = 0;

		long transfer_total_time = 0L;
		long balance_total_time = 0L;

		long finish = System.nanoTime() + sec_duration*s_to_ns;
		long current_time = 0L;

		try {
			int old_progress = 0;
			int progress = 0;
			System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") Progress: 0 %");
			while( (current_time = System.nanoTime()) < finish ) {
				for(int i = 0; i < n_wallets; i++) {
					double r = Math.random();

					long start = 0L; 
					if( r <= current_transfer_ratio ) {
						start = System.nanoTime();
						admin_wallet.transfer(new Transaction(ADMIN_PUB_KEY, addresses[i], 1.0, ADMIN_PRIV_KEY));
						transfer_total_time += System.nanoTime() - start;

						transaction_counter++;
					} else {
						start = System.nanoTime();
						admin_wallet.balance(addresses[i]);
						balance_total_time += System.nanoTime() - start;

						balance_counter++;
					}
				}

				// Print progress every 1/10 from the total duration
				progress = 100 - (int) Math.round((((double)(finish - current_time)) / (sec_duration*s_to_ns))*100.0);
				if( progress - old_progress >= progress_fraction ) {
					old_progress = progress;
					System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") Progress: " + progress + " %");
				}
			}
		} catch(InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			e.printStackTrace();
		}

		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Total Transfers: " + transaction_counter + " tx");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Total Transfers: ", "" + transaction_counter);

		double avg_transfers_second = ((double) transaction_counter) / sec_duration;
		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Transfers per Second: " + avg_transfers_second + " tx/s");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Transfers per Second: ", "" + avg_transfers_second);

		double avg_transfer_time = (transfer_total_time/((double) s_to_ns)) / ((double) transaction_counter);
		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Transfer Time: " + avg_transfer_time + " s");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Transfer Time: ", "" + avg_transfer_time);

		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Total Balances: " + balance_counter + " b");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Total Balances: ", "" + balance_counter);

		double avg_balances_second = ((double) balance_counter) / sec_duration;
		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Balances per Second: " + avg_balances_second + " b/s");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Balances per Second: ", "" + avg_balances_second);

		double avg_balance_time = (balance_total_time/((double) s_to_ns)) / ((double) balance_counter);
		System.out.println("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Balance Time: " + avg_balance_time + " s");
		results.put("{" + exp + "} ["+ run +"] ("+thread_id +") " + "Average Balance Time: ", "" + avg_balance_time);
	}

	private static void processThreadsResults(int run, int exp, int n_threads, ConcurrentMap<String, String> thread_results, Map<String, String> run_results) {
		double total_transfers = 0.0;
		double avg_transfers_second = 0.0;
		double avg_transfer_time = 0.0;

		double total_balances = 0.0;
		double avg_balances_second = 0.0;
		double avg_balance_time = 0.0;

		for(Entry<String, String> e : thread_results.entrySet()) {
			String key = e.getKey();
			String result = e.getValue();

			if(key.contains("Total Transfers")) {
				total_transfers += Long.parseLong(result);
			} else if(key.contains("Average Transfers per Second")) {
				avg_transfers_second += Double.parseDouble(result);
			} else if(key.contains("Average Transfer Time")) {
				avg_transfer_time += Double.parseDouble(result);
			} else if(key.contains("Total Balances")) {
				total_balances += Long.parseLong(result);
			} else if(key.contains("Average Balances per Second")) {
				avg_balances_second += Double.parseDouble(result);
			} else if(key.contains("Average Balance Time")) {
				avg_balance_time += Double.parseDouble(result);
			}
		}

		avg_transfers_second /= n_threads;
		avg_transfer_time /= n_threads;

		avg_balances_second /= n_threads;
		avg_balance_time /= n_threads;

		System.out.println("{" + exp + "} ["+ run +"] Total Transfers: " + total_transfers + " tx");
		System.out.println("{" + exp + "} ["+ run +"] Total Average Transfers: " + total_transfers / n_threads + " tx");
		System.out.println("{" + exp + "} ["+ run +"] Average Transfers per Second: " + avg_transfers_second + " tx/s");
		System.out.println("{" + exp + "} ["+ run +"] Average Transfer Time: " + avg_transfer_time + " s");

		run_results.put("{" + exp + "} ["+ run +"] Total Transfers", "" + total_transfers);
		run_results.put("{" + exp + "} ["+ run +"] Total Average Transfers", "" + (total_transfers / n_threads));
		run_results.put("{" + exp + "} ["+ run +"] Average Transfers per Second", "" + avg_transfers_second);
		run_results.put("{" + exp + "} ["+ run +"] Average Transfer Time", "" + avg_transfer_time);

		System.out.println("{" + exp + "} ["+ run +"] Total Balances: " + total_balances + " b");
		System.out.println("{" + exp + "} ["+ run +"] Total Average Balances: " + total_balances / n_threads + " b");
		System.out.println("{" + exp + "} ["+ run +"] Average Balances per Second: " + avg_balances_second + " b/s");
		System.out.println("{" + exp + "} ["+ run +"] Average Balance Time: " + avg_balance_time + " s");

		run_results.put("{" + exp + "} ["+ run +"] Total Balances", "" + total_balances);
		run_results.put("{" + exp + "} ["+ run +"] Total Average Balances", "" + (total_balances / n_threads));
		run_results.put("{" + exp + "} ["+ run +"] Average Balances per Second", "" + avg_balances_second);
		run_results.put("{" + exp + "} ["+ run +"] Average Balance Time", "" + avg_balance_time);
	}

	private static void processRunResults(int exp, int n_runs, Map<String, String> run_results, Map<String, String> exp_results) {
		double total_transfers = 0.0;
		double total_avg_transfers = 0.0;
		double avg_transfers_second = 0.0;
		double avg_transfer_time = 0.0;

		double total_balances = 0.0;
		double total_avg_balances = 0.0;
		double avg_balances_second = 0.0;
		double avg_balance_time = 0.0;

		for(Entry<String, String> e : run_results.entrySet()) {
			String key = e.getKey();
			String result = e.getValue();

			if(key.contains("Total Transfers")) {
				total_transfers += Double.parseDouble(result);
			} else if(key.contains("Total Average Transfers")) {
				total_avg_transfers += Double.parseDouble(result);
			} else if(key.contains("Average Transfers per Second")) {
				avg_transfers_second += Double.parseDouble(result);
			} else if(key.contains("Average Transfer Time")) {
				avg_transfer_time += Double.parseDouble(result);
			} else if(key.contains("Total Balances")) {
				total_balances += Double.parseDouble(result);
			} else if(key.contains("Total Average Balances")) {
				total_avg_balances += Double.parseDouble(result);
			} else if(key.contains("Average Balances per Second")) {
				avg_balances_second += Double.parseDouble(result);
			} else if(key.contains("Average Balance Time")) {
				avg_balance_time += Double.parseDouble(result);
			}
		}

		total_transfers /= n_runs;
		total_avg_transfers /= n_runs;
		avg_transfers_second /= n_runs;
		avg_transfer_time /= n_runs;

		total_balances /= n_runs;
		total_avg_balances /= n_runs;
		avg_balances_second /= n_runs;
		avg_balance_time /= n_runs;

		System.out.println("{" + exp + "} Total Transfers: " + total_transfers + " tx");
		System.out.println("{" + exp + "} Total Average Transfers: " + total_avg_transfers + " tx");
		System.out.println("{" + exp + "} Average Transfers per Second: " + avg_transfers_second + " tx/s");
		System.out.println("{" + exp + "} Average Transfer Time: " + avg_transfer_time + " s");

		exp_results.put("{" + exp + "} Total Transfers", "" + total_transfers);
		exp_results.put("{" + exp + "} Total Average Transfers", "" + total_avg_transfers);
		exp_results.put("{" + exp + "} Average Transfers per Second", "" + avg_transfers_second);
		exp_results.put("{" + exp + "} Average Transfer Time", "" + avg_transfer_time);

		System.out.println("{" + exp + "} Total Balances: " + total_balances + " b");
		System.out.println("{" + exp + "} Total Average Balances: " + total_avg_balances + " b");
		System.out.println("{" + exp + "} Average Balances per Second: " + avg_balances_second + " b/s");
		System.out.println("{" + exp + "} Average Balance Time: " + avg_balance_time + " s");

		exp_results.put("{" + exp + "} Total Balances", "" + total_balances);
		exp_results.put("{" + exp + "} Total Average Balances", "" + total_avg_balances);
		exp_results.put("{" + exp + "} Average Balances per Second", "" + avg_balances_second);
		exp_results.put("{" + exp + "} Average Balance Time", "" + avg_balance_time);
	}
	
	private static void storeResults(Map<String, String[]> results, String path) {
		
		Map<String, String[]> map = new TreeMap<String, String[]>(results);
		
		File yourFile = new File(path);

		FileOutputStream oFile = null;

		PrintWriter pw = null;
		try {
			yourFile.createNewFile(); // if file already exists will do nothing 
			oFile = new FileOutputStream(yourFile, false);
			pw = new PrintWriter(new FileWriter(oFile.getFD()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] lines = new String[n_threads+1];
		lines[0] = "";
		for(int i = 1; i < lines.length; i++) {
			lines[i] = i + "";
		}
		
		for( Entry<String, String[]> e : map.entrySet() ) {
			String[] v = e.getValue();
			
			lines[0] += String.format(";%s", e.getKey());
			
			for(int i = 0; i < v.length; i++) {
				lines[i+1] += String.format(";%s", v[i]);
			}
		}
		
		for(int i = 0; i < lines.length; i++) {
			pw.println(lines[i]);
			System.out.println(lines[i]);
		}
		
		pw.close();
	}

}
