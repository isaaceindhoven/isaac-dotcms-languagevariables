package nl.isaac.dotcms.languagevariables.languageservice;

import nl.isaac.dotcms.languagevariables.util.LanguageVariableContentlet;

public class IncompleteLanguageVariable {

	enum IncompleteStatus {
		ARCHIVED,
		UNPUBLISHED,
		MISSING,
		NOT_FOUND
	}

	private LanguageVariableContentlet contentlet;
	private IncompleteStatus status;
	private String keyName;
	private String languageId;

	public IncompleteLanguageVariable(LanguageVariableContentlet contentlet, IncompleteStatus status, String keyName, String languageId) {
		this.contentlet = contentlet;
		this.status = status;
		this.keyName = keyName;
		this.languageId = languageId;
	}

	public LanguageVariableContentlet getContentlet() {
		return contentlet;
	}

	public IncompleteStatus getStatus() {
		return status;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getLanguageId() {
		return languageId;
	}

}
