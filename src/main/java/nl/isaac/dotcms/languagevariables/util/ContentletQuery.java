package nl.isaac.dotcms.languagevariables.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.PaginatedArrayList;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.viewtools.content.util.ContentUtils;

/**
 * Provides functionality to build a Lucene query
 * 
 * @author xander
 * @author jorith.vandenheuvel
 *
 */
public class ContentletQuery {
	
	protected StringBuilder query = new StringBuilder();
	private String structureName;
	private final Map<String, String> exactFieldLimitations = new HashMap<String, String>();
	
	// Paging & sorting
	private boolean usePaging = false;
	private int offset = 0;
	private int limit = -1;
	private String sortBy = "";
	private long totalResults = -1;
	
	public ContentletQuery(String structureName) {
		this(true, structureName);
	}
	
	public ContentletQuery(boolean include, String structureName) {
		this.structureName = structureName;
		query.append((include ? "+" : "-") + "structureName:" + structureName);
	}
	
	protected String getStructureName() {
		return structureName;
	}
	
	/**
	 * 
	 * @param include True if this must be included in the result, false otherwise
	 * @param name The field name
	 * @param value The value to search for
	 */
	public void addFieldLimitation(boolean include, String name, String value) {
		query.append(" " + (include ? "+" : "-"));
		addFieldLimitationString(name, value);
	}
	
	public void addExactFieldLimitation(boolean include, String name, String value) {
		exactFieldLimitations.put(name,  value);
		addFieldLimitation(include, name, value);
	}
	
	private void addFieldLimitationString(String name, String value) {
		if(value.contains("*")) {
			query.append(structureName + "." + name + ":" + escapeValue(value) + " ");
		} else {
			query.append(structureName + "." + name + ":\"" + escapeValue(value) + "\" ");
		}
	}
	
	/**
	 * 
	 * @param include True if this must be included in the result, false otherwise
	 * @param name The field name
	 * @param values The values to search for
	 */
	public void addFieldLimitations(boolean include, String name, String... values) {
		query.append(" " + (include ? "+" : "-") + "(");
		for(String value: values) {
			addFieldLimitationString(name, value);
		}
		query.append(")");
	}
	
	public void addIdentifierLimitations(boolean include, String... identifiers) {
		query.append(" " + (include ? "+" : "-") + "(");
		for(String identifier : identifiers) {
			query.append("identifier:" + escapeValue(identifier) + " ");
		}
		query.append(")");
	}

	/**
	 * 
	 * @param name The field name
	 * @param from The lower bound value
	 * @param to The upper bound value
	 */
	public void addFieldRangeLimitation(String name, String from, String to) {
		query.append(" +" + structureName + "." + name + ":[" + from + " TO " + to + "] ");
	}
	
	private String escapeValue(String value) {
		return value.replace(":", "\\:").replace("\"", "\\\"");
	}
	
	public void addLatestLiveAndNotDeleted(boolean live) {
		if (live) {
			addLive(true);			
		} else {
			addWorking(true);			
		}
		addDeleted(false);
	}

	
	/**
	 * Limit the results of the query to certain categories
	 * @param include True if this must be included in the result, false otherwise
	 * @param categoryVelocityVarNames The Velocity varnames of the categories to filter on
	 */
	public void addCategoryLimitations(boolean include, String... categoryVelocityVarNames) {
		query.append(" " + (include ? "+" : "-") + "(");
		for(String categoryVelocityVarName: categoryVelocityVarNames) {
			query.append("categories:" + escapeValue(categoryVelocityVarName) + " ");
		}
		query.append(")");
	}
	
	/**
	 * Limit the results of the query to certain categories
	 * @param include True if this must be included in the result, false otherwise
	 * @param categories The categories to filter on
	 */
	public void addCategoryLimitations(boolean include, Category... categories) {
		String[] categoryVelocityVarNames = new String[categories.length];
		for(int i=0; i<categories.length; i++) {
			categoryVelocityVarNames[i] = categories[i].getCategoryVelocityVarName();
		}
		
		addCategoryLimitations(include, categoryVelocityVarNames);
	}

