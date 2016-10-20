package nl.isaac.dotcms.languagevariables.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotcms.repackage.org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.liferay.portal.model.User;

import nl.isaac.dotcms.languagevariables.util.Configuration;
import nl.isaac.dotcms.languagevariables.util.ContentletQuery;
import nl.isaac.dotcms.languagevariables.util.RequestUtil;

public class UnarchiveVariable extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hostIdentifier = new RequestUtil(request).getCurrentHost().getIdentifier();
		String languageId = request.getParameter("languageId");
		String contentletIdentifier = request.getParameter("contentletIdentifier");
		String referer = request.getParameter("referer");
		
		if (!StringUtils.isBlank(hostIdentifier) && !StringUtils.isBlank(languageId) && !StringUtils.isBlank(contentletIdentifier) && !StringUtils.isBlank(referer)) {		
			
			ContentletQuery archivedContentletQuery = new ContentletQuery(Configuration.getStructureVelocityVarName());
			archivedContentletQuery.addHostAndIncludeSystemHost(hostIdentifier);
			archivedContentletQuery.addIdentifierLimitations(true, contentletIdentifier);	
			archivedContentletQuery.addLanguage(languageId);
			archivedContentletQuery.addWorking(true);
			archivedContentletQuery.addDeleted(true);
			
			Contentlet archivedContent = archivedContentletQuery.executeSafeSingle();
			
			if (archivedContent != null) {
				try {
					User systemUser = APILocator.getUserAPI().getSystemUser();
					APILocator.getContentletAPI().unarchive(archivedContent, systemUser, false);
					request.getSession().setAttribute("languagevariables_unarchive_status", true);
				} catch (Exception e) {
					throw new RuntimeException("Error occured while unarchiving the Language Variable");
				}
			}
			
			// Wait until the cache/index is updated, and redirect afterwards
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
			
			response.sendRedirect(referer);
		} else {
			response.sendRedirect("/c/portal/layout");
		}
	}

}
