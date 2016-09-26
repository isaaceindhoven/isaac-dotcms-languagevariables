package nl.isaac.dotcms.languagevariables.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheCleaner;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

public class ContentletPostHook extends ContentletAPIPostHookAbstractImp{

	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats, List<Permission> selectedPermissions, User user,	boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(currentContentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet, List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet ,User user,boolean respectFrontendRoles,List<Category> cats,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet, User user,boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkinWithoutVersioning(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void publish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void publish(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		for(Contentlet contentlet : contentlets) {
			handleContentlet(contentlet, user, respectFrontendRoles);			
		}
	}

	public void unpublish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void unpublish(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		for(Contentlet contentlet : contentlets) {
			handleContentlet(contentlet, user, respectFrontendRoles);			
		}
	}
	
	public void restoreVersion(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void archive(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	public void archive(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		for(Contentlet contentlet : contentlets) {
			handleContentlet(contentlet, user, respectFrontendRoles);			
		}
	}
	private void handleContentlet(Contentlet newContentlet, User user, boolean respectFrontendRoles) {
		if(LanguageVariable.isLanguageVariable(newContentlet)) {
			LanguageVariable languageVariable = new LanguageVariable(newContentlet);

			flushCacheForLanguageVariable(languageVariable);
			
			//Also flush the previous key when the key changed. Unfortunately we can't detect a change of the host
			LanguageVariable previousVersion = LanguageVariableFactory.getPreviousVersionInCurrentLanguage(languageVariable, user, respectFrontendRoles);
			if(previousVersion != null && !previousVersion.getKey().equals(languageVariable.getKey())) {
				flushCacheForLanguageVariable(previousVersion);
			}
		}		
	}

	private void flushCacheForLanguageVariable(LanguageVariable languageVariable) {
		String propertyKey = languageVariable.getKey();
		String languageId = String.valueOf(languageVariable.getLanguageId());
		
		Logger.info(this, "Language key '" + propertyKey +  "' changed for language " + languageId + ". Flushing cache...");

		//We flush here for all the hosts, since we can't detect whether the host field changed
		//The previous versions somehow always have the same host as the new one. Maybe this is a bug?
		for(Host host: getAllHosts()) {
			LanguageVariablesCacheCleaner.flush(propertyKey, host.getIdentifier(), languageId);
		}
	}
	
	private Collection<Host> getAllHosts() {
		try {
			return APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(), false);
		} catch (DotDataException e) {
			Logger.warn(this, "Exception while trying to retrieve all hosts", e);
		} catch (DotSecurityException e) {
			Logger.warn(this, "Exception while trying to retrieve all hosts", e);
		}
		
		return new ArrayList<Host>();
	}

	/* Extra functions we need to override because dotCMS forgot to 
	 * put them in the ContentletAPIPostHookAbstractImp.. */
	
	public void isInodeIndexed(String arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}
}
