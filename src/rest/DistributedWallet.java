package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import bft.BFTReply;
import rest.entities.BalanceRequest;
import rest.entities.TransferRequest;
import wallet.InvalidNumberException;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path(DistributedWallet.PATH)
public interface DistributedWallet {

	static final String PATH = "/wallet";

	@POST
	@Path("/transfer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] transfer(TransferRequest request) throws InvalidNumberException;

	@POST
	@Path("/balance")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] balance(BalanceRequest request);

	@GET
	@Path("/ledger")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] ledger();
}