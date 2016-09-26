package nl.isaac.dotcms.languagevariables.util;

import java.util.List;
import java.util.Map;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheCleaner;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.languagesmanager.business.LanguageAPI;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

public class ContentletPostHook extends ContentletAPIPostHookAbstractImp{

	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats, List<Permission> selectedPermissions, User user,	boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(currentContentlet);
	}
	
	public void checkin(Contentlet contentlet, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet contentlet, List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet contentlet ,User user,boolean respectFrontendRoles,List<Category> cats,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet contentlet, User user,boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void checkinWithoutVersioning(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats ,List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet);
	}
	
	public void publish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet);
	}
	
	public void publish(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		for(Contentlet contentlet : contentlets) {
			handleContentlet(contentlet);			
		}
	}

	public void unpublish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet);
	}
	
	public void unpublish(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		for(Contentlet contentlet : contentlets) {
			handleContentlet(contentlet);			
		}
	}
	
	public void restoreVersion(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet);
	}
	
	private void handleContentlet(Contentlet newContentlet) {

		LanguageAPI languageAPI = APILocator.getLanguageAPI();
		Structure structure = newContentlet.getStructure();
		User systemUser;
		try {
			systemUser = APILocator.getUserAPI().getSystemUser();
		} catch (DotDataException e2) {
			throw new RuntimeException(e2.toString(), e2);
		}
		
		if(structure.getVelocityVarName().equals(Configuration.getStructureVelocityVarName())) {
			ContentletAPI contentletAPI = APILocator.getContentletAPI();
			String propertyKey = newContentlet.getStringProperty("key");
			String hostIdentifier = newContentlet.getHost();
			String languageId = String.valueOf(newContentlet.getLanguageId());
			
			Logger.debug(this, "Language key '" + propertyKey +  "' changed. Flushing cache...");
			LanguageVariablesCacheCleaner.flush(propertyKey, hostIdentifier, languageId);
		}		
	}

	/* Extra functions we need to override because dotCMS forgot to 
	 * put them in the ContentletAPIPostHookAbstractImp.. */
	
	public void isInodeIndexed(String arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}
}
