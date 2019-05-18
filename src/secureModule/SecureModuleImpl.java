package secureModule;

import java.math.BigInteger;

import javax.crypto.SecretKey;

import com.google.gson.GsonBuilder;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import utils.Bytes;
import utils.Cryptography;

public class SecureModuleImpl implements SecureModule {

	public static final String CIPHER_ALGORITHM = "AES";
	
	private SecretKey secret_key;

	public SecureModuleImpl() {
		secret_key = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, CIPHER_ALGORITHM);
	}

	public long addOPI(String req) {
		try {
		AddRequest request = new GsonBuilder().create().fromJson(req, AddRequest.class);
		long opi = request.opi;
		long amount = request.amount;
		String cipheredKey = request.cipheredKey;
		
		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		long key = Bytes.fromBytes(Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM));

		// Decrypt opi
		HomoOpeInt ope = new HomoOpeInt(key);
		int value = ope.decrypt(opi);
		int raw_amount = ope.decrypt(amount);

		// Sum
		value += raw_amount;

		// Encrypt opi
		long new_opi = ope.encrypt(value);

		System.out.println(String.format("addOPI(%d, %d, %s) : %d", opi, amount, cipheredKey, new_opi));
		
		return new_opi;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int compareSumInt(String req) {
		try {
		CompareRequest request = new GsonBuilder().create().fromJson(req, CompareRequest.class);
		
		BigInteger v1 = request.v1;
		BigInteger v2 = request.v2;
		String cipheredKey = request.cipheredKey;
		
		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		byte[] raw_key = Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM);
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
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	} 

}
