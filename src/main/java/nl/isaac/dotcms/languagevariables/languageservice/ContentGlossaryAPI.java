package nl.isaac.dotcms.languagevariables.languageservice;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariableCacheKey;
import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.util.Configuration;
import nl.isaac.dotcms.languagevariables.util.ContentletQuery;
import nl.isaac.dotcms.languagevariables.util.RequestUtil;

import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.UtilMethods;

public class ContentGlossaryAPI {
	
	private final String languageId;
	private final String hostIdentifier;
	private final boolean live;
	
	public ContentGlossaryAPI(HttpServletRequest request) {
		this(new RequestUtil(request).getLanguage(), new RequestUtil(request).getCurrentHost().getIdentifier(), new RequestUtil(request).isLiveMode());
	}

	public ContentGlossaryAPI(String languageId, String hostIdentifier, boolean live) {
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
		
		List<Contentlet> results = new ArrayList<Contentlet>();
		
		for(String prefix : prefixes) {
			
			// prevent searching for '*' if an empty prefix is given
			if(UtilMethods.isSet(prefix)) {
				results.addAll(getContentletsWithKey(prefix + "*"));
			}
		}
		
		//convert them to key-value pairs
		for(Contentlet contentlet: results) {
			KeyValuePair<String, String> keyValuePair = new KeyValuePair<String, String>(
					contentlet.getStringProperty(Configuration.getStructureKeyField()), 
					contentlet.getStringProperty(Configuration.getStructureValueField()));
			keyValuePairs.add(keyValuePair);
		}
		
		return keyValuePairs;
	}

	public List<Contentlet> getContentletsWithKey(String key) {
		//retrieve all the contentlets with the prefix
		ContentletQuery contentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		contentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		contentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField(), key);
		contentletQuery.addLanguage(languageId);

		if(live) {
			contentletQuery.addLive(true);
		} else {
			contentletQuery.addWorking(true);
		}
		
		List<Contentlet> results = contentletQuery.executeSafe();
		return results;
	}
}
