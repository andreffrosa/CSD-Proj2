package test;

import java.security.KeyPair;
import java.security.PublicKey;

import hlib.hj.mlib.HomoOpeInt;
import secureModule.SecureModuleRESTClient;
import utils.Bytes;
import utils.Cryptography;

public class TestSecureModule {
	
	public static void main(String[] args) {

	/*KeyPair kp = Cryptography.genRSAKeys();
	String pub = Cryptography.getPublicKey(kp);
	String priv = Cryptography.getPrivateKey(kp);
	
	Cryptography.storeKeyInFile(pub, "./keys/secureModuleServer/server.publicKey");
	Cryptography.storeKeyInFile(priv, "./keys/secureModuleServer/server.privateKey");
		
	System.exit(0);*/

	SecureModuleRESTClient sec_module = new SecureModuleRESTClient("https://localhost:8040/");
	
	long key = HomoOpeInt.generateKey();
	
	HomoOpeInt ope = new HomoOpeInt(key);
	
	long v1 = ope.encrypt(10);

	PublicKey pub = Cryptography.parsePublicKey(Cryptography.loadKeys("./keys/secureModuleServer/", "publicKey").get(0), null, "RSA");
	byte[] rawCipheredKey = Cryptography.encrypt(pub, Bytes.toBytes(key), "RSA");
	String cipheredKey = java.util.Base64.getEncoder().encodeToString(rawCipheredKey);
	
	long result = sec_module.addOPI(v1, 100, cipheredKey);
	
	System.out.println(ope.decrypt(result));
	}
	
}
