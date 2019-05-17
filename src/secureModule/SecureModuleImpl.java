package secureModule;

import java.math.BigInteger;
import java.security.PrivateKey;

import com.google.gson.GsonBuilder;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import utils.Bytes;
import utils.Cryptography;

public class SecureModuleImpl implements SecureModule {

	private PrivateKey private_key;

	public SecureModuleImpl() {
		private_key = Cryptography.parsePrivateKey(Cryptography.loadKeys("./keys/secureModuleServer/", "privateKey").get(0), null, "RSA");
	}

	public long addOPI(String req) {
		try {
		AddRequest request = new GsonBuilder().create().fromJson(req, AddRequest.class);
		long opi = request.opi;
		int amount = request.amount;
		String cipheredKey = request.cipheredKey;
		
		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		long key = Bytes.fromBytes(Cryptography.decrypt(private_key, rawCipheredKey, "RSA"));

		// Decrypt opi
		HomoOpeInt ope = new HomoOpeInt(key);
		int value = ope.decrypt(opi);

		// Sum
		value += amount;

		// Encrypt opi
		long new_opi = ope.encrypt(value);

		System.out.println(String.format("addOPI(%d, %d, %s) : %d", opi, amount, cipheredKey, new_opi));
		
		return new_opi;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int compareSumInt(CompareRequest request) {

		BigInteger v1 = request.v1;
		BigInteger v2 = request.v2;
		String cipheredKey = request.cipheredKey;
		
		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		byte[] raw_key = Cryptography.decrypt(private_key, rawCipheredKey, "RSA");
		PaillierKey pk = HomoAdd.keyFromString(new String(raw_key));

		// Decrypt v1 and v2
		try {
			BigInteger n1 = HomoAdd.decrypt(v1, pk);
			BigInteger n2 = HomoAdd.decrypt(v2, pk);

			int result = n1.compareTo(n2);
					
			System.out.println(String.format("compareSumInt(%s, %s, %s) : %d", v1, v2, cipheredKey, result));
			
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return 0;  // TODO: O QUE FAZER?
		}
	} 

}
