package nl.isaac.dotcms.languagevariables.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPreHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

public class ContentletPreHook extends ContentletAPIPreHookAbstractImp {

	@Override
	public boolean checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats,
			List<Permission> selectedPermissions, User user, boolean respectFrontendRoles) {
		keyIsUnique(currentContentlet);
		return true;
	}

	@Override
	public boolean publish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		keyIsUnique(contentlet);
		return true;
	}

	private void keyIsUnique(Contentlet newContentlet) {
		boolean isNew = newContentlet.getIdentifier() == null || newContentlet.getIdentifier().trim().isEmpty();
		boolean keyExists = false;

		if (LanguageVariableContentlet.isLanguageVariable(newContentlet)) {

			Logger.info(this, "Checking if key already exists for Language Variable: " + (isNew ? "[NEW]" : newContentlet.getIdentifier()));

			LanguageVariableContentlet newLanguageVariable = new LanguageVariableContentlet(newContentlet);

			if (!StringUtils.isBlank(newLanguageVariable.getKey())) {
				ContentletQuery contentletQueryByKey = new ContentletQuery(Configuration.getStructureVelocityVarName());
				contentletQueryByKey.addFieldLimitation(true, Configuration.getStructureKeyField() + "_dotraw", newLanguageVariable.getKey());
				contentletQueryByKey.addHostAndIncludeSystemHost(newContentlet.getHost());

				List<Contentlet> contentletsByKey = contentletQueryByKey.executeSafe();

				if (isNew) {
					keyExists = contentletsByKey.size() != 0;
				} else {
					for (Contentlet contentlet : contentletsByKey) {
						if (!contentlet.getIdentifier().equals(newContentlet.getIdentifier())) {
							keyExists = true;
							break;
						}
					}
				}

				if (keyExists) {
					throw new RuntimeException("Key already used in another Language Variable contentlet");
				} else {
					Logger.info(this, "Language Variable Key is allowed");
				}
			}
		}
	}

}