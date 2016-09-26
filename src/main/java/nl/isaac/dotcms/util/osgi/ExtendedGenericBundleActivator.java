package nl.isaac.dotcms.util.osgi;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.context.ViewContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.dotcms.repackage.org.apache.commons.lang.Validate;
import com.dotcms.repackage.org.apache.felix.http.api.ExtHttpService;
import com.dotcms.repackage.org.osgi.framework.BundleContext;
import com.dotcms.repackage.org.osgi.framework.ServiceReference;
import com.dotcms.repackage.org.osgi.util.tracker.ServiceTracker;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.config.RestServiceUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.filters.CMSFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHook;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPreHook;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.VelocityUtil;

/**
 * Provides a convenience method for adding viewtools.
 * @author Maarten
 *
 */
public abstract class ExtendedGenericBundleActivator extends GenericBundleActivator {
	private List<ServiceTracker<ExtHttpService, ExtHttpService>> trackers = new ArrayList<ServiceTracker<ExtHttpService, ExtHttpService>>();
	private boolean languageVariablesNotAdded = true;

	
	@Override
	protected void initializeServices(BundleContext context) throws Exception {
		super.initializeServices(context);
	}

	protected void addViewTool(BundleContext context, Class<?> viewtoolClass, String key, ViewToolScope scope) {
		OSGiSafeServletToolInfo viewtool = new OSGiSafeServletToolInfo();
		viewtool.setClassname(viewtoolClass);
		viewtool.setKey(key);
		switch (scope) {
		case APPLICATION:
			viewtool.setScope(ViewContext.APPLICATION);
			break;
		case REQUEST:
			viewtool.setScope(ViewContext.REQUEST);
			break;
		case RESPONSE:
			viewtool.setScope(ViewContext.RESPONSE);
			break;
		case SESSION:
			viewtool.setScope(ViewContext.SESSION);
			break;
		default:
			throw new RuntimeException("Unknown viewtoolscope: " + scope);
		}

		registerViewToolService(context, viewtool);
	}
	
	protected void addServlet(BundleContext context, final Class<? extends Servlet> clazz, final String path) {
		Validate.notNull(clazz, "Servlet class may not be null");
		Validate.notEmpty(path, "Servlet path may not be null");
		Validate.isTrue(path.startsWith("/"), "Servlet path must start with a /");

		final Servlet servlet;
		try {
			servlet = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		Logger.info(this, "Registering Servlet " + servlet.getClass().getSimpleName());
		
		addServlet(context, servlet, path, false);
	}
	
	/**
	 * @param handleBundleServices is used to add/remove bundleServices, which are needed for the DispatcherServlet
	 */
	private void addServlet(BundleContext context, final Servlet servlet, final String path, final boolean handleBundleServices) {
		ServiceTracker<ExtHttpService, ExtHttpService> tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				try {
					if(handleBundleServices) {
						publishBundleServices(context);
					}
					
					extHttpService.registerServlet(path, servlet, null, null);

				} catch (Exception e) {
					throw new RuntimeException("Failed to register servlet " + servlet.getClass().getSimpleName(), e);
				}

				CMSFilter.addExclude(path);
				CMSFilter.addExclude("/app" + path);

				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				extHttpService.unregisterServlet(servlet);
				CMSFilter.removeExclude(path);
				CMSFilter.removeExclude("/app" + path);

				if(handleBundleServices) {
					try {
						unpublishBundleServices();
					} catch (Exception e) {
						Logger.warn(this, "Unable to unpublish bundle services", e);
					}
				}
				
				super.removedService(reference, extHttpService);
			}
		};
		
