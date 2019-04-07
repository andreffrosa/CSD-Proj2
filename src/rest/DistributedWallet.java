package rest;

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

@Path(DistributedWallet.PATH)
public interface DistributedWallet {

	static final String PATH = "/wallet";

	@POST
	@Path("/transfer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] transfer(TransferRequest request) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;
	
	@POST
	@Path("/atomicTransfer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] atomicTransfer(AtomicTransferRequest request) throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	@POST
	@Path("/balance")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] balance(BalanceRequest request);

	@POST
	@Path("/ledger")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] ledger(LedgerRequest request);
}