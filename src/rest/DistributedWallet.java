package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import bft.BFTReply;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path(DistributedWallet.PATH)
public interface DistributedWallet {
	
	static final String PATH = "/wallet";

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	BFTReply createMoney( @QueryParam("who") String who, @QueryParam("amount") int amount );

	@POST
	@Path("/transfer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	BFTReply transfer( @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("amount") int amount );

	@GET
	@Path("/{who}")
	@Produces(MediaType.APPLICATION_JSON)
	BFTReply currentAmount( @PathParam("who") String who );

}