package nl.isaac.dotcms.languagevariables.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

public class LanguageVariableFactory {
	
	public static LanguageVariableContentlet getPreviousVersionInCurrentLanguage(LanguageVariableContentlet languageVariable, User user, boolean respectFrontendRoles) {
		
		List<Contentlet> contentletVersions = getContentletVersions(languageVariable.getContentlet(), user, respectFrontendRoles);
		List<LanguageVariableContentlet> versions = getLanguageVariablesFromListInLanguage(contentletVersions, languageVariable.getLanguageId());
		
		if(UtilMethods.isSet(languageVariable.getInode())) {
			// When changing version
			for(LanguageVariableContentlet version: versions) {
				if(!version.getInode().equals(languageVariable.getInode())) {
					return version;
				}
			}
		} else {
			// When this is a new version without an inode
			if(versions.size() > 0) {
				return versions.get(0);
			}
		}
		
		return null;
	}
	
	private static List<Contentlet> getContentletVersions(Contentlet newContentlet, User user, boolean respectFrontendRoles) {
		if(UtilMethods.isSet(newContentlet.getIdentifier())) {
			try {
					Identifier identifier = APILocator.getIdentifierAPI().find(newContentlet.getIdentifier());
					List<Contentlet> contentletVersions = APILocator.getContentletAPI().findAllVersions(identifier, user, respectFrontendRoles);
					Collections.sort(contentletVersions, new Comparator<Contentlet>() {
						public int compare(Contentlet o1, Contentlet o2) {
							return -1 * o1.getModDate().compareTo(o2.getModDate());
						}
					});
					return contentletVersions;
			} catch (Exception e) {
				Logger.warn(LanguageVariableFactory.class, "Can't retrieve versions of contentlet " + newContentlet.getMap().get("key") + ", language " + newContentlet.getLanguageId(), e);
			}
		}
		
		return new ArrayList<Contentlet>();
	}

	public static List<LanguageVariableContentlet> getLanguageVariablesFromList(List<Contentlet> results) {
		List<LanguageVariableContentlet> languageVariables = new ArrayList<LanguageVariableContentlet>();
		for(Contentlet result: results) {
			languageVariables.add(new LanguageVariableContentlet(result));
		}
		
		return languageVariables;
	}

	public static List<LanguageVariableContentlet> getLanguageVariablesFromListInLanguage(List<Contentlet> results, long languageId) {
		List<LanguageVariableContentlet> languageVariables = new ArrayList<LanguageVariableContentlet>();
		for(Contentlet result: results) {
			if(result.getLanguageId() == languageId) {
				languageVariables.add(new LanguageVariableContentlet(result));
			}
		}
		
		return languageVariables;
	}
	
}