	/**
	 * Adds +live to the query 
	 * @param live
	 */
	public void addLive(boolean live) {
		query.append(" +live:" + live);
	}

	/**
	 * Adds +working to the query 
	 * @param working
	 */
	public void addWorking(boolean working) {
		query.append(" +working:" + working);
	}
	
	/**
	 * Adds +deleted to the query 
	 * @param deleted
	 */
	public void addDeleted(boolean deleted) {
		query.append(" +deleted:" + deleted);
	}
	
	/**
	 * Adds a language limit to the query
	 * @param language
	 */
	public void addLanguage(Language language) {
		addLanguage(language.getId());
	}
	
	/**
	 * Adds a language limit to the query
	 * @param languageId
	 */
	public void addLanguage(Long languageId) {
		if(languageId != null) {
			addLanguage(languageId.toString());
		} else {
			Logger.warn(this, "Tried to add languageId Null!");
		}
	}

	/**
	 * Adds a language limit to the query
	 * @param languageId
	 */
	public void addLanguage(Integer languageId) {
		if(languageId != null) {
			addLanguage(languageId.toString());
		} else {
			Logger.warn(this, "Tried to add languageId Null!");
		}
	}
	
	/**
	 * Adds a language limit to the query
	 * @param languageId
	 */
	public void addLanguage(String languageId) {
		query.append(" +languageId:" + languageId);
	}
	
	/**
	 * Adds paging to the query
	 * @param pageSize The number of contentlets per page
	 * @param pageIndex The page number to get results for
	 */
	public void addPaging(Integer pageSize, Integer pageIndex) {
		ParamValidationUtil.validateParamNotNull(pageSize, "pageSize");
		ParamValidationUtil.validateParamNotNull(pageIndex, "pageIndex");
		
		int offset = pageIndex * pageSize;
		
		this.offset = offset;
		this.limit = pageSize;
		this.usePaging = true;
	}

	/**
	 * Adds sorting to the query
	 * @param fieldName the field to sort on. Must be in the format [structure name].[field name]
	 */
	public void addSorting(String structureName, String fieldName, boolean asc) {
		ParamValidationUtil.validateParamNotNull(fieldName, "sortBy");
		
		String sorting = "modDate".equals(fieldName) ? fieldName : structureName + "." + fieldName;
		String sortingWithOrder = sorting + " " + (asc ? "asc" : "desc");
		
		this.sortBy = UtilMethods.isSet(this.sortBy)? this.sortBy + ", " + sortingWithOrder : sortingWithOrder;			
	}
	
	/**
	 * 
	 * @return The total number of results, only when paging is set and the query has been executed. -1 otherwise.
	 */
	public long getTotalResults() {
		return this.totalResults;
	}
	
	/**
	 * Adds a host limit to the query
	 * @param host
	 * @return The updated ContentletQuery
	 */
	public ContentletQuery addHost(Host host) {
		return addHost(host.getIdentifier());
	}
	
	/**
	 * Adds a host limit to the query
	 * @param hostIdentifier
	 * @return The updated ContentletQuery
	 */
	public ContentletQuery addHost(String hostIdentifier) {
		query.append(" +conhost:" + hostIdentifier);
		return this;
	}
	
	/**
	 * Adds a host limit to the query (given host AND System HOST
	 * @param hostIdentifier
	 */
	public void addHostAndIncludeSystemHost(String hostIdentifier) {
		query.append(" +(conhost:SYSTEM_HOST conhost:" + hostIdentifier + ")");
	}
	
	/**
	 *
	 * @return The resulting query
	 */
	public String getQuery() {
		return query.toString();
	}
	
	/**
	 * 
	 * @return The resulting query as a StringBuilder object
	 */
	public StringBuilder getQueryStringBuilder() {
		return query;
	}
	
	public Map<String, String> getExactFieldLimitations() {
		return Collections.unmodifiableMap(exactFieldLimitations);
	}
	
