package nl.isaac.dotcms.languagevariables.osgi;

import javax.servlet.ServletException;

import nl.isaac.dotcms.languagevariables.cache.servlet.FlushVariablesCache;
import nl.isaac.dotcms.languagevariables.languageservice.LanguagePrefixesServlet;
import nl.isaac.dotcms.languagevariables.util.ContentletPostHook;
import nl.isaac.dotcms.languagevariables.util.LanguageVariablesStructureFactory;
import nl.isaac.dotcms.languagevariables.viewtool.LanguageVariablesWebAPI;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

import com.dotcms.repackage.org.apache.felix.http.api.ExtHttpService;
import com.dotcms.repackage.org.osgi.framework.BundleContext;
import com.dotcms.repackage.org.osgi.framework.ServiceReference;
import com.dotcms.repackage.org.osgi.service.http.NamespaceException;
import com.dotcms.repackage.org.osgi.util.tracker.ServiceTracker;
import com.dotmarketing.filters.CMSFilter;
import com.dotmarketing.util.Logger;

public class LanguageVariablesActivator extends ExtendedGenericBundleActivator {

	private LanguagePrefixesServlet languagePrefixesServlet;
	private FlushVariablesCache languageFlushServlet;
	private ServiceTracker<ExtHttpService, ExtHttpService> tracker;

	@Override
	public void start(BundleContext context) throws Exception {
		// Default DotCMS call
		initializeServices(context);
		
		// Add the viewtools
		addViewTool(context,  LanguageVariablesWebAPI.class, "languageVariables", ViewToolScope.REQUEST);

		// Register the portlets
		registerPortlets(context, new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml"});

		// Register the servlet
		registerServlets(context);

		// Register language variables (portlet name)
		addLanguageVariables(context);

		// Register hook
		addPostHook(new ContentletPostHook());

		// Create the Language Variables structure, if it doesn't exist already
		LanguageVariablesStructureFactory.createStructureIfUnavailable();
	}


	/**
	 * Mostly boilerplate to consistently get the httpservice where we can register the {@link #searcherServlet}, we also create the servlet here.
	 * @param context
	 */
	private void registerServlets(BundleContext context) {
		tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				languagePrefixesServlet = new LanguagePrefixesServlet();
				languageFlushServlet = new FlushVariablesCache();

				try {

					extHttpService.registerServlet("/servlets/glossary/prefixes", languagePrefixesServlet, null, null);
					extHttpService.registerServlet("/servlets/languagevariables/portlet/flush", languageFlushServlet, null, null);

					CMSFilter.addExclude("/app/servlets/glossary/prefixes");
					CMSFilter.addExclude("/app/servlets/languagevariables/portlet/flush");

				} catch (ServletException e) {
					throw new RuntimeException("Failed to register servlets", e);
				} catch (NamespaceException e) {
					throw new RuntimeException("Failed to register servlets", e);
				}

				Logger.info(this, "Registered prefixes and languageFlush servlet");

				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				extHttpService.unregisterServlet(languagePrefixesServlet);
				extHttpService.unregisterServlet(languageFlushServlet);
				super.removedService(reference, extHttpService);
			}
		};
		tracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		unregisterServices(context);
	}

}
