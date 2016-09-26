package nl.isaac.dotcms.languagevariables.cache;

import java.util.Map;
import java.util.Map.Entry;

import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.util.Logger;

/**
 * Class that handles the dotCMS cache. It uses an ItemHandler<T> to retrieve items that aren't stored in the cache yet

 * @param <T>	The type of object that will be cached.
 */
public class CacheGroupHandler<T> {
	
	private final String groupName;
	protected final ItemHandler<T> itemHandler;
	protected final Class<T> itemClass;
	
	public CacheGroupHandler(String groupName, ItemHandler<T> itemHandler, Class<T> itemClass) {
		this.groupName = groupName;
		this.itemHandler = itemHandler;
		this.itemClass = itemClass;
	}
	
	/**
	 * @param key	The key of the cached value 
	 * @return	The cached value belonging to the given key. If the key is not in the 
	 * 			cache yet, it will be added to it. Null if no such key is available in the 
	 * 			cache or in the source.
	 */
	@SuppressWarnings("unchecked")
	public T get(String key) {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		Object o = null;
		
		if(o == null) {
			try {
				o = cache.get(key, groupName);
			} catch (DotCacheException e) {
				Logger.error(this, String.format("DotCacheException for Group '%s', key '%s', message: %s", groupName, key, e.getMessage()), e);
			}
		}
		if(o == null || !itemClass.isInstance(o)) {
			T t = itemHandler.get(key);
			
			if (t != null) {
				put(key, t);				
			}
			
			return t;
		} else {
			return (T)o;
		}
	}
	
	/**
	 * @param key	The key to use when putting this object in the cache
	 * @param t		An object to put in the cache. Can be null. 
	 */
	public void put(String key, T t) {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.put(key, t, groupName);
	}
	
	/**
	 * Updates the cache for the given key using the value from the source
	 */
	public void updateWithItemHandler(String key) {
		remove(key);
		put(key, itemHandler.get(key));
	}
	
	/**
	 * 
	 * @param key The key to remove from the cache
	 */
	public void remove(String key) {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.remove(key, groupName);
	}
	
	/**
	 * Removes all items from the cache and fills it with the initial cache
	 */
	public void fillInitialCache() {
		removeAll();
		Map<String, T> initialCache = itemHandler.getInitialCache();
		for(Entry<String, T> entry: initialCache.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Removes all items from the cache
	 */
	public void removeAll() {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.flushGroup(groupName);
	}
	
}