		this.trackers.add(tracker);
		tracker.open();
		
	}
	
	protected void addFilter(BundleContext context, final Class <? extends Filter> clazz, final String regex) {
		Validate.notNull(clazz, "Filter class may not be null");
		Validate.notEmpty(regex, "Filter regex may not be null");
		Validate.isTrue(regex.startsWith("/"), "Filter regex must start with a /");
		
		final Filter filterToRegister;
		try {
			filterToRegister = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		Logger.info(this, "Registering Filter " + filterToRegister.getClass().getSimpleName());

		ServiceTracker<ExtHttpService, ExtHttpService> tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				try {

					extHttpService.registerFilter(filterToRegister, regex, null, trackers.size(), null);

				} catch (ServletException e) {
					throw new RuntimeException("Failed to register filter " + filterToRegister.getClass().getSimpleName(), e);
				}

				CMSFilter.addExclude(regex);
				CMSFilter.addExclude("/app" + regex);

				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				extHttpService.unregisterFilter(filterToRegister);
				CMSFilter.removeExclude(regex);
				CMSFilter.removeExclude("/app" + regex);
				super.removedService(reference, extHttpService);
			}
		};
		
		tracker.open();
		this.trackers.add(tracker);
		
	}
	
	protected void addMacros(BundleContext context) {
		Logger.info(this, "Registering macros");
		
		final VelocityEngine engine = VelocityUtil.getEngine();
		URL macrosExtUrl = context.getBundle().getResource("conf/macros-ext.vm");
		
		InputStream instream = null;
		try {
			instream = macrosExtUrl.openStream();
		    engine.evaluate(VelocityUtil.getBasicContext(), new StringWriter(), context.getBundle().getSymbolicName(), new InputStreamReader(instream, Charset.forName("UTF-8")));
		} catch (IOException e) {
			Logger.warn(this, "Exception while reading macros-ext.vm", e);
		} finally {
			try {
				if(instream != null) {
					instream.close();
				}
			} catch (IOException e) {
				Logger.warn(this, "Exception while closing stream to macros-ext.vm", e);
			}
		}		
	}
	
	private void addLanguageVariables(Map<String, String> languageVariables, Language language) {
		Map<String, String> emptyMap = new HashMap<String, String>();
		Set<String> emptySet = new HashSet<String>();
		try {

			Logger.info(this, "Registering " + languageVariables.keySet().size() + " language variable(s)");
			APILocator.getLanguageAPI().saveLanguageKeys(language, languageVariables, emptyMap, emptySet);

		} catch (DotDataException e) {
			Logger.warn(this, "Unable to register language variables", e);
		}
	}
	
	/**
	 * Registers the ENGLISH language variables that are saved in the conf/language-ext.properties file 
	 */
	protected void addLanguageVariables(BundleContext context) {
		if(languageVariablesNotAdded) {
			languageVariablesNotAdded = false;
			try {
	
				// Read all the language variables from the properties file
				URL resourceURL = context.getBundle().getResource("conf/Language-ext.properties");
				PropertyResourceBundle resourceBundle = new PropertyResourceBundle(resourceURL.openStream());
				
				// Put the properties in a map
				Map<String, String> languageVariables = new HashMap<String, String>();
				for(String key: resourceBundle.keySet()) {
					languageVariables.put(key, resourceBundle.getString(key));
				}
				
				// Register the variables in locale en_US
				addLanguageVariables(languageVariables, APILocator.getLanguageAPI().getLanguage("en", "US"));
				
			} catch (IOException e) {
				Logger.warn(this, "Exception while registering language variables", e);
			}
		}
	}
	
	protected void addPreHook(BundleContext context, Class <? extends ContentletAPIPreHook> clazz) {
		Logger.info(this, "Registering PreHook " + clazz.getSimpleName());
		try {
			addPreHook(clazz.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addPostHook(BundleContext context, Class <? extends ContentletAPIPostHook> clazz) {
		Logger.info(this, "Registering PostHook " + clazz.getSimpleName());
		try {
			addPostHook(clazz.newInstance());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addRestService(BundleContext context, final Class<? extends WebResource> clazz) {
		Logger.info(this, "Registering REST service " + clazz.getSimpleName());
		ServiceTracker<ExtHttpService, ExtHttpService> tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				RestServiceUtil.addResource(clazz);
				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				RestServiceUtil.removeResource(clazz);
				super.removedService(reference, extHttpService);
			}
		};
		
		tracker.open();
		this.trackers.add(tracker);
		
	}
	
	protected void addPortlets(BundleContext context) {
		if(languageVariablesNotAdded) {
			addLanguageVariables(context);
		}
		
		Logger.info(this, "Registering portlet(s)");
		
		ServiceTracker<ExtHttpService, ExtHttpService> tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				try {
					registerPortlets(context, new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml"});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				try {
					unregisterPortlets();
				} catch (Exception e) {
					Logger.warn(this, "Exception while unregistering portlet", e);
				}
				super.removedService(reference, extHttpService);
			}
		};
		
		tracker.open();
		this.trackers.add(tracker);
		
		//TODO: Flush Velocity cache
		
	}
	
	protected void addSpringController(BundleContext context, String path, String contextConfigLocation) {
		Logger.info(this, "Registering spring controller " + contextConfigLocation);
		
		try {
			publishBundleServices(context);
		} catch (Exception e) {
			Logger.warn(this, "Unable to publish bundle services", e);
		}
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setContextConfigLocation(contextConfigLocation);
		
		addServlet(context, dispatcherServlet, path, true);
	}
	
	/**
	 * Removes Dotcms services that are tracked by the ExtendedGenericBundleActivator. These are
	 * services that require more than just a simple register/unregister. For instance Servlets and Filters.
	 */
	protected void removeTrackedServices() {
		for(ServiceTracker<ExtHttpService, ExtHttpService> tracker: trackers) {
			tracker.close();
		}
	}
	
}
