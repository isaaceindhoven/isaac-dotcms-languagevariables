package nl.isaac.dotcms.languagevariables.util;

import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.Structure;

public class LanguageVariable {
	private final Contentlet contentlet;
	
	public LanguageVariable(Contentlet contentlet) {
		if(isLanguageVariable(contentlet)) {
			this.contentlet = contentlet;
		} else {
			throw new IllegalArgumentException("Contentlet is not a Language Variable");
		}
	}
	
	public static boolean isLanguageVariable(Contentlet contentlet) {
		Structure structure = contentlet.getStructure();
		return structure.getVelocityVarName().equals(Configuration.getStructureVelocityVarName());
	}
	
	
	public String getKey() {
		return contentlet.getStringProperty(Configuration.getStructureKeyField());
	}
	
	public String getHostIdentifier() {
		return contentlet.getHost();
	}
	
	public long getLanguageId() {
		return contentlet.getLanguageId();
	}
	
	public String getInode() {
		return contentlet.getInode();
	}
	
	public Contentlet getContentlet() {
		return contentlet;
	}
	
	public String getValue() {
		return contentlet.getStringProperty(Configuration.getStructureValueField());		
	}
	
	public boolean hasTheSameKeyLanguaegAndHost(LanguageVariable languageVariable) {
		return languageVariable.getKey().equals(getKey()) &&
				languageVariable.getHostIdentifier().equals(getHostIdentifier()) &&
				languageVariable.getLanguageId() == getLanguageId();
	}
	
}
