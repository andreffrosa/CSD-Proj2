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
			long key = Long.parseLong(new String(Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM)));

			// Decrypt opi
			HomoOpeInt ope = new HomoOpeInt(id);
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

	public boolean compareHomoAdd(String req) {
		try {
			CompareRequest request = new GsonBuilder().create().fromJson(req, CompareRequest.class);

			BigInteger v1 = request.v1;
			BigInteger v2 = request.v2;
			String cipheredKey = request.cipheredKey;
			ConditionalOperation cond = request.cond;

			// Decrypt key
			PaillierKey pk = decryptHomoAddKey(cipheredKey);

			// Decrypt v1 and v2
			try {
				BigInteger n1 = HomoAdd.decrypt(v1, pk);
				BigInteger n2 = HomoAdd.decrypt(v2, pk);

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
	public List<String> getBetweenHomoAdd(String req) {
		
		GetBetweenHomoAddRequest request = new GsonBuilder().create().fromJson(req, GetBetweenHomoAddRequest.class); 
		
		Map<String, BigInteger> homo_add_variables = request.homo_add_variables;
		List<GetBetweenOP> ops = request.ops;

		List<String> ids = new ArrayList<>();
		
		for( GetBetweenOP op : ops) {
			BigInteger encrypted_value = homo_add_variables.get(op.id);
			
			PaillierKey pk = decryptHomoAddKey(op.cipheredKey);
			try {
				int lower_value = HomoAdd.decrypt(new BigInteger(op.low_value), pk).intValue();
				int higher_value = HomoAdd.decrypt(new BigInteger(op.high_value), pk).intValue();
				int value = HomoAdd.decrypt(encrypted_value, pk).intValue();
				
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

	private PaillierKey decryptHomoAddKey(String cipheredKey) {
		byte[] rawCipheredKey = java.util.Base64.getDecoder().decode(cipheredKey);
		byte[] raw_key = Cryptography.decrypt(secret_key, rawCipheredKey, CIPHER_ALGORITHM);
		PaillierKey pk = HomoAdd.keyFromString(new String(raw_key));
		return pk;
	}

}
