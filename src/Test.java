import replicaManager.ReplicaManagerRESTClient;
import utils.Cryptography;
import utils.IO;

public class Test {
	// Temp
	public static void main(String args[]) throws Exception {
		
		String fileName = "./target/CSD-Proj-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
		byte[] hash = java.util.Base64.getDecoder().decode(Cryptography.computeHash(IO.loadFile(fileName)));
		String className = "rest.RESTWalletServer";
		String[] r_args = new String[] {"0"};

		ReplicaManagerRESTClient client = new ReplicaManagerRESTClient("CSD1819", "https://localhost:8030/");

		client.launch(fileName, hash, className, r_args);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.stop();
		
		//hlib.hj.mlib.HomoAdd;
		//hlib.hj.mlib.HomoOpeInt;
	}
}
