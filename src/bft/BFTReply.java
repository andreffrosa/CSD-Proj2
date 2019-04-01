package bft;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import bftsmart.reconfiguration.util.ECDSAKeyLoader;
import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.reconfiguration.util.SunECKeyLoader;
import bftsmart.tom.util.KeyLoader;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BFTReply implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private int n_replies;
	private byte[] reply;
	private byte[][] signatures;
	private int[] ids;
	private BFTWalletResultType result_type;

	public BFTReply() {}

	public BFTReply(int n_replies, byte[] reply, byte[][] signatures, int[] ids, BFTWalletResultType result_type) {
		this.n_replies = n_replies;
		this.reply = reply;
		this.signatures = signatures;
		this.ids = ids;
		this.result_type = result_type;
	}

	public static BFTReply processReply( byte[] reply ) {

		if (reply == null || reply.length == 0) {
			return null;
		}

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {

			int replies = objIn.readInt();

			byte[][] signatures = new byte[replies][];
			int[] ids =  new int[replies];
			byte[] content = (byte[]) objIn.readObject();

			for(int i = 0; i < replies; i++) {
				signatures[i] = (byte[]) objIn.readObject();
				ids[i] = (int) objIn.readInt();
			}
			
			ByteArrayInputStream byteIn2 = new ByteArrayInputStream(content.clone());
			ObjectInput objIn2 = new ObjectInputStream(byteIn2);
			
			BFTWalletResultType result_status = (BFTWalletResultType) objIn2.readObject();
			
			objIn2.close();
			byteIn2.close();
			
			System.out.println(result_status);
			
			return new BFTReply(replies, content, signatures, ids, result_status);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	public byte[][] getSignatures() {
		return signatures;
	}

	public int[] getids() {
		return ids;
	}

	public int getRepliesNumber() {
		return n_replies;
	}

	public byte[] getReply() {
		return reply;
	}

	public boolean validateSignatures() {
		System.out.println("Replies verification not implemented!");

		/*Map<String, String> configs = loadConfig();
		//System.out.println("system.communication.useSignatures = " + configs.get("system.communication.useSignatures"));
		System.out.println("system.communication.signatureAlgorithm = " + configs.get("system.communication.signatureAlgorithm"));

		try {
			for(int i = 0; i < signatures.length; i++) {
				System.out.println("id: " + ids[i]);
				KeyLoader keyLoader = getKeyloader(configs, ids[i]);
				PublicKey pk = keyLoader.loadPublicKey();

				System.out.println(pk.toString());

				if( !verifySignature(configs, pk, reply, signatures[i]) ) {
					System.out.println(ids[i] + "'s signature is invalid!");
					//return false;
				}
			}
			return false;

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | CertificateException | IOException e) {
			e.printStackTrace();
			return false;
		}*/

		/*System.out.println("All signatures are valid!");*/
		return true;
	}

	private boolean verifySignature(Map<String, String> configs, PublicKey pk, byte[] message, byte[] signature) {
		String sigAlgorithm = configs.get("system.communication.signatureAlgorithm");
		String sigAlgorithmProvider = configs.get("system.communication.signatureAlgorithmProvider");
		
		try {
			Signature signatureEngine = Signature.getInstance(sigAlgorithm, Security.getProvider(sigAlgorithmProvider));

			signatureEngine.initVerify(pk);

			signatureEngine.update(message);
			return signatureEngine.verify(signature);
		} catch( NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getReplyAsInt() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			
			objIn.readObject();
			return objIn.readInt();
		} catch(IOException | ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public double getReplyAsDouble() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			
			objIn.readObject();
			return objIn.readDouble();
		} catch(IOException | ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public boolean getReplyAsBoolean() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			
			objIn.readObject();
			return objIn.readBoolean();
		} catch(IOException | ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public Object getReplyAsObject() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			
			objIn.readObject();
			return objIn.readObject();
		} catch(IOException | ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public String getReplyAsString() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			
			objIn.readObject();
			return objIn.readUTF();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public boolean isException() {
		return !result_type.equals(BFTWalletResultType.OK);
	}
	
	public BFTWalletResultType getResultType() {
		return result_type;
	}

	private Map<String, String> loadConfig() {

		Map<String, String> configs = new HashMap<>();
		String configHome = "config";
		try {
			String sep = System.getProperty("file.separator");
			String path = configHome + sep + "system.config";
			;
			FileReader fr = new FileReader(path);
			BufferedReader rd = new BufferedReader(fr);
			String line = null;
			while ((line = rd.readLine()) != null) {
				if (!line.startsWith("#")) {
					StringTokenizer str = new StringTokenizer(line, "=");
					if (str.countTokens() > 1) {
						configs.put(str.nextToken().trim(), str.nextToken().trim());
					}
				}
			}
			fr.close();
			rd.close();
		} catch (Exception e) {
			//LoggerFactory.getLogger(this.getClass()).error("Could not load configuration", e);
			System.out.println("Could not load configuration" + e.getMessage());
		}
		return configs;
	}

	private KeyLoader getKeyloader(	Map<String, String> configs, int processId) {

		String configHome = "config";
		String defaultKeyLoader = (String) configs.get("system.communication.defaultKeyLoader");
		String signatureAlgorithm = (String) configs.get("system.communication.signatureAlgorithm");
		boolean defaultKeys = (((String) configs.get("system.communication.defaultkeys")).equalsIgnoreCase("true")) ? true : false;

		defaultKeys = false;

		switch (defaultKeyLoader) {
		case "RSA":
			return new RSAKeyLoader(processId, configHome, defaultKeys, signatureAlgorithm);
		case "ECDSA":
			return new ECDSAKeyLoader(processId, configHome, defaultKeys, signatureAlgorithm);
		case "SunEC":
			return new SunECKeyLoader(processId, configHome, defaultKeys, signatureAlgorithm);
		default:
			return new ECDSAKeyLoader(processId, configHome, defaultKeys, signatureAlgorithm);
		}
	}

}
