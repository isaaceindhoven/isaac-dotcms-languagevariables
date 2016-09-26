package nl.isaac.dotcms.util.osgi;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.apache.velocity.tools.view.context.ViewContext;
import org.osgi.framework.BundleContext;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;

/**
 * Provides a convenience method for adding viewtools.
 * @author Maarten
 *
 */
public abstract class ExtendedGenericBundleActivator extends GenericBundleActivator {

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
	

	protected void registerLanguageVariables(Map<String, String> languageVariables, Language language) {
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
	protected void registerLanguageVariables(BundleContext context) {
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
			registerLanguageVariables(languageVariables, APILocator.getLanguageAPI().getLanguage("en", "US"));
			
		} catch (IOException e) {
			Logger.warn(this, "Exception while registering language variables", e);
		}
	}
	
}
