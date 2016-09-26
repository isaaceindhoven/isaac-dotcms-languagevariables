package nl.isaac.dotcms.languagevariables.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.isaac.dotcms.languagevariables.languageservice.ContentGlossaryAPI;
import nl.isaac.dotcms.languagevariables.util.Configuration;

import com.dotmarketing.portlets.contentlet.model.Contentlet;

public class LanguageVariablesCacheItemHandler implements ItemHandler<String> {

	@Override
	public String get(String key) {
		
		LanguageVariableCacheKey keyObject = LanguageVariableCacheKey.createInstanceWithKey(key);
		
		String propertyKey = keyObject.getPropertyKey();
		String languageId = keyObject.getLanguageId();
		String hostIdentifier = keyObject.getHostId();
		boolean live = keyObject.getLive();
		
		ContentGlossaryAPI contentGlossaryAPI = new ContentGlossaryAPI(languageId, hostIdentifier, live);
		List<Contentlet> results = contentGlossaryAPI.getContentletsWithKey(propertyKey);
		
		String value = null;
		
		if (results != null && results.size() > 0) {
			Contentlet languageVariable = results.get(0);
			value = languageVariable.getStringProperty(Configuration.getStructureValueField());
		} 
		
		return value;
	}

	@Override
	public Map<String, String> getInitialCache() {
		
		// We use lazy loading of the keys, so we will return an empty initial cache
		return new HashMap<String, String>();
	}
	
}
