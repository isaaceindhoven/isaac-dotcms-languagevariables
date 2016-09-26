package nl.isaac.dotcms.languagevariables.languageservice;

import java.util.Comparator;

import com.dotmarketing.portlets.contentlet.model.Contentlet;

public class LanguageKeyComparator implements Comparator<Contentlet> {

	public int compare(Contentlet o1, Contentlet o2) {
		return o1.getStringProperty("key").compareTo(o2.getStringProperty("key"));
	}


}
