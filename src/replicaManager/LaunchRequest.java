package replicaManager;
import java.io.Serializable;

import com.google.gson.GsonBuilder;

public class LaunchRequest implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String fileName;
	public String hash;
	public String className;
	public String args;
	
	public LaunchRequest() {
	}
	
	public LaunchRequest(String fileName, byte[] hash, String className, String[] args) {
		this.fileName = fileName;
		this.hash = java.util.Base64.getEncoder().encodeToString(hash);
		this.className = className;
		this.args = new GsonBuilder().create().toJson(args);
	}
	
	byte[] getHash() {
		return java.util.Base64.getDecoder().decode(hash); 
	}
	
	String[] getArgs() {
		return new GsonBuilder().create().fromJson(args, String[].class);
	}
	
}
