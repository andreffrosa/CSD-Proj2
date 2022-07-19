package test;
import replicaManager.ReplicaManagerRESTClient;
import utils.Cryptography;
import utils.IO;

public class Test2 {
	// Temp
	public static void main(String args[]) throws Exception {

		String fileName = "./target/CSD-Proj-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
		byte[] hash = java.util.Base64.getDecoder().decode(Cryptography.computeHash(IO.loadFile(fileName)));
		String className = "rest.RESTWalletServer";
		String[] r_args = new String[] {"0"};

		ReplicaManagerRESTClient replica = new ReplicaManagerRESTClient("CSD1819", "https://localhost:8030/");

		/*String id = */replica.launch(fileName, hash, className, r_args);

		System.out.println("Replica lauched!");

		/*Thread.sleep(10000);

		////////////////////////////////////////////////////
		Wallet wallet = new RESTWalletClient((String[]) IO.loadObject("./servers.json", String[].class));

		Thread.sleep(5000);

		wallet.ledger();

		testOPI(wallet);

		testSum(wallet);*/

		/////////////////////////////////////////////////////

		//replica.stop(id);
	}

	/*private static void testSum(Wallet wallet) {
		try {
			PaillierKey pk = HomoAdd.generateKey();

			BigInteger big1 = new BigInteger("10");
			BigInteger big2 = new BigInteger("100");	

			System.out.println("big1:     "+big1);
			System.out.println("big2:     "+big2);

			BigInteger big1Code = HomoAdd.encrypt(big1, pk);
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);

			wallet.putSumInt("SUM-"+1, big1Code);
			wallet.putSumInt("SUM-"+2, big2Code);

			System.out.println(wallet.getSumInt("SUM-"+1).compareTo(big1Code) == 0);
			System.out.println(wallet.getSumInt("SUM-"+2).compareTo(big2Code) == 0);

			System.out.println("Add and Sub");

			BigInteger result = wallet.add_sumInt("SUM-"+1, BigInteger.ONE, pk.getNsquare());
			System.out.println(HomoAdd.decrypt(result, pk));
			result = wallet.sub("SUM-"+2, BigInteger.ONE, pk.getNsquare());
			System.out.println(HomoAdd.decrypt(result, pk));

			BigInteger value = HomoAdd.sum(big1Code, big2Code, pk.getNsquare());

			System.out.println(HomoAdd.decrypt(value, pk));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testOPI(Wallet wallet) throws InvalidAddressException {
		long key = HomoOpeInt.generateKey();
		HomoOpeInt ope = new HomoOpeInt(id);

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
		List<Entry<String, Long>> between = wallet.getBetweenKeysOPI("OPI-"+start, "OPI-"+finish);

		for(Entry<String, Long> e : between) {
			System.out.println(e.getKey() + " = " + e.getValue() + "(" + ope.decrypt(e.getValue()) + ")");
		}
	}*/
}
