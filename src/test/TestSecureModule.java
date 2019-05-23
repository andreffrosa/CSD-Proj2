package test;

import java.math.BigInteger;

import javax.crypto.SecretKey;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import secureModule.SecureModuleImpl;
import secureModule.SecureModuleRESTClient;
import utils.Bytes;
import utils.Cryptography;

public class TestSecureModule {
	
	public static void main(String[] args) throws Exception {

	/*KeyPair kp = Cryptography.genRSAKeys();
	String pub = Cryptography.getPublicKey(kp);
	String priv = Cryptography.getPrivateKey(kp);
	
	Cryptography.storeKeyInFile(pub, "./keys/secureModuleServer/server.publicKey");
	Cryptography.storeKeyInFile(priv, "./keys/secureModuleServer/server.privateKey");*/
	
	/*String ks = Cryptography.generateSymetricKey("AES", 256);
	Cryptography.storeKeyInFile(ks, "./keys/secureModuleServer/server.secretKey");
		
	System.exit(0);*/

	SecureModuleRESTClient sec_module = new SecureModuleRESTClient("https://localhost:8040/");
	
	long key = HomoOpeInt.generateKey();
	
	HomoOpeInt ope = new HomoOpeInt(id);
	
	long v1 = ope.encrypt(10);

	SecretKey secretKey = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, SecureModuleImpl.CIPHER_ALGORITHM);
	byte[] rawCipheredKey = Cryptography.encrypt(secretKey, Bytes.toBytes(id), SecureModuleImpl.CIPHER_ALGORITHM);
	String cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
	
	long result = sec_module.addOPI(v1, 100, cipheredKey);
	
	System.out.println(ope.decrypt(result));
	
	//////////////
	
	PaillierKey pk = HomoAdd.generateKey();
	
	rawCipheredKey = Cryptography.encrypt(secretKey, HomoAdd.stringFromKey(pk).getBytes(), SecureModuleImpl.CIPHER_ALGORITHM);
	cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
	
	BigInteger big1Code = HomoAdd.encrypt(new BigInteger("1"), pk);	
	BigInteger big2Code = HomoAdd.encrypt(new BigInteger("10"), pk);	
	
	int result2 = sec_module.compareSumInt(big1Code, big2Code, cipheredKey);
	
	System.out.println(result2 < 0); // 1 < 10 ?
	
	}
	
}
