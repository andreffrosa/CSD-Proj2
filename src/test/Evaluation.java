package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * Each experiment executes transfers and balances with a given ratio, for each
 * number of threads between 1 and <n_threads>, <n_runs> times to average the
 * results.
 * 
 */

public class Evaluation {

	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static final String[] servers = (String[]) IO.loadObject("./servers.json", String[].class);

	private static final Wallet admin_wallet = new RESTWalletClient(servers);

	private static final long s_to_ns = 1000 * 1000 * 1000;

	private static int n_runs = 1;
	private static int n_threads = 8;
	private static long sec_duration = 180;
	private static int n_wallets = 1;

	private static double[] transfer_ratios = {0.1, 0.5, 0.9};
	
	private static int progress_fraction = 20;

	private static String output_folder = "./";
	
	public static void main(String[] args)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException {

		parseArgs(args);
		
		String s = "[" + transfer_ratios[0];
		for(int i = 1; i < transfer_ratios.length; i++) {
			s += ", " + transfer_ratios[i];
		}
		s += "]";
		System.out.println("exps: " + s);

		System.out.println("n_runs: " + n_runs);
		System.out.println("n_threads: " + n_threads);
		System.out.println("n_wallets: " + n_wallets);

		System.out.println("duration: " + sec_duration + " s");

		System.out.println("progres_fraction: " + progress_fraction);

		System.out.println("output_folder: " + output_folder);

		// If not wait, a 500 happens
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		admin_wallet.ledger(); // To force logger to print the error before the evaluation starts and to check if the servers are accessible

		System.out.println("Starting evaluation...");

		evaluate();
	}

