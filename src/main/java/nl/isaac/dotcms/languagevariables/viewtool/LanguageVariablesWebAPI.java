package nl.isaac.dotcms.languagevariables.viewtool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;

import nl.isaac.dotcms.languagevariables.languageservice.LanguageVariablesAPI;
import nl.isaac.dotcms.languagevariables.util.Configuration;
import nl.isaac.dotcms.languagevariables.util.LanguageVariablesUtil;

/**
 *
 * @author jorith.vandenheuvel
 *
 */
public class LanguageVariablesWebAPI implements ViewTool {
	
	private LanguageVariablesUtil util;
	
	@Override
	public void init(Object obj) {
		this.util = new LanguageVariablesUtil(((ViewContext) obj));
	}
	
	public String get(String key) {
		return util.get(key);
	}

	public String get(String key, String languageId) {
		return util.get(key, languageId);
	}
	
	public List<String> getKeysWithoutValue(String languageId) {
		return util.getKeysWithoutValue(languageId);
	}
	
	public String getLanguageVariableContentletURL(HttpServletRequest request, String key, String languageId, String referer) {
		LanguageVariablesAPI languageVariablesAPI = new LanguageVariablesAPI(request);
		try {
			return languageVariablesAPI.getLanguageVariableContentletURL(key, languageId, URLEncoder.encode(referer, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Logger.warn(this, "Error occured while encoding referer URL", e);
		}
		return null;
	}
	
	public Structure testStuctureExists() {
		Structure structure = 
				StructureFactory
				.getStructures()
				.stream()
				.filter((s) -> Configuration.getStructureVelocityVarName().equalsIgnoreCase(s.getVelocityVarName()))
				.findFirst()
				.orElse(null);
		
		return structure;
	}
	
	// Used in the monitoringservlet to test the basic functionality
	public String testLanguageVariable() {
		if (testStuctureExists() == null) {
			return null;
		}
		
		// Non-existing language variable key
		final String nonExistingLanguageKey = "123a-non-existing-key456";
		
		String languageValue = util.get(nonExistingLanguageKey);
		if (nonExistingLanguageKey.equals(languageValue)) {
			return nonExistingLanguageKey;
		}
		
		return null;
	}
}
