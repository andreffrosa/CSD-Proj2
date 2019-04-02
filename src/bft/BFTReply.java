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
import java.util.Map.Entry;
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

	private Object content;
	private BFTWalletResultType result_type;

	public BFTReply() {}

	public BFTReply(Object content, BFTWalletResultType result_type) {
		/*this.replies = replies;
		this.signatures = signatures;
		this.ids = ids;*/
		this.content = content;
		this.result_type = result_type;
	}

	public static BFTReply processReply( byte[] reply ) {

		if (reply == null || reply.length == 0) {
			return null;
		}

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {

			int n_replies = objIn.readInt();

			byte[][] contents = new byte[n_replies][];
			byte[][] signatures = new byte[n_replies][];
			int[] ids =  new int[n_replies];
			
			System.out.println(n_replies);

			for(int i = 0; i < n_replies; i++) {
				ids[i] = (int) objIn.readInt();
				contents[i] = (byte[]) objIn.readObject();
				signatures[i] = (byte[]) objIn.readObject();
			}
			
			// Validate the replies
			return chooseValid(n_replies, ids, contents, signatures);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	private static BFTReply chooseValid(int n_replies, int[] ids, byte[][] contents, byte[][] signatures) {
		
		System.out.println("Choosing the valid...");
		
		Map<String, Integer> aux = new HashMap<>(n_replies);
		
		for(int i = 0; i < n_replies; i++) {
			// Verificar se assinatura é válida
			if( validSignature(ids[i], contents[i], signatures[i]) ) {
				String hash = java.util.Base64.getEncoder().encodeToString(contents[i]);
				Integer count = aux.get(hash);
				if(count == null) {
					count = 0;
				}
				
				aux.put(hash, count+1);
			}
		}
		
		System.out.println("replies sorted " + aux.size());
		
		// Choose the reply with more repetions and without ties
		String choosen = null;
		int max = 0;
		boolean tie = false;
		for( Entry<String, Integer> e : aux.entrySet() ) {
			if(e.getValue() > max) {
				choosen = e.getKey();
				max = e.getValue();
				tie = false;
			} else if(e.getValue() == max) {
				tie = true;
			}
		}
		System.out.println("max " + max + " tie " + tie);
		
		if(choosen != null && !tie) {
			byte[] content = java.util.Base64.getDecoder().decode(choosen);
			
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(content);
					       ObjectInput objIn = new ObjectInputStream(byteIn)) {

				BFTWalletResultType result_status = (BFTWalletResultType) objIn.readObject();
				Object value = objIn.readObject();
				
				System.out.println("return new BFTReply;");
				
				return new BFTReply(value, result_status);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}
		System.out.println("return null;");
		
		return null;
	}
	
	private static boolean validSignature(int id, byte[] content, byte[] signature) {
		// TODO
		return true;
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

	public Object getContent() {
		return this.content;
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
