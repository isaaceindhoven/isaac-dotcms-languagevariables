package nl.isaac.dotcms.languagevariables.cache;

import java.util.Map;

/**
 * Interface for handling an item that is not available in the cache
 * 
 * @param <T>
 */
public interface ItemHandler<T> {
	
	/**
	 * @param key	The key that is used to get the value from the source (the dotCMS DB for instance) 
	 * @return	The value for the given key, null if not found. 
	 */
	public T get(String key);
	
	/**
	 * @return	An cache filled with only the values that are always needed. Most of the times this means it will be an empty Map.
	 */
	public Map<String,T> getInitialCache();
}