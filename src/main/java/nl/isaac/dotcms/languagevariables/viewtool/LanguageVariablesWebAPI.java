package nl.isaac.dotcms.languagevariables.viewtool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;

import nl.isaac.dotcms.languagevariables.languageservice.IncompleteLanguageVariable;
import nl.isaac.dotcms.languagevariables.util.Configuration;
import nl.isaac.dotcms.languagevariables.util.LanguageVariablesUtil;

/**
 *
 * @author jorith.vandenheuvel
 *
 */
public class LanguageVariablesWebAPI implements ViewTool {
	
	private LanguageVariablesUtil languageVariablesUtil;
	
	@Override
	public void init(Object obj) {
		this.languageVariablesUtil = new LanguageVariablesUtil(((ViewContext) obj));
	}
	
	public String get(String key) {
		return languageVariablesUtil.get(key);
	}

	public String get(String key, String languageId) {
		return languageVariablesUtil.get(key, languageId);
	}
	
	public List<IncompleteLanguageVariable> getIncompleteKeys(String languageId, String referer) {
		try {
			return languageVariablesUtil.getIncompleteKeys(languageId, URLEncoder.encode(referer, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Logger.info(this.getClass().getName(), "Error occured while encoding referer URL: " + e.getMessage());
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
		final String nonExistingLanguageKey = UUID.randomUUID().toString();
		
		String languageValue = languageVariablesUtil.get(nonExistingLanguageKey);
		if (nonExistingLanguageKey.equals(languageValue)) {
			return nonExistingLanguageKey;
		}
		
		return null;
	}
}
