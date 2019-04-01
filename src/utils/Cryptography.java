package utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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

public class Cryptography {

	private static String domainParam = "prime192v1";
	private static String provider = "BC";
	private static String keyGenAlgorithm = "EC";
	private static String signatureAlgorithm = "SHA256withECDSA";

	public static KeyPair genKeys() {
		KeyPair kp = null;
		try {
			ECGenParameterSpec specs = new ECGenParameterSpec(domainParam);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyGenAlgorithm, provider);
			keyGen.initialize(specs);

			kp = keyGen.generateKeyPair();
			
			/*System.out.println(kp.getPublic().getAlgorithm());
			System.out.println(kp.getPublic().getFormat());
			
			System.out.println(kp.getPrivate().getAlgorithm());
			System.out.println(kp.getPrivate().getFormat());*/
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

	public static String sign(byte[] message, String privateKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyGenAlgorithm, provider);

			Signature sig = Signature.getInstance(signatureAlgorithm, provider);
			
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(privateKey));
			PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);

			sig.initSign(privKey);
			sig.update(message);
			byte[] signatureBytes = sig.sign();

			String signature = java.util.Base64.getEncoder().encodeToString(signatureBytes);

			return signature;
		} catch(NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static boolean validateSignature(byte[] message, String signature, String publicKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance(keyGenAlgorithm, provider);

			Signature sig = Signature.getInstance(signatureAlgorithm, provider);
			
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(publicKey));
			PublicKey pubKey = kf.generatePublic(keySpecX509);

			sig.initVerify(pubKey);
			sig.update(message);
			
			boolean valid = sig.verify(java.util.Base64.getDecoder().decode(signature));
			
			return valid;
		} catch(InvalidKeySpecException | InvalidKeyException | SignatureException e) {
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
}
