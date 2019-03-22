package rest;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import wallet.Wallet;

public class RESTWalletClient implements Wallet {
	
	private WebTarget target;
	
	public RESTWalletClient(String server_location) { 

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri(server_location).build();

		target = client.target(baseURI);
	}

	// TODO: verificar se a passagem de parametros está correta
	
	// TODO: Colocar as repetições de pedidos como em SD
	
	@Override
	public int createMoney(String who, int amount) {
		Response response = target.path(Wallet.PATH + "/create/").queryParam("who", who).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			int result = response.readEntity(Integer.class);
			return result;
		} else
			throw new RuntimeException("WalletClient Create: " + response.getStatus());
	}

	@Override
	public boolean transfer(String from, String to, int amount) {
		Response response = target.path(Wallet.PATH + "/transfer/").queryParam("from", from).queryParam("to", to).queryParam("amount", amount).request().post(Entity.entity(amount, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			boolean result = response.readEntity(Boolean.class);
			return result;
		} else
			throw new RuntimeException("WalletClient Transfer: " + response.getStatus());
	}

	@Override
	public int currentAmount(String who) {
		Response response = target.path(Wallet.PATH + "/" + who + "/").request().get();
		if (response.getStatus() == 200) {
			int balance = response.readEntity(Integer.class);
			return balance;
		} else
			throw new RuntimeException("WalletClient currentAmount: " + response.getStatus());
	}
	
}
