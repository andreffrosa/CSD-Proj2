import replicaManager.ReplicaManagerRESTClient;

public class Test {
	// Temp
	public static void main(String args[]) {
		String fileName = "./target/CSD-Proj-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
		byte[] hash = "".getBytes();
		String className = "rest.RESTWalletServer";
		String[] r_args = new String[] {"0"};

		ReplicaManagerRESTClient client = new ReplicaManagerRESTClient("https://localhost:8030/");

		client.launch(fileName, hash, className, r_args);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.stop();
	}
}
