package test;

import javax.crypto.SecretKey;

import replicaManager.ReplicaManagerRESTClient;
import rest.RESTWalletClient;
import secureModule.SecureModuleImpl;
import utils.Cryptography;
import utils.IO;
import wallet.Transaction;
import wallet.Wallet;
import wallet.client.WalletClient;

public class TestWalletClient {

	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static final SecretKey secretKey = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, SecureModuleImpl.CIPHER_ALGORITHM);

	public static void main(String[] args) throws Exception {
		String fileName = "./target/CSD-Proj-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
		byte[] hash = java.util.Base64.getDecoder().decode(Cryptography.computeHash(IO.loadFile(fileName)));
		String className = "rest.RESTWalletServer";
		String[] r_args = new String[] {"0"};
		
		ReplicaManagerRESTClient replica = new ReplicaManagerRESTClient("CSD1819", "https://localhost:8030/");
		
		String replica_id = replica.launch(fileName, hash, className, r_args);

		System.out.println("Replica lauched!");

		Thread.sleep(20000);
		
		String[] servers = (String[]) IO.loadObject("./servers.json", String[].class);
		
		Wallet wallet = new RESTWalletClient(servers);
		
		WalletClient client = new WalletClient(servers);
		
		String addr = client.generateNewAddress();
		wallet.transfer(new Transaction(ADMIN_PUB_KEY, addr, 100.0, ADMIN_PRIV_KEY));
		
		/////////////////////////////////////////
		
		client.createVariable("OPI", "OPI-1", 10);
		client.createVariable("SumInt", "SUM-1", 0);
		
		client.cond_set("wallet", addr, 50, "SumInt", "SUM-1", 50);
		
		client.cond_add("SumInt", "SUM-1", 20, "wallet", addr, 50);
		
		System.out.println(client.getBalance());
		
		/////////////////////////////////////////
		
		replica.stop(replica_id);
	}
}
