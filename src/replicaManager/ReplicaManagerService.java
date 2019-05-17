package replicaManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path(ReplicaManagerService.PATH)
public interface ReplicaManagerService {

	static final String PATH = "/replicaManager";

	@POST
	@Path("/launch")
	@Consumes(MediaType.APPLICATION_JSON)
	public String launch(LaunchRequest request);

	@POST
	@Path("/stop")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean stop(StopRequest request);

}