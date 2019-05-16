import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import replicaManager.ReplicaManagerRESTClient;
import rest.RESTWalletClient;
import rest.RESTWalletServer;
import utils.Cryptography;
import utils.IO;
import wallet.Wallet;
import wallet.client.WalletClient;

public class Test2 {
	// Temp
	public static void main(String args[]) throws Exception {
		
		Wallet wallet = new RESTWalletClient((String[]) IO.loadObject("./servers.json", String[].class));
		
		Thread.sleep(5000);
		
		System.out.println("get ledger!");
		
		wallet.ledger();
		
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
		
		BigInteger result = wallet.add("SUM-"+1, BigInteger.ONE, pk.getNsquare());
		System.out.println(HomoAdd.decrypt(result, pk));
		result = wallet.sub("SUM-"+2, BigInteger.ONE, pk.getNsquare());
		System.out.println(HomoAdd.decrypt(result, pk));
		
		BigInteger value = HomoAdd.sum(big1Code, big2Code, pk.getNsquare());
		System.out.println(HomoAdd.decrypt(value, pk));
	}
}
