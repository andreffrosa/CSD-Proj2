package replicaManager;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.LedgerRequest;
import rest.entities.TransferRequest;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

@Path(ReplicaManagerService.PATH)
public interface ReplicaManagerService {

	static final String PATH = "/replicaManager";

	@POST
	@Path("/launch")
	@Consumes(MediaType.APPLICATION_JSON)
	public void launch(LaunchRequest request) ;

	@POST
	@Path("/stop")
	@Consumes(MediaType.APPLICATION_JSON)
	public void stop();

}