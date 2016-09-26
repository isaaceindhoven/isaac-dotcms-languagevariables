package nl.isaac.dotcms.languagevariables.cache;

import java.util.List;

public class LanguageListCacheGroupHandler extends CacheGroupHandler<List> {
	
	public static final String GROUP_NAME = "LANGUAGE_LIST_CACHE";
	private static LanguageListCacheGroupHandler cache;
	
	private LanguageListCacheGroupHandler() {
		super(GROUP_NAME, new LanguageListCacheItemHandler(), List.class);
	}
	
	public static LanguageListCacheGroupHandler getInstance() {
		if(cache == null) {
			cache = new LanguageListCacheGroupHandler();
		}
		
		return cache;
	}

}
