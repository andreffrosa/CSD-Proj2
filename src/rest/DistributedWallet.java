package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.GetBetweenOrderPreservingRequest;
import rest.entities.GetOrderPreservingRequest;
import rest.entities.LedgerRequest;
import rest.entities.PutOrderPreservingRequest;
import rest.entities.TransferRequest;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

@Path(DistributedWallet.PATH)
public interface DistributedWallet {

	static final String PATH = "/wallet";

	static final String TRANSFER_PATH = "/transfer";
	@POST
	@Path(TRANSFER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] transfer(String request)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	static final String ATOMIC_TRANSFER_PATH = "/atomicTransfer";
	@POST
	@Path(ATOMIC_TRANSFER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] atomicTransfer(String request)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	static final String BALANCE_PATH = "/balance";
	@POST
	@Path(BALANCE_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] balance(String request);

	static final String LEDGER_PATH = "/ledger";
	@POST
	@Path(LEDGER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] ledger(String request);
	
	
	static final String PUT_OPI_PATH = "/PutOrderPreservingInt/";
	@POST
	@Path(PUT_OPI_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] putOrderPreservingInt(String request); // TODO: precisa de assinatura?
	
	static final String GET_OPI_PATH = "/GetOrderPreservingInt/";
	@POST
	@Path(GET_OPI_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] getOrderPreservingInt(String request);
	
	static final String GET_BETWEEN_OPI_PATH = "/GetBetweenOrderPreservingInt/";
	@POST
	@Path(GET_BETWEEN_OPI_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] getBetween(String request);
	
}