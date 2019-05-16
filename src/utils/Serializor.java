package utils;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializor {

	public static String serializeList(List<Entry<String, Long>> list) {
		
		String[] keys = new String[list.size()];
		Long[] values = new Long[list.size()];
		int i = 0;
		for(Entry<String, Long> e : list) {
			keys[i] = e.getKey();
			values[i]= e.getValue();
			i++;
		}
		Gson gson = new GsonBuilder().create();
		String k = gson.toJson(keys);
		String v = gson.toJson(values);
		
		return gson.toJson(new String[] {k, v});
	}
	
	public static List<Entry<String, Long>> deserialize(String json) {
		Gson gson = new GsonBuilder().create();
		String[] KV = gson.fromJson(json, String[].class);
		String[] keys = gson.fromJson(KV[0], String[].class);
		Long[] values = gson.fromJson(KV[1], Long[].class);
		
		List<Entry<String, Long>> list = new ArrayList<>(keys.length);
		
		for(int i = 0; i < keys.length; i++) {
			list.add(new AbstractMap.SimpleEntry<String, Long>(keys[i], values[i]));
		}
		
		return list;
	}
	
}
