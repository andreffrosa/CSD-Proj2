package rest;

import java.net.URI;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpsServer;

import bft.BFTReplicatedWallet;
import tls.ClientCertificateVerifier;
import wallet.Wallet;

public class RESTWalletServer {
	
	private static final String PATH = "./tls/Server/"; // "/home/sd"
	private static final String SERVER_KEYSTORE = PATH + "server-ks.jks";
	private static final String SERVER_KEYSTORE_PWD = "CSD1819";
	private static final String SERVER_TRUSTSTORE = PATH + "server-ts.jks";
	private static final String SERVER_TRUSTSTORE_PWD = "CSD1819";

	@SuppressWarnings("restriction")
	public static void main(String[] args) throws Exception {
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		// TODO: É preciso?
		System.setProperty("javax.net.ssl.keyStore",  SERVER_KEYSTORE);
		System.setProperty("javax.net.ssl.keyStorePassword", SERVER_KEYSTORE_PWD);
		System.setProperty("javax.net.ssl.trustStore", SERVER_TRUSTSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", SERVER_TRUSTSTORE_PWD);
		
		if(args.length < 1) {
			System.err.println("Usage: RESTWalletServer <id> [port]");
			System.exit(-1);
		}
		
		int id = Integer.parseInt(args[0]);
		
		int port = 8080 + id;
		if( args.length > 1) {
			port = Integer.parseInt(args[1]);
		}

		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();
		
		ClientCertificateVerifier ccv = new ClientCertificateVerifier();
		SSLContext ctx = ccv.init(SERVER_KEYSTORE, SERVER_KEYSTORE_PWD, SERVER_TRUSTSTORE, SERVER_TRUSTSTORE_PWD);
		
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

		ResourceConfig config = new ResourceConfig();
		Wallet wallet = new BFTReplicatedWallet(id);
		config.register(wallet);

		HttpsServer server = (HttpsServer) JdkHttpServerFactory.createHttpServer(baseUri, config, ctx, false);

		ccv.configureHttps(server);

		server.start();
		
		//ResourceConfig config = new ResourceConfig();
		//config.register(new BFTReplicatedWallet(id) );

		/*HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);*/

		System.out.println("REST Wallet Server ready @ " + baseUri);
	}

}
