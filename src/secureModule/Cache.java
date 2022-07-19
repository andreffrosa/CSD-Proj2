package secureModule;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class Cache {

	private static final int DEFAULT_CACHE_SIZE = 10;
	
	private HashMap<String,Object> cache;
	private Queue<String> cache_meta;
	private int cache_size;
	
	public Cache() {
		this(DEFAULT_CACHE_SIZE);
	}
	
	public Cache(int size) {
		cache = new HashMap<>(size);
		cache_meta = new ArrayDeque<>(size);
		cache_size = size;
	}
	
	public synchronized Object get(String key) {
		return cache.get(key);
	}
	
	public synchronized void add(String key, Object result) {
		if(cache.size() == cache_size) {
			String rm = cache_meta.poll();
			if(rm != null) {
				cache.remove(rm);
			}
		}
		
		cache.put(key, result);
		cache_meta.add(key);
	}
	
	public boolean isEmpty() {
		return cache.isEmpty();
	}
	
	public int size() {
		return cache.size();
	}
	
	
}
