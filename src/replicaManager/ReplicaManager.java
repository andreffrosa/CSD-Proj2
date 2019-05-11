package replicaManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Cryptography;
import utils.IO;

public class ReplicaManager implements ReplicaManagerService {
	
	private static final String PASSWORD_HASH = "Ni54DZjSFXGONgao/GZH4fXd2KjbYJNQtBbKwpX3R54=";

	// 1 réplica apenas ou isto lança todas?
	private Process process;

	// TODO: meter para retornar boolean?
	public void launch(LaunchRequest request) {

		String password = request.password;
		String fileName = request.fileName;
		String hash = request.hash;
		String className = request.className;
		String[] args = request.getArgs();
		
		validatePassword(password);
		
		// TODO: verificar hash
		String new_hash = Cryptography.computeHash(IO.loadFile(fileName));
		if( !hash.equals(new_hash) ) {
			throw new HashMisMatchWebException("Hash does not match!");
		}

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
        
		builder.inheritIO();
		try {
			process = builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop(String password) {
		validatePassword(password);
		
		if(process != null)
			process.destroy();
	}
	
	private static void validatePassword(String password) {
		String new_hash = Cryptography.computeHash(password);
		if(!new_hash.equals(PASSWORD_HASH))
			throw new InvalidPasswordWebException(password + " is not a valid password!");
	}

}
