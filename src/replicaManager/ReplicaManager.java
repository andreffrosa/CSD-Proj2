package replicaManager;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Cryptography;
import utils.IO;

public class ReplicaManager implements ReplicaManagerService {

	private static final String PASSWORD_HASH = "Ni54DZjSFXGONgao/GZH4fXd2KjbYJNQtBbKwpX3R54=";

	private Process process;
	private String id;

	public synchronized String launch(LaunchRequest request) {

		// If replica is not runnig yet
		if(process == null) {
			String password = request.password;
			String fileName = request.fileName;
			String hash = request.hash;
			String className = request.className;
			String[] args = request.getArgs();

			// Validate Password
			validatePassword(password);

			// Verify Hash
			String new_hash = Cryptography.computeHash(IO.loadFile(fileName));
			if( !hash.equals(new_hash) ) {
				throw new HashMisMatchWebException("Hash does not match!");
			}

			// Lauch new process
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome +
					File.separator + "bin" +
					File.separator + "java";
			String classpath = System.getProperty("java.class.path");

			List<String> container_args = new ArrayList<>();
			container_args.add(javaBin);
			container_args.add("-cp");
			container_args.add(classpath);
			container_args.add(ReplicaContainer.class.getName());
			container_args.add(fileName);
			container_args.add(className);
			container_args.addAll(Arrays.asList(args));

			ProcessBuilder builder = new ProcessBuilder(container_args);
			builder.directory(new File(System.getProperty("user.dir")));
			builder.inheritIO();
			
			try {
				process = builder.start();
				id = "" + new SecureRandom().nextLong();
				System.out.println("Lauched replica: " + id);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return id;
		} else {
			return id; // TODO: o que fazer aqui?
		}
	}

	public synchronized boolean stop(StopRequest request) {
		
		if(request.id.equals(id)) {
			validatePassword(request.password);

			if(process != null) {
				process.destroy();
				process = null;
			}
			
			System.out.println("Stopped replica: " + id);
			
			id = null;
			
			return true;
		}
		
		return false;
	}

	private static void validatePassword(String password) {
		String new_hash = Cryptography.computeHash(password);
		if(!new_hash.equals(PASSWORD_HASH))
			throw new InvalidPasswordWebException(password + " is not a valid password!");
	}

}
