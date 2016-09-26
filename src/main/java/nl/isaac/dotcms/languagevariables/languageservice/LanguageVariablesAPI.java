package nl.isaac.dotcms.languagevariables.languageservice;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariableCacheKey;
import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.util.Configuration;
import nl.isaac.dotcms.languagevariables.util.ContentletQuery;
import nl.isaac.dotcms.languagevariables.util.LanguageVariableContentlet;
import nl.isaac.dotcms.languagevariables.util.LanguageVariableFactory;
import nl.isaac.dotcms.languagevariables.util.RequestUtil;

public class LanguageVariablesAPI {

	private final String languageId;
	private final String hostIdentifier;
	private final boolean live;

	public LanguageVariablesAPI(HttpServletRequest request) {
		this(new RequestUtil(request).getLanguage(), new RequestUtil(request).getCurrentHost().getIdentifier(),
				new RequestUtil(request).isLiveMode());
	}

	public LanguageVariablesAPI(String languageId, String hostIdentifier, boolean live) {
		this.languageId = languageId;
		this.hostIdentifier = hostIdentifier;
		this.live = live;
	}

	public String getValue(String key) {
		LanguageVariableCacheKey cacheKey = new LanguageVariableCacheKey(key, languageId, hostIdentifier, live);
		LanguageVariablesCacheGroupHandler cache = LanguageVariablesCacheGroupHandler.getInstance();

		String value = cache.get(cacheKey.getKey());

		return value;
	}

	public List<KeyValuePair<String, String>> getKeysWithPrefixes(List<String> prefixes) {
		List<KeyValuePair<String, String>> keyValuePairs = new ArrayList<KeyValuePair<String, String>>();

		List<LanguageVariableContentlet> results = new ArrayList<LanguageVariableContentlet>();

		for (String prefix : prefixes) {

			// prevent searching for '*' if an empty prefix is given
			if (UtilMethods.isSet(prefix)) {
				results.addAll(getLanguageVariablesContentletsWithKey(prefix + "*"));
			}
		}

		// convert them to key-value pairs
		for (LanguageVariableContentlet languageVariable : results) {
			KeyValuePair<String, String> keyValuePair = new KeyValuePair<String, String>(languageVariable.getKey(),
					languageVariable.getValue());
			keyValuePairs.add(keyValuePair);
		}

		return keyValuePairs;
	}

	public List<LanguageVariableContentlet> getLanguageVariablesContentletsWithKey(String key) {
		// retrieve all the contentlets with the prefix
		ContentletQuery contentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		contentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		contentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField(), key);
		contentletQuery.addLanguage(languageId);

		if (live) {
			contentletQuery.addLive(true);
		} else {
			contentletQuery.addWorking(true);
		}

		contentletQuery.addDeleted(false);

		List<Contentlet> results = contentletQuery.executeSafe();
		return LanguageVariableFactory.getLanguageVariablesFromList(results);
	}

	public String getLanguageVariableContentletURL(String key, String languageId, String referer) {
		String url = null;

		// Language Variable contentlet exists for current language but is not published
		LanguageVariableContentlet unpublishedLanguageVariableContentlet = getUnpublishedLanguageVariableContentletByKeyAndLanguage(key, languageId);

		if (unpublishedLanguageVariableContentlet != null) {
			url = "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=edit&inode="
					+ unpublishedLanguageVariableContentlet.getInode()
					+ "&referer=" + referer;
		}

		// Language Variable contentlet exists but doesn't for the current language
		else {
			LanguageVariableContentlet existingLanguageVariableContentlet = getExistingLanguageVariableContentletInAnotherLanguageByKey(key);

			if (existingLanguageVariableContentlet != null) {
				// siblingId is equal to the existing node ID
				String sibling = existingLanguageVariableContentlet.getInode();

				// siblingStructure URL Query Parameter is not required
				// (&_EXT_11_sibblingStructure=)
				url = "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&_EXT_11_sibbling="
						+ sibling
						+ "&_EXT_11_cmd=edit&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&lang="
						+ languageId + "&inode=&host=" + hostIdentifier + "&referer=" + referer + "&folder=";
			}
		}

		return url;
	}

	private LanguageVariableContentlet getUnpublishedLanguageVariableContentletByKeyAndLanguage(String key, String languageId) {
		ContentletQuery unpublishedContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		unpublishedContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		unpublishedContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField(), key);
		unpublishedContentletQuery.addLanguage(languageId);
		unpublishedContentletQuery.addWorking(true);
		unpublishedContentletQuery.addLive(false);
		unpublishedContentletQuery.addDeleted(false);

		Contentlet unpublishedContentlet = unpublishedContentletQuery.executeSafeSingle();

		if (unpublishedContentlet != null) {
			return new LanguageVariableContentlet(unpublishedContentlet);
		}

		return null;
	}

	private LanguageVariableContentlet getExistingLanguageVariableContentletInAnotherLanguageByKey(String key) {
		List<Language> languages = APILocator.getLanguageAPI().getLanguages();

		for (Language language : languages) {
			if (language.getId() == Long.valueOf(languageId)) {
				continue;
			}

			ContentletQuery existingContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
			existingContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
			existingContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField(), key);
			existingContentletQuery.addLanguage(language.getId());

			Contentlet existingContentlet = existingContentletQuery.executeSafeSingle();

			if (existingContentlet != null) {
				return new LanguageVariableContentlet(existingContentlet);
			}
		}

		return null;
	}
}
