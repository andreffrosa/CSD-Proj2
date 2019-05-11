package replicaManager;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class HashMisMatchWebException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public HashMisMatchWebException() {
		super(Response.status(Response.Status.PRECONDITION_FAILED)
	             .entity("").type(MediaType.APPLICATION_JSON).build());
	}

	public HashMisMatchWebException(String message) {
		super(Response.status(Response.Status.PRECONDITION_FAILED)
	             .entity(message).type(MediaType.APPLICATION_JSON).build());
	}
	
}
