package replicaManager;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class InvalidPasswordWebException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public InvalidPasswordWebException() {
		super(Response.status(Response.Status.UNAUTHORIZED)
	             .entity("").type(MediaType.TEXT_PLAIN).build());
	}

	public InvalidPasswordWebException(String message) {
		super(Response.status(Response.Status.UNAUTHORIZED)
	             .entity(message).type(MediaType.TEXT_PLAIN).build());
	}
	
}
