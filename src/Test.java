import java.util.List;
import java.util.Map.Entry;

import hlib.hj.mlib.HomoOpeInt;
import replicaManager.ReplicaManagerRESTClient;
import rest.RESTWalletClient;
import rest.RESTWalletServer;
import utils.Cryptography;
import utils.IO;
import wallet.Wallet;
import wallet.client.WalletClient;

public class Test {
	// Temp
	public static void main(String args[]) throws Exception {
		
	 	/*String fileName = "./target/CSD-Proj-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
		byte[] hash = java.util.Base64.getDecoder().decode(Cryptography.computeHash(IO.loadFile(fileName)));
		String className = "rest.RESTWalletServer";
		String[] r_args = new String[] {"0"};

		ReplicaManagerRESTClient replica = new ReplicaManagerRESTClient("CSD1819", "https://localhost:8030/");

		replica.launch(fileName, hash, className, r_args);
		
		System.out.println("Replica lauched!");
		
		Thread.sleep(5000);*/

		////////////////////////////////////////////////////
		Wallet wallet = new RESTWalletClient((String[]) IO.loadObject("./servers.json", String[].class));
		
		Thread.sleep(5000);
		
		System.out.println("get ledger!");
		
		wallet.ledger();
		
		long key = HomoOpeInt.generateKey();
		HomoOpeInt ope = new HomoOpeInt(key);
		
		int max = 10;
		
		long results[] = new long[max];
		
		for(int i = 0; i < max; i++) {
			results[i] = ope.encrypt(i);
			wallet.putOrderPreservingInt("OPI-"+i, results[i]);
			System.out.println(i + " : " + results[i]);
		}
		
		for(int i = 0; i < max; i++) {
			long opi = wallet.getOrderPreservingInt("OPI-"+i);
			int x = ope.decrypt(opi);
			
			System.out.println(i + " : " + opi );
			System.out.println(i + " : " + opi );
			
			if(x != i) {
				System.out.println("ERROR decrypting!");
			}
		}

		int start = 3, finish = 6;
		List<Entry<String, Long>> between = wallet.getBetween("OPI-"+start, "OPI-"+finish);
		
		for(Entry<String, Long> e : between) {
			System.out.println(e.getKey() + " = " + e.getValue() + "(" + ope.decrypt(e.getValue()) + ")");
		}
		
		/////////////////////////////////////////////////////
		
		// replica.stop();
		
		//hlib.hj.mlib.HomoAdd;
		//hlib.hj.mlib.HomoOpeInt;
	}
}
