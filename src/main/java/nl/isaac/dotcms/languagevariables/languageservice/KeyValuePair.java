package nl.isaac.dotcms.languagevariables.languageservice;

public class KeyValuePair<KEY, VALUE> {
	
	private final KEY key;
	private final VALUE value;

	public KeyValuePair(KEY key, VALUE value) {
		super();
		this.key = key;
		this.value = value;
	}

	public KEY getKey() {
		return key;
	}

	public VALUE getValue() {
		return value;
	}

}
