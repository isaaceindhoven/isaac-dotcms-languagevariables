package nl.isaac.dotcms.languagevariables.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.isaac.dotcms.languagevariables.util.Configuration;

public class LanguageListCacheItemHandler implements ItemHandler<List> {

	@Override
	public List<String> get(String key) {
		
		//For each language a new list (CacheListKeysWithoutValue + languageId)
		if(key.startsWith(Configuration.CacheListKeysWithoutValue)) {
			return new ArrayList<String>();
		}
		
		return null;
	}

	@Override
	public Map<String, List> getInitialCache() {
		return new HashMap<String, List>();
	}
	
}
