package utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Cryptography {

	private static final String domainParam = "prime192v1";
	private static final String provider = "BC";
	private static final String keyGenAlgorithm = "EC";
	private static final String signatureAlgorithm = "SHA256withECDSA";
	private static final String DIGEST_ALGORITHM = "SHA256";

	public static KeyPair genKeys() {
		KeyPair kp = null;
		try {
			ECGenParameterSpec specs = new ECGenParameterSpec(domainParam);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyGenAlgorithm, provider);
			keyGen.initialize(specs);

			kp = keyGen.generateKeyPair();

		} catch(NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		return kp;
	}

	public static String getPrivateKey(KeyPair kp) {
		PrivateKey privkey = kp.getPrivate();
		return java.util.Base64.getEncoder().encodeToString(privkey.getEncoded());
	}

	public static String getPublicKey(KeyPair kp) {
		PublicKey pubkey = kp.getPublic();
		return java.util.Base64.getEncoder().encodeToString(pubkey.getEncoded());
	}

	public static PrivateKey parsePrivateKey(String privateKey, KeyFactory kf) {

		try {
			kf = (kf == null) ? KeyFactory.getInstance(keyGenAlgorithm, provider) : kf;
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(privateKey));
			return kf.generatePrivate(keySpecPKCS8);
		} catch (NoSuchProviderException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static PublicKey parsePublicKey(String publicKey, KeyFactory kf) {

		try {
			kf = (kf == null) ? KeyFactory.getInstance(keyGenAlgorithm, provider) : kf;
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(publicKey));
			return kf.generatePublic(keySpecX509);
		} catch (NoSuchProviderException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String sign(byte[] message, String privateKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyGenAlgorithm, provider);

			Signature sig = Signature.getInstance(signatureAlgorithm, provider);

			PrivateKey privKey = parsePrivateKey(privateKey, kf);

			sig.initSign(privKey);
			sig.update(message);
			byte[] signatureBytes = sig.sign();

			String signature = java.util.Base64.getEncoder().encodeToString(signatureBytes);

			return signature;
		} catch(NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static boolean validateSignature(byte[] message, String signature, String publicKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyGenAlgorithm, provider);

			Signature sig = Signature.getInstance(signatureAlgorithm, provider);

			PublicKey pubKey = parsePublicKey(publicKey, kf);

			sig.initVerify(pubKey);
			sig.update(message);

			boolean valid = sig.verify(java.util.Base64.getDecoder().decode(signature));

			return valid;
		} catch(InvalidKeyException | SignatureException e) {
			return false;
		} catch(NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static boolean validateAdress(String address) {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyGenAlgorithm, provider);
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(address));
			kf.generatePublic(keySpecX509);

			return true;
		} catch(InvalidKeySpecException e) {
			return false;
		} catch(NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static long getNonce() {
		long result = 0L;

		java.security.SecureRandom sr;
		try {
			sr = java.security.SecureRandom.getInstance("sha1PRNG");

			int size = Long.BYTES+1;
			byte[] tmp = new byte[ size ];
			sr.nextBytes( tmp );

			ByteBuffer buffer = ByteBuffer.wrap(tmp);
			//buffer.flip(); //need flip 
			result = buffer.getLong();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			result = Math.round(Math.random()*Long.MAX_VALUE);
		}

		return result;
	}

	public static void storeKeyInFile(String key, String path) {
		IO.storeTextFile(key, path);
	}

	public static String loadKeyInFile(String path) {
		return IO.loadTextFile(path);
	}

	public static List<String> loadKeys(String path) {
		return loadKeys(path, "");
	}
	
	public static List<String> loadKeys(String path, String filter) {

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		List<String> keys = new ArrayList<>(listOfFiles.length);

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(filter) ) {
				String k = loadKeyInFile(listOfFiles[i].getPath());
				keys.add(k);
			} 
		}
		return keys;
	}
	
	public static void storeKeys(Map<String, String> keys) {
		
		for( Entry<String, String> e : keys.entrySet() ) {
			String key = e.getKey();
			String path = e.getValue();
			storeKeyInFile(key, path);
		}
	}
	
	public static String computeHash(byte[] data) {
		
		try {
			MessageDigest d = MessageDigest.getInstance(DIGEST_ALGORITHM);
			d.update(data);
			return java.util.Base64.getEncoder().encodeToString(d.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return java.util.Base64.getEncoder().encodeToString(data);
	}
	
	public static String computeHash(String data) {
		
		try {
			MessageDigest d = MessageDigest.getInstance(DIGEST_ALGORITHM);
			d.update(data.getBytes());
			return java.util.Base64.getEncoder().encodeToString(d.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return data;
	}

}
