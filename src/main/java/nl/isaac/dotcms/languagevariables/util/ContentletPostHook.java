package nl.isaac.dotcms.languagevariables.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.business.DotContentletStateException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

import nl.isaac.dotcms.languagevariables.cache.LanguageVariablesCacheCleaner;

public class ContentletPostHook extends ContentletAPIPostHookAbstractImp {

	public void checkin(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats ,List<Permission> permissions, User user, boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats, List<Permission> selectedPermissions, User user,	boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(currentContentlet, user, respectFrontendRoles);
		renameAllKeys(currentContentlet, user, respectFrontendRoles);
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
	
	public void checkinWithoutVersioning(Contentlet contentlet, Map<Relationship, List<Contentlet>> contentRelationships, List<Category> cats, List<Permission> permissions, User user,boolean respectFrontendRoles,Contentlet returnValue) {
		handleContentlet(contentlet, user, respectFrontendRoles);
	}
	
	public void publish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		handleContentlet(contentlet, user, respectFrontendRoles);
		renameAllKeys(contentlet, user, respectFrontendRoles);
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
		if(LanguageVariableContentlet.isLanguageVariable(newContentlet)) {
			LanguageVariableContentlet languageVariable = new LanguageVariableContentlet(newContentlet);

			flushCacheForLanguageVariable(languageVariable);
			
			// Also flush the previous key when the key changed. Unfortunately we can't detect a change of the host
			LanguageVariableContentlet previousVersion = LanguageVariableFactory.getPreviousVersionInCurrentLanguage(languageVariable, user, respectFrontendRoles);
			if(previousVersion != null && !previousVersion.getKey().equals(languageVariable.getKey())) {
				flushCacheForLanguageVariable(previousVersion);
			}
		}		
	}
	
	private void renameAllKeys(Contentlet newContentlet, User user, boolean respectFrontendRoles) {
		Logger.info(this, "RenameAllKeys called");
		
		if(!LanguageVariableContentlet.isLanguageVariable(newContentlet)) {
			Logger.info(this, "Skipped RenameAllKeys - contentlet (id: " + newContentlet.getIdentifier() + ") is not a Language Variable");
			return;
		}
		
		// A new LanguageVariableKey content is added, no need to rename keys since
		// this has been checked in the PreHook
		if (StringUtils.isBlank(newContentlet.getIdentifier())) {
			Logger.info(this, "Skipped RenameAllKeys - a new Language Variable contentlet is added");
			return;
		}
		
		// Save clicked, without publish, causes the Inode to be blank
		if (StringUtils.isBlank(newContentlet.getInode())) {
			Contentlet savedContentlet = null;
			try {
				savedContentlet = APILocator.getContentletAPI().findContentletByIdentifier(newContentlet.getIdentifier(), false, newContentlet.getLanguageId(), user, respectFrontendRoles);
			} catch (DotContentletStateException | DotDataException | DotSecurityException e) {
				e.printStackTrace();
			}
			
			if (savedContentlet != null) {
				newContentlet = savedContentlet;
			}
		}
		
		LanguageVariableContentlet languageVariable = new LanguageVariableContentlet(newContentlet);
		
		LanguageVariableContentlet previousVersion = LanguageVariableFactory.getPreviousVersionInCurrentLanguage(languageVariable, user, respectFrontendRoles);
		
		if (previousVersion == null) {
			Logger.info(this, "Skipped RenameAllKeys - previous version of the Language Variable not found");
			return;
		} else if (previousVersion.getKey().equals(languageVariable.getKey())) {
			Logger.info(this, "Skipped RenameAllKeys - key of previous version of the Language Variable is empty");
			return;
		} else {
			Logger.info(this, "Executing RenameAllKeys - previous version of Language Variable's key is not equal (or not found): " + previousVersion.getKey());
		}
		
		// 'Save and publish' is clicked, we have access to the old key value to retrieve
		// the language variable contentlets which have to be renamed
		ContentletQuery contentletQueryByKey = new ContentletQuery(Configuration.getStructureVelocityVarName());
		contentletQueryByKey.addFieldLimitation(true, Configuration.getStructureKeyField(), previousVersion.getKey());
		contentletQueryByKey.addHostAndIncludeSystemHost(newContentlet.getHost());
		
		List<Contentlet> results = contentletQueryByKey.executeSafe();
		
		// Archived contentlets cannot be renamed
		List<Contentlet> archivedContentlets = results.stream().filter(c -> {
			try {
				return c.isArchived();
			} catch (DotStateException | DotDataException | DotSecurityException e) {
				throw new RuntimeException("Error occured while checking Language Variable contentlets state", e);
			}
		}).collect(Collectors.toList());
		
		if (archivedContentlets != null && !archivedContentlets.isEmpty()) {
			throw new RuntimeException("Please unarchive the archived Language Variable with key '" + previousVersion.getKey() + "' first");
		}
		
		ContentletAPI conAPI = APILocator.getContentletAPI();
		
		for (Contentlet contentlet : results) {
			// Skip current contentlet, this one has already been renamed before this method got called
			if (languageVariable.getLanguageId() == contentlet.getLanguageId() || contentlet.getInode().equals(newContentlet.getInode())) {
				continue;
			}
			
			Logger.info(this, "Renaming key for contentlet " + contentlet.getInode());
			
			try {
				
				// Checkout the Language Variable to edit
				Contentlet renamedContentlet = conAPI.checkout(contentlet.getInode(), user, respectFrontendRoles);
				
				// Change the existing Language Variable's key to the new key
				renamedContentlet.setStringProperty(Configuration.getStructureKeyField(), languageVariable.getKey());
				
				// Check in the updated Language Variable
				renamedContentlet = conAPI.checkin(renamedContentlet,  user, respectFrontendRoles);
            	
			} catch (DotStateException | DotSecurityException | DotDataException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void flushCacheForLanguageVariable(LanguageVariableContentlet languageVariable) {
		String propertyKey = languageVariable.getKey();
		String languageId = String.valueOf(languageVariable.getLanguageId());
		
		Logger.info(this, "Language key '" + propertyKey +  "' changed for language " + languageId + ". Flushing cache...");

		// We flush here for all the hosts, since we can't detect whether the host field changed
		// The previous versions somehow always have the same host as the new one.
		for(Host host: getAllHosts()) {
			LanguageVariablesCacheCleaner.flush(propertyKey, host.getIdentifier(), languageId, languageVariable.getContentlet().getIdentifier());
		}
	}
	
	private Collection<Host> getAllHosts() {
		try {
			return APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(), false);
		} catch (DotDataException | DotSecurityException e) {
			Logger.warn(this, "Exception while trying to retrieve all hosts", e);
		}
		
		return new ArrayList<Host>();
	}

	/* Extra functions we need to override. This is required.*/
	public void isInodeIndexed(String arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
	}
}
