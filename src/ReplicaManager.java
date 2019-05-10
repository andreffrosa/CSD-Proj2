import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplicaManager implements ReplicaManagerService {

	// 1 réplica apenas ou isto lança todas?
	private Process process;

	public void launch(LaunchRequest request) {

		String fileName = request.fileName;
		byte[] hash = request.getHash();
		String className = request.className;
		String[] args = request.getArgs();
		
		// TODO: verificar hash

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

	public void stop() {
		process.destroy();
	}

}
