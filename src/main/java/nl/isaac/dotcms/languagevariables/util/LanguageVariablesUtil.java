package nl.isaac.dotcms.languagevariables.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.view.context.ViewContext;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.languagevariables.cache.LanguageListCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.cache.LanguageVariableCacheKey;
import nl.isaac.dotcms.languagevariables.languageservice.IncompleteLanguageVariable;
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
				if (value == null) {
					if (Configuration.isNotFoundShowInDefaultLanguage()) {
						value = addKeyToCacheAndReturnValueInDefaultLanguage(key, languageId);
						if (value != null) {
							return value;
						}
					}
					return addKeyToCacheAndReturnKey(key, languageId);
				} else {
					return new RenderTool().eval(context.getVelocityContext(), value);
				}
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

		List<String> keyList  = LanguageListCacheGroupHandler.getInstance().get(Configuration.CacheListKeysWithoutValue + languageId);

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
	 * Add key without value to a cache list per language, so it can be displayed on the portlet
	 * @param key
	 * @param languageId
	 * @return Value in default language if present
	 */
	@SuppressWarnings("unchecked")
	private String addKeyToCacheAndReturnValueInDefaultLanguage(String key, String languageId) {

		RequestUtil requestUtil = new RequestUtil(request);
		String defaultLanguageId = String.valueOf(APILocator.getLanguageAPI().getDefaultLanguage().getId());

		if (!requestUtil.getLanguage().equals(defaultLanguageId)) {

			List<String> keyList  = LanguageListCacheGroupHandler.getInstance().get(Configuration.CacheListKeysWithoutValue + languageId);

			if(!keyList.contains(key)) {
				keyList.add(key);
			}

			LanguageVariablesAPI languageVariablesAPI = new LanguageVariablesAPI(defaultLanguageId, requestUtil.getCurrentHost().getIdentifier(), requestUtil.isLiveMode());

			return languageVariablesAPI.getValue(key);
		}

		return null;
	}

	/**
	 * Get cache list per language for the portlet
	 * @param languageId
	 * @return List with keys
	 */
	@SuppressWarnings("unchecked")
	public List<IncompleteLanguageVariable> getIncompleteKeys(String languageId, String referer) {
		List<IncompleteLanguageVariable> incompleteLanguageVariables = new ArrayList<>();

		LanguageVariablesAPI languageVariablesAPI = new LanguageVariablesAPI(request);

		List<String> keyList = LanguageListCacheGroupHandler.getInstance().get(Configuration.CacheListKeysWithoutValue + languageId);

		for (String key : keyList) {

			ContentletQuery query = new ContentletQuery(Configuration.getStructureVelocityVarName());
			query.addHostAndIncludeSystemHost(new RequestUtil(request).getCurrentHost().getIdentifier());
			query.addFieldLimitation(true, Configuration.getStructureKeyField(), key);

			Contentlet contentlet = null;
			List<Contentlet> contentlets = query.executeSafe();
			if (contentlets != null && !contentlets.isEmpty()) {
				contentlet = contentlets.get(0);
			}

			LanguageVariableCacheKey workingCacheKey;
			if (contentlet != null) {
				workingCacheKey = new LanguageVariableCacheKey(new LanguageVariableContentlet(contentlet).getKey(), languageId, new RequestUtil(request).getCurrentHost().getIdentifier(), false, contentlet.getIdentifier());
			} else {
				workingCacheKey = new LanguageVariableCacheKey(key, languageId, new RequestUtil(request).getCurrentHost().getIdentifier(), false);
			}

			IncompleteLanguageVariable incompleteLanguageVariable = languageVariablesAPI.getIncompleteLanguageVariable(workingCacheKey, referer);
			if (incompleteLanguageVariable != null) {
				incompleteLanguageVariables.add(incompleteLanguageVariable);
			}
		}

		return incompleteLanguageVariables;
	}

	private boolean shouldReturnKey() {
		return UtilMethods.isSet(request.getParameter(Configuration.getDisplayKeysParameterName()));
	}

}
