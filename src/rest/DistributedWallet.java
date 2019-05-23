package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.InvalidTypeException;
import wallet.exceptions.NotEnoughMoneyException;

@Path(DistributedWallet.PATH)
public interface DistributedWallet {

	static final String PATH = "/wallet";

	static final String TRANSFER_PATH = "/transfer";
	@POST
	@Path(TRANSFER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] transfer(String request)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	static final String ATOMIC_TRANSFER_PATH = "/atomicTransfer";
	@POST
	@Path(ATOMIC_TRANSFER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] atomicTransfer(String request)
			throws InvalidAddressException, InvalidAmountException, InvalidSignatureException, NotEnoughMoneyException;

	static final String BALANCE_PATH = "/balance";
	@POST
	@Path(BALANCE_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] balance(String request);

	static final String LEDGER_PATH = "/ledger";
	@POST
	@Path(LEDGER_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] ledger(String request);

	static final String CREATE_PATH = "/create";
	@POST
	@Path(CREATE_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] create(String request);

	static final String GET_PATH = "/get";
	@POST
	@Path(GET_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] get(String request) throws InvalidAddressException;

	static final String GET_BETWEEN_PATH = "/getBetween";
	@POST
	@Path(GET_BETWEEN_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] getBetween(String request);

	static final String SET_PATH = "/set";
	@POST
	@Path(SET_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] set(String request) throws InvalidTypeException;

	static final String SUM_PATH = "/sum";
	@POST
	@Path(SUM_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] sum(String request) throws InvalidAddressException, InvalidTypeException;

	static final String COMPARE_PATH = "/sum";
	@POST
	@Path(COMPARE_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] compare(String request) throws InvalidAddressException, InvalidTypeException, InvalidOperationException;

	static final String COND_UPD_PATH = "/condUpd";
	@POST
	@Path(COND_UPD_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] cond_upd(String request) throws InvalidAddressException, InvalidTypeException, InvalidOperationException;


}