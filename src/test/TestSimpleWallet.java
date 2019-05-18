package test;

import java.math.BigInteger;
import java.security.KeyPair;

import javax.crypto.SecretKey;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import secureModule.SecureModuleImpl;
import utils.Bytes;
import utils.Cryptography;
import wallet.SimpleWallet;
import wallet.Transaction;
import wallet.Wallet;

public class TestSimpleWallet {

	private static final String ADMINS_DIRECTORY = "./admins/";

	private static final String ADMIN_PUB_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "publicKey").get(0);
	private static final String ADMIN_PRIV_KEY = Cryptography.loadKeys(ADMINS_DIRECTORY, "privateKey").get(0);

	private static final SecretKey secretKey = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, SecureModuleImpl.CIPHER_ALGORITHM);


	public static void main(String[] args) throws Exception {
		
		KeyPair kp = Cryptography.genKeys();
		String pubKey = Cryptography.getPublicKey(kp);
		//String privKey = Cryptography.getPrivateKey(kp);

		// Test_cond_set
		test_cond_set(new SimpleWallet(), "SumInt", "SUM-1", 10, 5, "OPI", "OPI-1", 3, 25);
		test_cond_set(new SimpleWallet(), "SumInt", "SUM-1", 5, 10, "OPI", "OPI-1", 3, 25);
		test_cond_set(new SimpleWallet(), "OPI", "OPI-1", 10, 5, "SumInt", "SUM-1", 3, 25);
		test_cond_set(new SimpleWallet(), "OPI", "OPI-1", 5, 10, "SumInt", "SUM-1", 3, 25);
		test_cond_set(new SimpleWallet(), "wallet", pubKey, 10, 5, "SumInt", "SUM-1", 3, 25);
		test_cond_set(new SimpleWallet(), "wallet", pubKey, 5, 10, "SumInt", "SUM-1", 3, 25);
		test_cond_set(new SimpleWallet(), "wallet", pubKey, 10, 5, "OPI", "OPI-1", 3, 25);
		test_cond_set(new SimpleWallet(), "wallet", pubKey, 5, 10, "OPI", "OPI-1", 3, 25);
		test_cond_set(new SimpleWallet(), "SumInt", "SUM-1", 10, 5, "wallet", pubKey, 3, 25);
		test_cond_set(new SimpleWallet(), "SumInt", "SUM-1", 5, 10, "wallet", pubKey, 3, 25);
		test_cond_set(new SimpleWallet(), "OPI", "OPI-1", 10, 5, "wallet", pubKey, 3, 25);
		test_cond_set(new SimpleWallet(), "OPI", "OPI-1", 5, 10, "wallet", pubKey, 3, 25);

		// Test_cond_add
		test_cond_add(new SimpleWallet(), "SumInt", "SUM-1", 10, 5, "OPI", "OPI-1", 3, 25);
		test_cond_add(new SimpleWallet(), "SumInt", "SUM-1", 5, 10, "OPI", "OPI-1", 3, 25);
		test_cond_add(new SimpleWallet(), "OPI", "OPI-1", 10, 5, "SumInt", "SUM-1", 3, 25);
		test_cond_add(new SimpleWallet(), "OPI", "OPI-1", 5, 10, "SumInt", "SUM-1", 3, 25);
		test_cond_add(new SimpleWallet(), "wallet", pubKey, 10, 5, "SumInt", "SUM-1", 3, 25);
		test_cond_add(new SimpleWallet(), "wallet", pubKey, 5, 10, "SumInt", "SUM-1", 3, 25);
		test_cond_add(new SimpleWallet(), "wallet", pubKey, 10, 5, "OPI", "OPI-1", 3, 25);
		test_cond_add(new SimpleWallet(), "wallet", pubKey, 5, 10, "OPI", "OPI-1", 3, 25);
		test_cond_add(new SimpleWallet(), "SumInt", "SUM-1", 10, 5, "wallet", pubKey, 3, 25);
		test_cond_add(new SimpleWallet(), "SumInt", "SUM-1", 5, 10, "wallet", pubKey, 3, 25);
		test_cond_add(new SimpleWallet(), "OPI", "OPI-1", 10, 5, "wallet", pubKey, 3, 25);
		test_cond_add(new SimpleWallet(), "OPI", "OPI-1", 5, 10, "wallet", pubKey, 3, 25);

		// Test_cond_add TODO
	}

	private static void test_cond_add(Wallet wallet, String x_type, String x_name, int x, int n, String y_type, String y_name, int y, int u) throws Exception {
		long key = HomoOpeInt.generateKey();
		HomoOpeInt ope = new HomoOpeInt(key);

		PaillierKey pk = HomoAdd.generateKey();

		String  cond_cipheredKey = null;
		String  upd_auxArg = null;

		String n_ = "", u_ = "";

		// Insert X
		if(x_type.equals("SumInt")) {
			BigInteger big1 = new BigInteger("" + x);
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);
			wallet.putSumInt(x_name, big1Code);

			BigInteger big2 = new BigInteger("" + n);
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);
			n_ = big2Code.toString();

			byte[]  rawCipheredKey = Cryptography.encrypt(secretKey, HomoAdd.stringFromKey(pk).getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
			cond_cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		} else if(x_type.equals("OPI")) {
			long opi = ope.encrypt(x);
			wallet.putOrderPreservingInt(x_name, opi);

			n_ = "" + ope.encrypt(n);
		} else if(x_type.equals("wallet")) {
			wallet.transfer(new Transaction(ADMIN_PUB_KEY, x_name, x, ADMIN_PRIV_KEY));

			n_ = "" + n;
		}

		// Insert Y
		if(y_type.equals("SumInt")) {			
			BigInteger big1 = new BigInteger("" + y);
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);
			wallet.putSumInt(y_name, big1Code);

			BigInteger big2 = new BigInteger("" + u);
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);
			u_ = big2Code.toString();

			upd_auxArg = "" + pk.getNsquare();
		} else if(y_type.equals("OPI")) {
			long opi = ope.encrypt(y);
			wallet.putOrderPreservingInt(y_name, opi);

			u_ = "" + ope.encrypt(u);

			byte[] rawCipheredKey = Cryptography.encrypt(secretKey, Bytes.toBytes(key), SecureModuleImpl.CIPHER_ALGORITHM);
			String cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
			upd_auxArg = cipheredKey;

		} else if(y_type.equals("wallet")) {
			wallet.transfer(new Transaction(ADMIN_PUB_KEY, y_name, y, ADMIN_PRIV_KEY));

			u_ = "" + u;
		}

		// if SUM-1 >= 5 then OPI-1 += 20
		boolean result = wallet.cond_add(x_name, x_type, n_, cond_cipheredKey, y_name, y_type, u_, upd_auxArg);
		boolean expected_result = (x >= n);

		int add_value = -1;
		// Get set_value
		if(y_type.equals("SumInt")) {
			add_value = HomoAdd.decrypt(wallet.getSumInt(y_name), pk).intValue();
		} else if(y_type.equals("OPI")) {
			add_value = ope.decrypt(wallet.getOrderPreservingInt(y_name));
		} else if(y_type.equals("wallet")) {
			add_value = (int) wallet.balance(y_name);
		}

		boolean add_value_is_correct = add_value == (result ? y+u : y);
		//System.out.println();

		boolean isValid = (result == expected_result) && add_value_is_correct;

		System.out.println(isValid + " | cond = " + result + " : " + expected_result + " | " + "add_value = " + add_value + " : " + (result ? y+u : y));
	}

	// if x >= n then y = u
	private static void test_cond_set(Wallet wallet, String x_type, String x_name, int x, int n, String y_type, String y_name, int y, int u) throws Exception {
		long key = HomoOpeInt.generateKey();
		HomoOpeInt ope = new HomoOpeInt(key);

		PaillierKey pk = HomoAdd.generateKey();

		String  cipheredKey = null;

		String n_ = "", u_ = "";

		// Insert X
		if(x_type.equals("SumInt")) {
			BigInteger big1 = new BigInteger("" + x);
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);
			wallet.putSumInt(x_name, big1Code);

			BigInteger big2 = new BigInteger("" + n);
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);
			n_ = big2Code.toString();

			byte[]  rawCipheredKey = Cryptography.encrypt(secretKey, HomoAdd.stringFromKey(pk).getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
			cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
		} else if(x_type.equals("OPI")) {
			long opi = ope.encrypt(x);
			wallet.putOrderPreservingInt(x_name, opi);

			n_ = "" + ope.encrypt(n);
		} else if(x_type.equals("wallet")) {
			wallet.transfer(new Transaction(ADMIN_PUB_KEY, x_name, x, ADMIN_PRIV_KEY));

			n_ = "" + n;
		}

		// Insert Y
		if(y_type.equals("SumInt")) {			
			BigInteger big1 = new BigInteger("" + y);
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);
			wallet.putSumInt(y_name, big1Code);

			BigInteger big2 = new BigInteger("" + u);
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);
			u_ = big2Code.toString();
		} else if(y_type.equals("OPI")) {
			long opi = ope.encrypt(y);
			wallet.putOrderPreservingInt(y_name, opi);

			u_ = "" + ope.encrypt(u);
		} else if(y_type.equals("wallet")) {
			wallet.transfer(new Transaction(ADMIN_PUB_KEY, y_name, y, ADMIN_PRIV_KEY));

			u_ = "" + u;
		}

		// if SUM-1 >= 5 then OPI-1 = 20
		boolean result = wallet.cond_set(x_name, x_type, n_, cipheredKey, y_name, y_type, u_);
		boolean expected_result = (x >= n);

		int set_value = -1;
		// Get set_value
		if(y_type.equals("SumInt")) {
			set_value = HomoAdd.decrypt(wallet.getSumInt(y_name), pk).intValue();
		} else if(y_type.equals("OPI")) {
			set_value = ope.decrypt(wallet.getOrderPreservingInt(y_name));
		} else if(y_type.equals("wallet")) {
			set_value = (int) wallet.balance(y_name);
		}

		boolean set_value_is_correct = set_value == (result ? u : y);
		//System.out.println();

		boolean isValid = (result == expected_result) && set_value_is_correct;

		System.out.println(isValid + " | cond = " + result + " : " + expected_result + " | " + "set_value = " + set_value + " : " + (result ? u : y));
	}



}
