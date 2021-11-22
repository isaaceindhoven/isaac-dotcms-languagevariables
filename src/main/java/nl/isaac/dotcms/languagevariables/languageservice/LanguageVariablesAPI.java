package nl.isaac.dotcms.languagevariables.languageservice;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariableCacheKey;
import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.languageservice.IncompleteLanguageVariable.IncompleteStatus;
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
				results.addAll(getLanguageVariablesContentletsWithKeyPrefix(prefix + "*"));
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

	public List<LanguageVariableContentlet> getLanguageVariablesContentletsWithKeyPrefix(String key) {
		// retrieve all the contentlets with the prefix
		Logger.info(this.getClass().getName(), "get prefix " + key);
		ContentletQuery contentletQuery = getBaseLanguageVariablesContentletQuery();
		contentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField(), key);

		List<Contentlet> results = contentletQuery.executeSafe();
		return LanguageVariableFactory.getLanguageVariablesFromList(results);
	}


	public List<LanguageVariableContentlet> getLanguageVariablesContentletsWithExactKey(String key) {
		ContentletQuery contentletQuery = getBaseLanguageVariablesContentletQuery();
		contentletQuery.addExactFieldLimitation(true, Configuration.getStructureKeyField(), key);

		List<Contentlet> results = contentletQuery.executeSafe();
		return LanguageVariableFactory.getLanguageVariablesFromList(results);
	}

	private ContentletQuery getBaseLanguageVariablesContentletQuery() {
		ContentletQuery contentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		contentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		contentletQuery.addLanguage(languageId);

		if (live) {
			contentletQuery.addLive(true);
		} else {
			contentletQuery.addWorking(true);
		}

		contentletQuery.addDeleted(false);

		return contentletQuery;
	}

	public IncompleteLanguageVariable getIncompleteLanguageVariable(LanguageVariableCacheKey cacheItem, String referer) {
		LanguageVariableContentlet archived = getArchivedLanguageVariableContentlet(cacheItem);
		LanguageVariableContentlet unpublished = getUnpublishedLanguageVariableContentlet(cacheItem);
		LanguageVariableContentlet missing = getExistingLanguageVariableContentletInAnotherLanguage(cacheItem);
		LanguageVariableContentlet exists = getLanguageVariableContentlet(cacheItem);
		
		// Hide LanguageVariable when the key is still in cache, but not incomplete anymore (not archived, published, exists in all languages, exists)
		// For example after updating the status of a LanguageVariable it still exists in cache as incomplete but isn't, so it shouldn't be shown in the list
		if (exists != null && archived == null && unpublished == null && missing == null) {
			return null;
		}
		
		// Archived
		try {
			if (archived != null) {					
				if (archived.getContentlet().isArchived()) {
					return new IncompleteLanguageVariable(archived, IncompleteStatus.ARCHIVED, cacheItem.getPropertyKey(), cacheItem.getLanguageId(), referer, hostIdentifier);
				} else {
					return new IncompleteLanguageVariable(archived, IncompleteStatus.UNPUBLISHED, cacheItem.getPropertyKey(), cacheItem.getLanguageId(), referer, hostIdentifier);
				}
			}
		} catch (DotStateException | DotDataException | DotSecurityException e) {
			Logger.warn(this.getClass().getName(), "Error while checking if Language Variable is archived", e);
		}
		
		// Unpublished
		if (unpublished != null) {
			return new IncompleteLanguageVariable(unpublished, IncompleteStatus.UNPUBLISHED, cacheItem.getPropertyKey(), cacheItem.getLanguageId(), referer, hostIdentifier);
		}
		
		// Exists - exists, but not in all languages yet
		if (missing != null) {
			return new IncompleteLanguageVariable(missing, IncompleteStatus.MISSING, cacheItem.getPropertyKey(), cacheItem.getLanguageId(), referer, hostIdentifier);
		}
		
		// New - key doesn't exist yet
		if (exists == null) {
			return new IncompleteLanguageVariable(null, IncompleteStatus.NOT_FOUND, cacheItem.getPropertyKey(), cacheItem.getLanguageId(), referer, hostIdentifier);
		}
		
		return null;
	}
	
	private LanguageVariableContentlet getUnpublishedLanguageVariableContentlet(LanguageVariableCacheKey cacheItem) {
		ContentletQuery unpublishedContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		unpublishedContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		if (cacheItem.getContentletIdentifier() != null) {
			unpublishedContentletQuery.addIdentifierLimitations(true, cacheItem.getContentletIdentifier());
		} else {
			unpublishedContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField() + "_dotraw", cacheItem.getPropertyKey());
		}
		unpublishedContentletQuery.addLanguage(cacheItem.getLanguageId());
		unpublishedContentletQuery.addLive(false);
		unpublishedContentletQuery.addWorking(true);
		unpublishedContentletQuery.addDeleted(false);
		
		Contentlet unpublishedContentlet = unpublishedContentletQuery.executeSafeSingle();
		
		if (unpublishedContentlet != null) {
			return new LanguageVariableContentlet(unpublishedContentlet);
		}

		return null;
	}
	
	private LanguageVariableContentlet getArchivedLanguageVariableContentlet(LanguageVariableCacheKey cacheItem) {
		ContentletQuery archivedContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		archivedContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		
		if (cacheItem.getContentletIdentifier() != null) {
			archivedContentletQuery.addIdentifierLimitations(true, cacheItem.getContentletIdentifier());
		} else {
			archivedContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField() + "_dotraw", cacheItem.getPropertyKey());
		}
		
		archivedContentletQuery.addLanguage(cacheItem.getLanguageId());
		archivedContentletQuery.addWorking(true);
		archivedContentletQuery.addDeleted(true);
		
		Contentlet archivedContent = archivedContentletQuery.executeSafeSingle();
		
		if (archivedContent != null) {
			return new LanguageVariableContentlet(archivedContent);
		}

		return null;
	}

	private LanguageVariableContentlet getExistingLanguageVariableContentletInAnotherLanguage(LanguageVariableCacheKey cacheItem) {
		if (getLanguageVariableContentlet(cacheItem) == null) {
			List<Language> languages = APILocator.getLanguageAPI().getLanguages();
			
			for (Language language : languages) {
				if (language.getId() == Long.valueOf(cacheItem.getLanguageId())) {
					continue;
				}
				
				ContentletQuery existingContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
				existingContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
				if (cacheItem.getContentletIdentifier() != null) {
					existingContentletQuery.addIdentifierLimitations(true, cacheItem.getContentletIdentifier());
				} else {
					existingContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField() + "_dotraw", cacheItem.getPropertyKey());
				}
				existingContentletQuery.addLanguage(language.getId());
				
				
				Contentlet existingContentlet = existingContentletQuery.executeSafeSingle();
	
				if (existingContentlet != null) {
					return new LanguageVariableContentlet(existingContentlet);
				}
			}
		}

		return null;
	}
	
	private LanguageVariableContentlet getLanguageVariableContentlet(LanguageVariableCacheKey cacheItem) {
		ContentletQuery existingContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
		existingContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
		if (cacheItem.getContentletIdentifier() != null) {
			existingContentletQuery.addIdentifierLimitations(true, cacheItem.getContentletIdentifier());
		} else {
			existingContentletQuery.addFieldLimitation(true, Configuration.getStructureKeyField() + "_dotraw", cacheItem.getPropertyKey());
		}
		existingContentletQuery.addLanguage(cacheItem.getLanguageId());
		
		Contentlet existingContentlet = existingContentletQuery.executeSafeSingle();

		if (existingContentlet != null) {
			return new LanguageVariableContentlet(existingContentlet);
		}

		return null;
	}
}
