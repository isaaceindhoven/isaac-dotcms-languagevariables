package nl.isaac.dotcms.languagevariables.cache;

public class LanguageVariablesCacheGroupHandler extends CacheGroupHandler<String> {
	
	public static final String GROUP_NAME = "LANGUAGE_VARIABLES";
	private static LanguageVariablesCacheGroupHandler cache;
	
	private LanguageVariablesCacheGroupHandler() {
		super(GROUP_NAME, new LanguageVariablesCacheItemHandler(), String.class);
	}
	
	public static LanguageVariablesCacheGroupHandler getInstance() {
		if(cache == null) {
			cache = new LanguageVariablesCacheGroupHandler();
		}
		
		return cache;
	}
	

}
