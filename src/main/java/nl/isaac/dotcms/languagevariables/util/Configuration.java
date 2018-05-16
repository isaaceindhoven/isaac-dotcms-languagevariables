package nl.isaac.dotcms.languagevariables.util;

import nl.isaac.dotcms.util.osgi.PropertiesManager;

public class Configuration {

	public static final String CacheListKeysWithoutValue = "CacheListKeys." + Configuration.class.getName();
	private static PropertiesManager propertiesManager;

	private static String get(String key) {
		if(propertiesManager == null) {
			propertiesManager = new PropertiesManager();
		}
		return propertiesManager.get(key);
	}

	public static String getStructureVelocityVarName() {
		return get("structure.velocityvarname");
	}

	public static String getStructureKeyField() {
		return get("structure.key");
	}

	public static String getStructureValueField() {
		return get("structure.value");
	}

	public static String getStructureKeyFieldLabel() {
		return get("structure.key.label");
	}

	public static String getStructureValueFieldLabel() {
		return get("structure.value.label");
	}

	public static boolean isValueOfKeyEmptyShowKey() {
		return Boolean.valueOf(get("show-key-if-value-empty"));
	}

	public static boolean isReplacementValueAnEmptyString() {
		return Boolean.valueOf(get("replacement-value.show-empty-string"));
	}

	public static String getReplacementValueIfValueIsEmpty() {
		return get("replacement-if-value-is-empty");
	}

	public static String getDisplayKeysParameterName() {
		return get("parameter.display-keys");
	}

	public static boolean isNotFoundShowInDefaultLanguage() {
		return Boolean.valueOf(get("show-in-default-language-if-not-found"));
	}

}