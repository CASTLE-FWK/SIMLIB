package castleComponents.objects;

import java.util.concurrent.ConcurrentHashMap;

public class Map<K,T> {
	private ConcurrentHashMap<K, T> theMap = null;
	
	public Map(){
		theMap = new ConcurrentHashMap<K,T>();
		
	}
	
	public T add(K key, T value){
		return theMap.put(key,value);
	}

	public T get(K key){
		return theMap.get(key);
	}
	
	public T remove(K key){
		return theMap.remove(key);
	}
	
	public boolean containsKey(K key){
		return theMap.containsKey(key);
	}
	
	public boolean containsValue(T value){
		return theMap.containsValue(value);
	}
	
	public void replace(K key, T value){
		theMap.replace(key, value);
	}
	
	public int size(){
		return theMap.size();
	}
	
	
	
}
