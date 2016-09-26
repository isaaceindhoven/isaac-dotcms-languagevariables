package nl.isaac.dotcms.languagevariables.osgi;

import com.dotcms.repackage.org.osgi.framework.BundleContext;

import nl.isaac.dotcms.languagevariables.cache.servlet.FlushVariablesCache;
import nl.isaac.dotcms.languagevariables.languageservice.LanguagePrefixesServlet;
import nl.isaac.dotcms.languagevariables.util.ContentletPostHook;
import nl.isaac.dotcms.languagevariables.util.LanguageVariablesStructureFactory;
import nl.isaac.dotcms.languagevariables.viewtool.LanguageVariablesWebAPI;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

public class LanguageVariablesActivator extends ExtendedGenericBundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// Default DotCMS call
		initializeServices(context);
		
		// Add the viewtools
		addViewTool(context,  LanguageVariablesWebAPI.class, "languageVariables", ViewToolScope.REQUEST);

		// Register the portlets
		registerPortlets(context, new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml"});

		// Register the servlet
		addServlet(context, LanguagePrefixesServlet.class, "/servlets/glossary/prefixes");
		addServlet(context, FlushVariablesCache.class, "/servlets/languagevariables/portlet/flush");
		
		// Register language variables (portlet name)
		addLanguageVariables(context);

		// Register hook
		addPostHook(new ContentletPostHook());

		// Create the Language Variables structure, if it doesn't exist already
		LanguageVariablesStructureFactory.createStructureIfUnavailable();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		unregisterServices(context);
	}

}
