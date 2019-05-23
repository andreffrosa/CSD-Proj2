package utils;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wallet.GetBetweenOP;
import wallet.UpdOp;

public class Serializor {

	public static <T> String serializeList(List<T> list) {
		String[] temp = new String[list.size()];
		
		Gson gson = new GsonBuilder().create();
		
		int i = 0;
		for(T element : list) {
			temp[i++] = gson.toJson(element);
		}
		
		return gson.toJson(temp);
	}
	
	public static <T> List<T> deserializeList(String json, Class<T> c) {
		
		Gson gson = new GsonBuilder().create();
		
		String[] temp = gson.fromJson(json, String[].class);
		
		List<T> list = new ArrayList<>(temp.length);
		
		for(String s : temp) {
			list.add((T) gson.fromJson(s, c));
		}
		
		return list;
	}
	
/*	public static String serializeUpds() {
		
	}
	
	public static List<UpdOp> deSerializeUpds(String json) {
		
	}*/
	
	public static String serializeEntryList(List<Entry<String, Long>> list) {
		
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
	
	public static List<Entry<String, Long>> deserializeEntryList(String json) {
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
