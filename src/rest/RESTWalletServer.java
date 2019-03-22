package rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

import bft.BFTReplicatedWallet;
import wallet.Wallet;

public class RESTWalletServer {

	public static void main(String[] args) throws Exception {
		
		if(args.length < 1) {
			System.err.println("Usage: RESTWalletServer <id> [port]");
			System.exit(-1);
		}
		
		int id = Integer.parseInt(args[0]);
		
		int port = 8080 + id;
		if( args.length > 1) {
			port = Integer.parseInt(args[1]);
		}

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();
		
		ResourceConfig config = new ResourceConfig();
		config.register( new BFTReplicatedWallet(id) );

		/*HttpServer server = */JdkHttpServerFactory.createHttpServer(baseUri, config);

		System.out.println("REST Wallet Server ready @ " + baseUri);
	}

}
