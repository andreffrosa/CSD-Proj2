package replicaManager;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class ReplicaContainer {

	public static void main(String[] args) {
		String fileName = args[0];
		String className = args[1];
		
		String[] r_args = Arrays.copyOfRange(args, 2, args.length);
		
		try {
			System.out.println();
			URL[] urls = new URL[] {new File(fileName).toURI().toURL()};

			@SuppressWarnings("resource")
			URLClassLoader classLoader = new URLClassLoader(urls);

			@SuppressWarnings("rawtypes")
			Class c = classLoader.loadClass(className);
			@SuppressWarnings("unchecked")
			Method m = c.getDeclaredMethod("main", String[].class);

			m.invoke(null, (Object)r_args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
