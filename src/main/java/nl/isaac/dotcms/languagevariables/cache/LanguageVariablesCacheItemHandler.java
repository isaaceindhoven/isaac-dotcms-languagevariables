package nl.isaac.dotcms.languagevariables.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.isaac.dotcms.languagevariables.languageservice.LanguageVariablesAPI;
import nl.isaac.dotcms.languagevariables.util.LanguageVariable;

import com.dotmarketing.beans.Host;
import com.dotmarketing.util.Logger;

public class LanguageVariablesCacheItemHandler implements ItemHandler<String> {

	@Override
	public String get(String key) {
		
		LanguageVariableCacheKey keyObject = LanguageVariableCacheKey.createInstanceWithKey(key);
		
		String propertyKey = keyObject.getPropertyKey();
		String languageId = keyObject.getLanguageId();
		String hostIdentifier = keyObject.getHostId();
		boolean live = keyObject.getLive();
		
		LanguageVariablesAPI contentGlossaryAPI = new LanguageVariablesAPI(languageId, hostIdentifier, live);
		List<LanguageVariable> results = contentGlossaryAPI.getLanguageVariablesWithKey(propertyKey);
		
		return getHostSpecificResult(results);
	}
		
	/**
	 * When there is more than one result in the results, this method will extract a host specific version (we assume there is only 1 host specific version)
	 * When there is only one result then that result will be returned
	 */
	private String getHostSpecificResult(List<LanguageVariable> results) {
		if (results != null && results.size() > 0) {
			LanguageVariable languageVariable = null;
			if(results.size() > 1) {
				List<LanguageVariable> resultsWithoutSystemHost = removeSystemHost(results);
				if(resultsWithoutSystemHost.size() == 0) {
					languageVariable = results.get(0);
					if(results.size() > 1) {
						Logger.info(this, "Found multiple language variables with key " + languageVariable.getKey() + " and host SYSTEM_HOST in language " + languageVariable.getLanguageId() + ". Returning random one");
					}
				} else {
					languageVariable = resultsWithoutSystemHost.get(0);
					if(resultsWithoutSystemHost.size() > 1) {
						Logger.info(this, "Found multiple language variables with key " + languageVariable.getKey() + " and host " + languageVariable.getHostIdentifier() + " in language " + languageVariable.getLanguageId() + ". Returning random one");
					}
				}
			} else {
				languageVariable = results.get(0);
			}
			
			return languageVariable.getValue();
		}
		
		return null;
	}

	private List<LanguageVariable> removeSystemHost(List<LanguageVariable> languageVariables) {
		List<LanguageVariable> languageVariablesWithoutSystemHost = new ArrayList<LanguageVariable>();
		for(LanguageVariable languageVariable: languageVariables) {
			if(!languageVariable.getHostIdentifier().equals(Host.SYSTEM_HOST)) {
				languageVariablesWithoutSystemHost.add(languageVariable);
			}
		}
		
		return languageVariablesWithoutSystemHost;
	}

	@Override
	public Map<String, String> getInitialCache() {
		
		// We use lazy loading of the keys, so we will return an empty initial cache
		return new HashMap<String, String>();
	}
	
}
