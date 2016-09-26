package nl.isaac.dotcms.languagevariables.cache;

public class LanguageVariablesCacheCleaner {

	public static void flush(String propertyKey, String hostIdentifier, String languageId) {
		LanguageVariableCacheKey liveCacheKey = new LanguageVariableCacheKey(propertyKey, languageId, hostIdentifier, true);
		LanguageVariableCacheKey workingCacheKey = new LanguageVariableCacheKey(propertyKey, languageId, hostIdentifier, false);
		
		LanguageVariablesCacheGroupHandler cache = LanguageVariablesCacheGroupHandler.getInstance();
		
		cache.remove(liveCacheKey.getKey());
		cache.remove(workingCacheKey.getKey());
	}
	
}