	private static void parseArgs(String[] args) {
		String usage = "Usage: Evaluation [options] \n" + "Options: \n" 
	            + "\t -help : display this menu \n"
				+ "\t -t <n_threads> : maximum number of threads \n"
				+ "\t -d <sec_duration> : duration of each run, in seconds \n"
				+ "\t -w <n_wallets> : number of wallets of each thread \n"
				+ "\t -e [transfer_ratio_1,..., transfer_ratio_n] : array of experiments (different transacion ratios) \n"
				+ "\t -r <n_runs> : number of runs of each experiment to average the results \n"
				+ "\t -p <progress_fraction> : interval of progress to display the curretn progress\n"
				+ "\t -o <output_folder> : output folder for the results' files \n" + "\n";

		try {
			for (int i = 0; i < args.length; i++) {

				switch (args[i]) {
				case "-t":
					n_threads = Integer.parseInt(args[i + 1]);
					break;
				case "-d":
					sec_duration = Long.parseLong(args[i + 1]);
					break;
				case "-w":
					n_wallets = Integer.parseInt(args[i + 1]);
					break;
				case "-e":
					String[] aux = args[i+1].substring(1, args[i+1].length()-1).split(",");
					transfer_ratios = new double[aux.length];
					for(int j = 0; j < aux.length; j++) {
						transfer_ratios[j] = Double.parseDouble(aux[j].trim());
					}
					break;
				case "-r":
					n_runs = Integer.parseInt(args[i + 1]);
					break;
				case "-p":
					progress_fraction = Integer.parseInt(args[i + 1]);
					break;
				case "-o":
					output_folder = args[i + 1];
					break;
				case "-help":
					System.out.println(usage);
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(usage);
		}
	}

	private static void evaluate() {

		int n_exps = transfer_ratios.length;
		Map<String, String[]> throughput_results = new HashMap<>(n_exps);
		Map<String, String[]> latency_results = new HashMap<>(n_exps);

		for (int exp = 1; exp <= n_exps; exp++) {
			double current_transfer_ratio = transfer_ratios[exp-1];

			String[] current_exp_throughput = new String[n_threads];
			String[] current_exp_latency = new String[n_threads];

			System.out.println("\n\t Exp " + exp + "/" + n_exps + "\n\t current_transfer_ratio: " + current_transfer_ratio + "\n\n");

			for (int t = 1; t <= n_threads; t++) {
				Map<String, String> exp_results = new HashMap<>(n_runs * 4);

				Map<String, String> run_results = new HashMap<>(n_runs * 4);
				for (int r = 1; r <= n_runs; r++) {
					ConcurrentMap<String, String> thread_results = new ConcurrentHashMap<>(t * 4);

					Thread[] threads = new Thread[t];

					for (int i = 0; i < t; i++) {
						threads[i] = lauchThread(i, r, exp, t, current_transfer_ratio, thread_results);
					}

					for (int i = 0; i < t; i++) {
						try {
							threads[i].join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					processThreadsResults(r, exp, t, thread_results, run_results);
				}

				processRunResults(exp, n_runs, run_results, exp_results);

				double total_transfers = 0.0;
				double total_transfer_time = 0.0;
				double total_balances = 0.0;
				double total_balance_time = 0.0;

				total_transfers = Double.parseDouble(exp_results.get("{" + exp + "} Total Transfers"));
				total_transfer_time = Double.parseDouble(exp_results.get("{" + exp + "} Total Transfer Time"));

				total_balances = Double.parseDouble(exp_results.get("{" + exp + "} Total Balances"));
				total_balance_time = Double.parseDouble(exp_results.get("{" + exp + "} Total Balance Time"));

				double latency = (total_transfer_time + total_balance_time) / (total_transfers + total_balances);
				double throughput = (total_transfers + total_balances) / (sec_duration);
				
				current_exp_latency[t - 1] = "" + latency;
				current_exp_throughput[t - 1] = "" + throughput;
			}

			int r = (int) Math.round(current_transfer_ratio * 100);
			String label = r + "T-" + (100 - r) + "B";
			throughput_results.put(label, current_exp_throughput);
			latency_results.put(label, current_exp_latency);
		}

		storeResults(throughput_results, output_folder + "throughput.csv");
		storeResults(latency_results, output_folder + "latency.csv");
		
		storeResults2(throughput_results, latency_results, output_folder + "out.csv");

		System.out.println("\nThroughput");
		for (Entry<String, String[]> e : throughput_results.entrySet()) {
			String[] v = e.getValue();
			String values = "[" + v[0];
			for (int i = 1; i < v.length; i++) {
				values += ", " + v[i];
			}
			values += "]";
			System.out.println(e.getKey() + ": " + values);
		}
		System.out.println("");

		System.out.println("\nlatency");
		for (Entry<String, String[]> e : latency_results.entrySet()) {
			String[] v = e.getValue();
			String values = "[" + v[0];
			for (int i = 1; i < v.length; i++) {
				values += ", " + v[i];
			}
			values += "]";
			System.out.println(e.getKey() + ": " + values);
		}
	}

	private static Thread lauchThread(int thread_id, int run, int exp, int n_threads, double current_transfer_ratio,
			ConcurrentMap<String, String> results) {
		Thread t = new Thread(() -> {
			System.out.println("Lauching thread " + thread_id + " ...");
			executeOperation(thread_id, run, exp, n_threads, current_transfer_ratio, results);
		});
		t.start();
		return t;
	}

	private static void executeOperation(int thread_id, int run, int exp, int n_threads, double current_transfer_ratio,
			ConcurrentMap<String, String> results) {

		WalletClient[] wallets = new WalletClient[n_wallets];
		for (int i = 0; i < n_wallets; i++) {
			wallets[i] = new WalletClient(servers);
		}

		String[] addresses = new String[n_wallets];
		for (int i = 0; i < n_wallets; i++) {
			addresses[i] = wallets[i].generateNewAddress();
		}

		int transaction_counter = 0;
		int balance_counter = 0;

		long transfer_total_time = 0L;
		long balance_total_time = 0L;

		long finish = System.nanoTime() + sec_duration * s_to_ns;
		long current_time = 0L;

		try {
			int old_progress = 0;
			int progress = 0;
			System.out.println("{" + exp + "} [" + run + "] (" + thread_id + ") Progress: 0 %");
			while ((current_time = System.nanoTime()) < finish) {
				for (int i = 0; i < n_wallets; i++) {
					double r = Math.random();

					long start = 0L;
					if (r <= current_transfer_ratio) {
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

				// Print progress
				progress = 100 - (int) Math.round((((double) (finish - current_time)) / (sec_duration * s_to_ns)) * 100.0);
				if (progress - old_progress >= progress_fraction) {
					old_progress = progress;
					System.out.println("{" + exp + "} [" + run + "] (" + thread_id + ") Progress: " + progress + " %");
				}
			}
		} catch (InvalidAddressException | InvalidAmountException | InvalidSignatureException | NotEnoughMoneyException e) {
			e.printStackTrace();
		}
		
		String key = "";
		double value = 0.0;

		id = "{" + exp + "} [" + run + "] (" + thread_id + ") " + "Total Transfers";
		System.out.println(id + ": " + transaction_counter + " tx");
		results.put(id, "" + transaction_counter);
		
		id = "{" + exp + "} [" + run + "] (" + thread_id + ") " + "Total Transfer Time";
		value = transfer_total_time / ((double) s_to_ns);
		System.out.println(id + ": " + value + " s");
		results.put(id, "" + value);
		
		id = "{" + exp + "} [" + run + "] (" + thread_id + ") " + "Total Balances";
		System.out.println(id + ": " + balance_counter + " bx");
		results.put(id, "" + balance_counter);
		
		id = "{" + exp + "} [" + run + "] (" + thread_id + ") " + "Total Balance Time";
		value = balance_total_time / ((double) s_to_ns);
		System.out.println(id + ": " + value + " s");
		results.put(id, "" + value);
	}

	private static void processThreadsResults(int run, int exp, int n_threads, ConcurrentMap<String, String> thread_results, Map<String, String> run_results) {
		double total_transfers = 0.0;
		double total_transfer_time = 0.0;

		double total_balances = 0.0;
		double total_balance_time = 0.0;

		for (Entry<String, String> e : thread_results.entrySet()) {
			String key = e.getKey();
			String result = e.getValue();

			if (id.contains("Total Transfers")) {
				total_transfers += Double.parseDouble(result);
			} else if (id.contains("Total Transfer Time")) {
				total_transfer_time += Double.parseDouble(result);
			} else if (id.contains("Total Balances")) {
				total_balances += Double.parseDouble(result);
			} else if (id.contains("Total Balance Time")) {
				total_balance_time += Double.parseDouble(result);
			} 
		}
		
		String key = "";

		id = "{" + exp + "} [" + run + "] Total Transfers";
		System.out.println(id + ": " + total_transfers + " tx");
		run_results.put(id, "" + total_transfers);
		
		id = "{" + exp + "} [" + run + "] Total Transfer Time";
		System.out.println(id + ": " + total_transfer_time + " s");
		run_results.put(id, "" + total_transfer_time);
		
		id = "{" + exp + "} [" + run + "] Total Balances";
		System.out.println(id + ": " + total_balances + " bx");
		run_results.put(id, "" + total_balances);
		
		id = "{" + exp + "} [" + run + "] Total Balance Time";
		System.out.println(id + ": " + total_balance_time + " s");
		run_results.put(id, "" + total_balance_time);
	}

	private static void processRunResults(int exp, int n_runs, Map<String, String> run_results, Map<String, String> exp_results) {
		double total_transfers = 0.0;
		double total_transfer_time = 0.0;

		double total_balances = 0.0;
		double total_balance_time = 0.0;

		for (Entry<String, String> e : run_results.entrySet()) {
			String key = e.getKey();
			String result = e.getValue();

			if (id.contains("Total Transfers")) {
				total_transfers += Double.parseDouble(result);
			} else if (id.contains("Total Transfer Time")) {
				total_transfer_time += Double.parseDouble(result);
			} else if (id.contains("Total Balances")) {
				total_balances += Double.parseDouble(result);
			} else if (id.contains("Total Balance Time")) {
				total_balance_time += Double.parseDouble(result);
			} 
		}

		total_transfers /= n_runs;
		total_transfer_time /= n_runs;
		total_balances /= n_runs;
		total_balance_time /= n_runs;
		
		String key = "";

		id = "{" + exp + "} Total Transfers";
		System.out.println(id + ": " + total_transfers + " tx");
		exp_results.put(id, "" + total_transfers);
		
		id = "{" + exp + "} Total Transfer Time";
		System.out.println(id + ": " + total_transfer_time + " s");
		exp_results.put(id, "" + total_transfer_time);
		
		id = "{" + exp + "} Total Balances";
		System.out.println(id + ": " + total_balances + " bx");
		exp_results.put(id, "" + total_balances);
		
		id = "{" + exp + "} Total Balance Time";
		System.out.println(id + ": " + total_balance_time + " s");
		exp_results.put(id, "" + total_balance_time);
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

		String[] lines = new String[n_threads + 1];
		lines[0] = "";
		for (int i = 1; i < lines.length; i++) {
			lines[i] = i + "";
		}

		for (Entry<String, String[]> e : map.entrySet()) {
			String[] v = e.getValue();

			lines[0] += String.format(";%s", e.getKey());

			for (int i = 0; i < v.length; i++) {
				lines[i + 1] += String.format(";%s", v[i]);
			}
		}

		for (int i = 0; i < lines.length; i++) {
			pw.println(lines[i]);
			System.out.println(lines[i]);
		}

		pw.close();
	}
	
	private static void storeResults2(Map<String, String[]> throughput, Map<String, String[]> latency, String path) {

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
		
		Iterator<Entry<String, String[]>> it1 = throughput.entrySet().iterator();
		Iterator<Entry<String, String[]>> it2 = latency.entrySet().iterator();
		
		String[] lines = new String[n_threads];
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = "";
		}
		
		String tags = "";
		
		while( it1.hasNext() && it2.hasNext() ) {
			Entry<String, String[]> e1 = it1.next();
			Entry<String, String[]> e2 = it2.next();
			
			tags += e1.getKey() + ";;";

			String[] values = e1.getValue();
			for(int i = 0; i < values.length; i++) {
				lines[i] += values[i] + ";";
			}
			
			values = e2.getValue();
			for(int i = 0; i < values.length; i++) {
				lines[i] += values[i] + ";";
			}
		}

		pw.println(tags);
		System.out.println(tags);
		for (int i = 0; i < lines.length; i++) {
			pw.println(lines[i]);
			System.out.println(lines[i]);
		}

		pw.close();
	}

}
