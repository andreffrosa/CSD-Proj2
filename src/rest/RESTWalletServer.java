package rest;

import java.net.URI;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpsServer;

import bft.BFTReplicatedWallet;
import rest.tls.ClientCertificateVerifier;

@SuppressWarnings("restriction")
public class RESTWalletServer {

	// Constants
	private static final String PATH = "./tls/Server/";
	private static final String SERVER_KEYSTORE = PATH + "server-ks.jks";
	private static final String SERVER_KEYSTORE_PWD = "CSD1819";
	private static final String SERVER_TRUSTSTORE = PATH + "server-ts.jks";
	private static final String SERVER_TRUSTSTORE_PWD = "CSD1819";

	public static void main(String[] args) throws Exception {

		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("javax.net.ssl.keyStore", SERVER_KEYSTORE);
		System.setProperty("javax.net.ssl.keyStorePassword", SERVER_KEYSTORE_PWD);
		System.setProperty("javax.net.ssl.trustStore", SERVER_TRUSTSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", SERVER_TRUSTSTORE_PWD);

		if (args.length < 1) {
			System.err.println("Usage: RESTWalletServer <id> [port]");
			System.exit(-1);
		}

		int id = Integer.parseInt(args[0]);

		boolean byzantine = false;
		if (args.length > 1) {
			byzantine = Boolean.parseBoolean(args[1]);
		}

		boolean authenticate_clients = false;
		if (args.length > 2) {
			authenticate_clients = Boolean.parseBoolean(args[2]);
		}

		int port = 8080 + id;
		if (args.length > 3) {
			port = Integer.parseInt(args[3]);
		}

		DistributedWallet wallet = new BFTReplicatedWallet(id, byzantine);

		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

		HttpsServer server = null;
		SSLContext ctx = null;

		ResourceConfig config = new ResourceConfig();
		config.register(wallet);

		if (authenticate_clients) {
			ClientCertificateVerifier ccv = new ClientCertificateVerifier();
			ctx = ccv.init(SERVER_KEYSTORE, SERVER_KEYSTORE_PWD, SERVER_TRUSTSTORE, SERVER_TRUSTSTORE_PWD);

			HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

			server = (HttpsServer) JdkHttpServerFactory.createHttpServer(baseUri, config, ctx, false);

			ccv.configureHttps(server);
		} else {
			ctx = SSLContext.getDefault();

			server = (HttpsServer) JdkHttpServerFactory.createHttpServer(baseUri, config, ctx, false);
		}

		server.start();

		System.out.println("\n\t#######################################################"
						 + "\n\t    REST Wallet Server ready @ " + baseUri 
						 + "\n\t        Client Authentication: " + authenticate_clients 
						 + "\n\t                    Byzantine: " + byzantine
						 + "\n\t#######################################################");

	}

}
