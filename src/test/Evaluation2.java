package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.IO;
import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.client.WalletClient;

public class Evaluation2 {

	private static final String[] servers = (String[]) IO.loadObject("./servers.json", String[].class);

	private static int max_vars = 50;
	private static int max_runs = 3;
	private static int dummy_runs = 30;
	private static String output_folder = "./";

	public static void main(String[] args) throws Exception {

		processArgs(args);

		WalletClient myWallet = new WalletClient(servers);

		// To get rid o sl4j print so that it does not influence the measurements of the first op and
		// to warm up the replicas
		for(int i = 0; i < dummy_runs; i++) {
			myWallet.create(DataType.WALLET, "DUMMY-"+i, 12345); 
		}
		
		Thread.sleep(500);

		int n_ops = 6;
		List<Entry<String,Operation>> ops = new ArrayList<>(n_ops);

		ops.add(new AbstractMap.SimpleEntry<>("Create", (type, var) -> {
			myWallet.create(type, var, 100);
		}));

		ops.add(new AbstractMap.SimpleEntry<>("Get", (type, var) -> {
			myWallet.get(type, var);
		}));

		ops.add(new AbstractMap.SimpleEntry<>("Sum", (type, var) -> {
			myWallet.sum(type, var, 5);
		}));

		ops.add(new AbstractMap.SimpleEntry<>("Set", (type, var) -> {
			myWallet.set(type, var, 10);
		}));

		ops.add(new AbstractMap.SimpleEntry<>("getBetween", (type, var) -> {
			myWallet.get(type.toString(), 5, 15);
		}));

		ops.add(new AbstractMap.SimpleEntry<>("Compare", (type, var) -> {
			myWallet.compare(type, var, ConditionalOperation.GREATER_OR_EQUAL, 1);
		}));

		System.out.println("Starting experience...\n");

		Map<String,Map<DataType,double[]>> total_results = new HashMap<>(n_ops);

		int progress_counter = 0;
		for(Entry<String,Operation> current_op : ops) {
			String op_name = current_op.getKey();
			Operation op = current_op.getValue();

			processOp(op_name, total_results, op);

			System.out.println(String.format("\n Experience Progress %d%%\n", getProgress(++progress_counter, ops.size())));
		}

		processResults(total_results, output_folder + "latency.csv", 0);
		System.out.println();
		processResults(total_results, output_folder + "throughput.csv", 1);
	}

	private static void processArgs(String[] args) {
		String usage = "Usage: Evaluation2 [options] \n" + "Options: \n" 
	            + "\t -help or -h : display this menu \n"
				+ "\t -d <dummy_variables> : number of dummy variables to be created at the beggining \n"
				+ "\t -v <variables> : number of variables of each type to be created \n"
				+ "\t -r <n_runs> : number of runs of each experiment to average the results \n"
				+ "\t -o <output_folder> : output folder for the results' files \n";
		
		try {
			for (int i = 0; i < args.length; i++) {

				switch (args[i]) {
				case "-d":
					dummy_runs = Integer.parseInt(args[i + 1]);
					break;
				case "-v":
					max_vars = Integer.parseInt(args[i + 1]);
					break;
				case "-r":
					max_runs = Integer.parseInt(args[i + 1]);
					break;
				case "-o":
					output_folder = args[i + 1];
					break;
				case "-help":
				case "-h":
					System.out.println(usage);
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(usage);
		}
	}

	private static int getProgress(int current, int total) {
		return (int) Math.round(100*(((double)current)/total));
	}

	private static interface Operation {
		public void run(DataType type, String var) throws Exception;
	}

	private static void processOp(String op_name, Map<String,Map<DataType,double[]>> total_results, Operation op) throws Exception {

		DataType[] dataTypes = DataType.values();

		Map<DataType,double[]> op_results = new HashMap<>(dataTypes.length);

		for(DataType type : dataTypes) {
			String[] vars = new String[max_vars];

			double avg_latency = 0.0;
			double avg_throughput = 0.0;

			for(int r = 0; r < max_runs; r++) {
				System.out.println("Run " + (r+1) + "/" + (max_runs));
				double latency = 0;
				double throughput = System.nanoTime();

				for(int i = 0; i < max_vars; i++) {
					vars[i] = type.toString() + "-" + i;
					long start = System.nanoTime();
					op.run(type, vars[i]);
					latency += System.nanoTime() - start;
				}

				latency = nanoToMilli(latency/max_vars);

				throughput = max_vars/nanoToSecond(System.nanoTime() - throughput);

				System.out.println("Run " + r + " " + type.toString() + " " + op_name + " Latency : " + latency + " ms Throughput: " + throughput + " ops/s");

				avg_latency += latency;
				avg_throughput += throughput;

				//System.out.println(String.format("\n Current Run Progress %d%%\n", getProgress(r, max_runs)));
			}

			avg_latency /= max_runs;
			avg_throughput /= max_runs;

			System.out.println(type.toString() + " " + op_name + " Latency : " + avg_latency + " ms Throughput: " + avg_throughput + " ops/s");

			op_results.put(type, new double[] {avg_latency, avg_throughput});

			//System.out.println(String.format("\n Types Progress %d%%\n", getProgress(++progress_counter, dataTypes.length)));
		}

		total_results.put(op_name, op_results);
	}

	private static double nanoToMilli(double avg_latency) {
		return avg_latency/(1000*1000);
	}

	private static double nanoToSecond(double avg_latency) {
		return avg_latency/(1000*1000*1000);
	}

	private static void processResults(Map<String,Map<DataType,double[]>> total_results, String path, int index) {
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

		for(Entry<String,Map<DataType,double[]>> e : total_results.entrySet()) {
			String line = "";
			String op_name = e.getKey();
			line += op_name + ";";

			Map<DataType,double[]> m = e.getValue();

			String[] s = new String[m.size()];

			// tem de vir pela mesma ordem sempre 
			for(Entry<DataType,double[]> e2 : m.entrySet()) {
				int pos = e2.getKey().ordinal();
				s[pos] = e2.getValue()[index] + ";";
			}

			for(String a : s) {
				line += a;
			}

			System.out.println(line);

			pw.println(line);
		}

		for(DataType t : DataType.values()) {
			System.out.println(t.toString() + " " + t.ordinal());
		}

		/*String[] lines = new String[n_threads + 1];
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
		 */

		pw.close();
	}

}
