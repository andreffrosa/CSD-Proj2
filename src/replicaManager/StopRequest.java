package replicaManager;

import java.io.Serializable;

public class StopRequest implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String password;
	public String id;
	
	public StopRequest() {
	}
	
	public StopRequest(String password, String id) {
		this.password = password;
		this.id = id;
	}
	
}
