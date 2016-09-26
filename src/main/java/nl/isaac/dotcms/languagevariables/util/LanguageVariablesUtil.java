package nl.isaac.dotcms.languagevariables.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.view.context.ViewContext;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.languagevariables.cache.LanguageListCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.languageservice.LanguageVariablesAPI;

public class LanguageVariablesUtil {
	
	private final ViewContext context;
	private final HttpServletRequest request;
	
	public LanguageVariablesUtil(ViewContext context) {
		super();
		this.context = context;
		this.request = context.getRequest();
	}
	
	/**
	 * 
	 * @param key
	 * @return 	The value of the key in the current language. If there is no current language 
	 * 			defined, the default language will be used. Note that when a certain request 
	 * 			variable is set, only the key will be returned for debugging purposes.
	 * 
	 */
	public String get(String key) {
		try {
			if(shouldReturnKey()) {
				return key;
			} else {
				String language = (String) request.getSession().getAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE);
				
				if (language == null) {
					language = String.valueOf(APILocator.getLanguageAPI().getDefaultLanguage().getId());
				}
				
				return get(key, language);
			}
		} catch (Throwable t) {
			Logger.error(this, "Error occured while returning value of key: " + key, t);
			throw new RuntimeException(t);
		}
	}

	/**
	 * 
	 * @param key
	 * @param languageId
	 * @return The value of the key in the given language. Note that when a certain request 
	 * 			variable is set, only the key will be returned for debugging purposes.
	 */
	public String get(String key, String languageId) {
		if(shouldReturnKey()) {
			return key;
		} else {
			LanguageVariablesAPI languageVariablesAPI = new LanguageVariablesAPI(request);
			String value = languageVariablesAPI.getValue(key);
			
			try {
				return (value == null) ? addKeyToCacheAndReturnKey(key, languageId) : new RenderTool().eval(context.getVelocityContext(), value);
			} catch (Exception e) {
				Logger.error(this, "Error occured while returning value for key: " + key, e);
			}
		}
		return null;
	}
	
	/**
	 * Add key without value to a cache list per language, so it can be displayed on the portlet
	 * @param key
	 * @param languageId
	 * @return Key or a replacement from the configuration file
	 */
	@SuppressWarnings("unchecked")
	private String addKeyToCacheAndReturnKey(String key, String languageId) {
		List<String> keyList = (List<String>) LanguageListCacheGroupHandler.getInstance().get(Configuration.CacheListKeysWithoutValue + languageId);
		
		if(!keyList.contains(key)) {
			keyList.add(key);
		}
		
		//If true show key otherwise get the replacement value from the configuration file
		if(!Configuration.isValueOfKeyEmptyShowKey()) {
			if(Configuration.isReplacementValueAnEmptyString()) {
				key = "";
			} else {
				key = Configuration.getReplacementValueIfValueIsEmpty();				
			}
		}
		
		return key;
	}
	
	/**
	 * Get cache list per language for the portlet
	 * @param languageId
	 * @return List with keys
	 */
	@SuppressWarnings("unchecked")
	public List<String> getKeysWithoutValue(String languageId) {
		List<String> keyList = (List<String>) LanguageListCacheGroupHandler.getInstance().get(Configuration.CacheListKeysWithoutValue + languageId);
		return keyList;
	}
	
	private boolean shouldReturnKey() {
		return UtilMethods.isSet(request.getParameter(Configuration.getDisplayKeysParameterName()));
	}
	
}
