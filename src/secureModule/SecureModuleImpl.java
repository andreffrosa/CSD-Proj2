package secureModule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import com.google.gson.GsonBuilder;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import utils.ConditionParser;
import utils.Cryptography;
import wallet.ConditionalOperation;
import wallet.GetBetweenOP;

public class SecureModuleImpl implements SecureModule {

	public static final String CIPHER_ALGORITHM = "AES";

	private SecretKey secret_key;

	private static final int DEFAULT_CACHE_SIZE = 120;
	private boolean use_cache;
	private Cache cache;

	public SecureModuleImpl(boolean use_cache) {
		secret_key = Cryptography.parseSecretKey(Cryptography.loadKeys("./keys/secureModuleServer/", "secretKey").get(0), null, CIPHER_ALGORITHM);

		this.use_cache = use_cache;
		cache = use_cache ? new Cache(DEFAULT_CACHE_SIZE) : null;
	}

	private synchronized long getOPIKey(String cipheredKey) {

		if(use_cache) {
			//Verify if its in cache
			Object aux = cache.get(cipheredKey);
			if(aux!=null) {
				System.out.println("In cache!");
				return (long) aux;
			}
		}

		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		long key = Long.parseLong(new String(Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM)));

		if(use_cache)
			cache.add(cipheredKey, key);

		return key;
	}

	private synchronized PaillierKey getHomoAddKey(String cipheredKey) {

		if(use_cache) {
			//Verify if its in cache
			Object aux = cache.get(cipheredKey);
			if(aux!=null) {
				System.out.println("In cache!");
				return (PaillierKey) aux;
			} 
		}

		// Decrypt key
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		byte[] raw_data = Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM);
		PaillierKey pk = HomoAdd.keyFromString(new String(raw_data));

		if(use_cache) 
			cache.add(cipheredKey, pk);

		return pk;
	}

	private synchronized BigInteger homoAddDecrypt(BigInteger cipher, PaillierKey pk) throws Exception {
		if(use_cache) {
			//Verify if its in cache
			Object aux = cache.get(cipher.toString());
			if(aux!=null) {
				System.out.println("In cache!");
				return (BigInteger) aux;
			}
		}
		
		// Decrypt key
		BigInteger b = HomoAdd.decrypt(cipher, pk);

		if(use_cache) 
			cache.add(cipher.toString(), b);

		return b;
	}

	public synchronized long addOPI(String req) {

		try {
			AddRequest request = new GsonBuilder().create().fromJson(req, AddRequest.class);
			long opi = request.opi;
			long amount = request.amount;
			String cipheredKey = request.cipheredKey;

			// Decrypt key
			long key = getOPIKey(cipheredKey);

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

	public synchronized boolean compareHomoAdd(String req) {

		try {
			CompareRequest request = new GsonBuilder().create().fromJson(req, CompareRequest.class);

			BigInteger v1 = request.v1;
			BigInteger v2 = request.v2;
			String cipheredKey = request.cipheredKey;
			ConditionalOperation cond = request.cond;

			// Decrypt key
			PaillierKey pk = getHomoAddKey(cipheredKey);

			// Decrypt v1 and v2
			try {
				BigInteger n1 = homoAddDecrypt(v1, pk);
				BigInteger n2 = homoAddDecrypt(v2, pk);

				boolean result = ConditionParser.evaluate(cond, n1, n2);

				System.out.println(String.format("compareSumInt(%s, %s, %s) : %b", v1, v2, cipheredKey, result));

				return result;
			} catch(Exception e) {
				e.printStackTrace();
				return false;  // TODO: O QUE FAZER?
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	} 

	@Override
	public synchronized List<String> getBetweenHomoAdd(String req) {

		GetBetweenHomoAddRequest request = new GsonBuilder().create().fromJson(req, GetBetweenHomoAddRequest.class); 

		Map<String, BigInteger> homo_add_variables = request.homo_add_variables;
		List<GetBetweenOP> ops = request.ops;

		List<String> ids = new ArrayList<>();

		Integer lower_value = null;
		Integer higher_value = null;

		for( GetBetweenOP op : ops) {
			BigInteger encrypted_value = homo_add_variables.get(op.id);

			PaillierKey pk = getHomoAddKey(op.cipheredKey);
			try {
				if(lower_value == null)
					lower_value = homoAddDecrypt(new BigInteger(op.low_value), pk).intValue();

				if(higher_value == null)
					higher_value = homoAddDecrypt(new BigInteger(op.high_value), pk).intValue();


				int value = homoAddDecrypt(encrypted_value, pk).intValue();

				if(lower_value <= value && value <= higher_value) {
					ids.add(op.id);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		System.out.println(String.format("getBetweenHomoAdd(%d) : %d", ops.size(), ids.size()));

		return ids;
	}

}