	/**
	 * Executes the query
	 * @return The resulting List of Contentlets
	 */
	public List<Contentlet> executeSafe() {
		
		Logger.debug(this, "Executing query: " + query.toString());
		Logger.debug(this, "Use Paging: " + this.usePaging + ", Limit: " + this.limit + ", Offset: " + this.offset + ", Sort By: " + this.sortBy);
		
		try {
			if(this.usePaging) {
				if(!exactFieldLimitations.isEmpty()) {
					Logger.warn(this, "Can't use exact matching in paginated search");
				}

				PaginatedArrayList<Contentlet> contentlets = ContentUtils.pullPagenated(query.toString(), this.limit, this.offset, this.sortBy, APILocator.getUserAPI().getSystemUser(), null);
				this.totalResults = contentlets.getTotalResults();
				
				Logger.debug(this, "Number Of Results: " + contentlets.size() + ", Total Results: " + contentlets.getTotalResults());
				
				return contentlets;
			} else {
				List<Contentlet> contentlets = APILocator.getContentletAPI().search(query.toString(), this.limit, this.offset, this.sortBy, APILocator.getUserAPI().getSystemUser(), false);
				contentlets = removeNonExactMatches(contentlets);
				
				if (Logger.isDebugEnabled(this.getClass())) {
					if (contentlets == null) {
						Logger.debug(this, "Contentlets == null");
					} else {
						Logger.debug(this, "Number Of Results: " + contentlets.size());
					}
				}
				
				return contentlets;
			}
			
		} catch (DotDataException | DotSecurityException e) {
			Logger.warn(this, "Exception while executing query", e);
		}
		
		return new ArrayList<Contentlet>();
	}

	/**
	 * Combines two ContentletQuery objects into one query.
	 * @param and True if the resulting query must be an AND query. False if it must
	 * be an OR query.
	 * @param contentletQuery The query to add
	 */
	public void addQuery(boolean and, ContentletQuery contentletQuery) {
		if(and) {
			//just add them together
			query.append(" " + contentletQuery.getQuery());
		} else {
			//group them and make it an OR query
			query.insert(0, "(");
			query.append(") (");
			query.append(contentletQuery.getQuery());
			query.append(")");
		}
	}
	
	/**
	 * Executes the query and returns the first result. Can be used when you
	 * are sure that the query returns exactly one result, for instance when
	 * the query contains an identifier limitation or when the limit is set
	 * to 1.
	 *
	 * @return The resulting Contentlet, null if the query has no result
	 */
	public Contentlet executeSafeFirst() {
		List<Contentlet> result = executeSafe();

		if (result.size() > 0) {
			return result.get(0);
		}

		return null;
	}
	
	public Contentlet executeSafeSingle() {
		List<Contentlet> result = executeSafe();

		if (result.size() == 1) {
			return result.get(0);
		} else {
			Logger.warn(this, "Expected 1 result but found " + result.size() + ". Returning null");
		}

		return null;
	}

	/**
	 * Remove contentlets that do not exactly match the key-values in exactFieldLimitations
	 * These are added by executing the addExactFieldLimitation method.
	 * When there are no exact field limitations then the entered contentlet list is returned
	 */
	private List<Contentlet> removeNonExactMatches(List<Contentlet> contentlets) {
		if(exactFieldLimitations.isEmpty()) {
			return contentlets;
		} else {
			List<Contentlet> correctMatches = new ArrayList<Contentlet>();
			for(Contentlet contentlet: contentlets) {
				for(Entry<String, String> fieldLimitation: exactFieldLimitations.entrySet()) {
					// do not use contentlet.getStringProperty, since that only handles long and string values (not booleans)
					Object fieldValue = contentlet.get(fieldLimitation.getKey());
					if(fieldValue != null && fieldValue.toString().equalsIgnoreCase(fieldLimitation.getValue())) {
						correctMatches.add(contentlet);
					}
				}
			}

			return correctMatches;
		}
	}
	
}
