package nl.isaac.dotcms.languagevariables.osgi;

import org.osgi.framework.BundleContext;

import nl.isaac.dotcms.languagevariables.languageservice.LanguagePrefixesServlet;
import nl.isaac.dotcms.languagevariables.servlet.FlushVariablesCache;
import nl.isaac.dotcms.languagevariables.servlet.UnarchiveVariable;
import nl.isaac.dotcms.languagevariables.util.ContentletPostHook;
import nl.isaac.dotcms.languagevariables.util.ContentletPreHook;
import nl.isaac.dotcms.languagevariables.util.LanguageVariablesStructureFactory;
import nl.isaac.dotcms.languagevariables.viewtool.LanguageVariablesWebAPI;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

public class LanguageVariablesActivator extends ExtendedGenericBundleActivator {

	@Override
	public void init(BundleContext context) {
		try {

			// Add the viewtools
			addViewTool(context, LanguageVariablesWebAPI.class, "languageVariables", ViewToolScope.REQUEST);

			// Register the portlets
			registerPortlets(context, new String[]{"conf/portlet.xml", "conf/liferay-portlet.xml"});

			// Register the servlet
			addServlet(context, LanguagePrefixesServlet.class, "/servlets/glossary/prefixes");
			addServlet(context, FlushVariablesCache.class, "/servlets/languagevariables/portlet/flush");
			addServlet(context, UnarchiveVariable.class, "/servlets/languagevariables/portlet/unarchive");

			// Register language variables (portlet name)
			addLanguageVariables(context);

			// Register PreHook
			addPreHook(new ContentletPreHook());

			// Register PostHook
			addPostHook(new ContentletPostHook());

			// Create the Language Variables structure, if it doesn't exist already
			LanguageVariablesStructureFactory.createStructureIfUnavailable();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
