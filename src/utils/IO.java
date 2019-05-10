package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IO {

	public static byte[] loadFile(String path) {
		File file = new File(path);
		
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new byte[0];
	}
	
	
	public static String loadTextFile(String path) {
		String text = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader(path));
			text = r.readLine();
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}

	public static void storeTextFile(String text, String path) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(path, false));
			w.write(text);
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object loadObject(String path, Class c) {
		Gson gson = new GsonBuilder().create();
		String json = loadTextFile(path);
		return gson.fromJson(json, c);
	}

	public static void storeObject(Object obj, String path) {
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(obj);
		storeTextFile(json, path);
	}

}
