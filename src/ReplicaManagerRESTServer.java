import java.net.URI;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpsServer;

import rest.tls.ClientCertificateVerifier;

@SuppressWarnings("restriction")
public class ReplicaManagerRESTServer {

	// TODO: atualizar estas constantes e criar uma keystore para isto
	
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
			System.err.println("Usage: ReplicaManagerRESTServer <port> [authenticate-clients]");
			System.exit(-1);
		}

		int port = 8080;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		
		boolean authenticate_clients = false;
		if (args.length == 2) {
			authenticate_clients = Boolean.parseBoolean(args[2]);
		}

		ReplicaManager manager = new ReplicaManager();

		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

		HttpsServer server = null;
		SSLContext ctx = null;

		ResourceConfig config = new ResourceConfig();
		config.register(manager);

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
						 + "\n\t REST ReplicaManagerServer ready @ " + baseUri 
						 + "\n\t            Client Authentication: " + authenticate_clients 
						 + "\n\t#######################################################");

	}

}
