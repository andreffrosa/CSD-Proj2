package secureModule;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path(SecureModule.PATH)
public interface SecureModule {
	
	static final String PATH = "/secureModule";

	static final String ADD_OPI_PATH = "/addOPI";
	@POST
	@Path(ADD_OPI_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public long addOPI(String request);
	
	static final String COMPARE_SUM_PATH = "/compareSumInt";
	@POST
	@Path(COMPARE_SUM_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public int compareSumInt(CompareRequest request);
	
}
