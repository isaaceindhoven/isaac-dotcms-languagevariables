package nl.isaac.dotcms.languagevariables.cache.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.languagesmanager.model.Language;

import nl.isaac.dotcms.languagevariables.cache.LanguageListCacheGroupHandler;
import nl.isaac.dotcms.languagevariables.util.Configuration;

/**
 * Flush cache for portlet keys without values.
 * 
 * @author danny.gloudemans
 *
 */
public class FlushVariablesCache extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String referer = request.getParameter("referer");

		List<Language> languages = APILocator.getLanguageAPI().getLanguages();

		for (Language language : languages) {
			LanguageListCacheGroupHandler.getInstance()
					.remove(Configuration.CacheListKeysWithoutValue + language.getId());
		}

		request.getSession().setAttribute("languagevariables_flush_status", true);
		response.sendRedirect(referer);
	}

}
