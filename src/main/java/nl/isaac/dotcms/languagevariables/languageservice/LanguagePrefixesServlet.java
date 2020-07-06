package nl.isaac.dotcms.languagevariables.languageservice;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

public class LanguagePrefixesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties properties = new Properties();
		String prefix = request.getParameter("prefix");

		// Return an empty list when no prefix is entered
		if (UtilMethods.isSet(prefix)) {

			// retrieve the key-value pairs
			LanguageVariablesAPI contentGlossaryAPI = new LanguageVariablesAPI(request);

			List<String> prefixes = Arrays.asList(prefix.split(","));

			List<KeyValuePair<String, String>> keyValuePairs = contentGlossaryAPI.getKeysWithPrefixes(prefixes);

			// add the key-values as properties
			for (KeyValuePair<String, String> keyValuePair : keyValuePairs) {
				String key = keyValuePair.getKey();
				String value = keyValuePair.getValue();

				if (key == null) {
					Logger.warn(this, "Encountered null key, skip it...");
					continue;
				}

				if (value == null) {
					Logger.debug(this, "Encountered null value, replace it with an empty String...");
					value = "";
				}

				properties.setProperty(key, value);
			}
		}

		// set the correct character encoding:
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		// If the property value contains an '=', it will be escaped to '\='.
		// This will break the jquery.i18n.properties.isaac.js plugin, so we
		// 'unescape' all escaped equals signs.
		StringWriter writer = new StringWriter();
		properties.store(writer, "Key-value pairs");

		String propertiesWithoutEscapedEqualsSign = writer.toString().replace("\\=", "=");
		response.getWriter().write(propertiesWithoutEscapedEqualsSign);
	}
}