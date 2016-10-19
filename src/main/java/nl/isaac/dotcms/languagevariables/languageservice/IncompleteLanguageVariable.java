package nl.isaac.dotcms.languagevariables.languageservice;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.languagevariables.util.Configuration;
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
	private Action action;
	
	public IncompleteLanguageVariable(LanguageVariableContentlet contentlet, IncompleteStatus status, String keyName, String languageId, String referer, String hostIdentifier) {
		super();
		this.status = status;
		this.keyName = keyName;
		this.languageId = languageId;
		this.contentlet = contentlet;
		this.action = new Action(new URLUtil(referer, hostIdentifier));
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
	
	public LanguageVariableContentlet getContentlet() {
		return contentlet;
	}
	
	public Action getAction() {
		return action;
	}
	
	public class Action {
		
		private String text;
		private String url;
		
		public Action(URLUtil urlUtil) {
			super();
			
			switch (status) {
				case ARCHIVED:
					text = "Edit";
					url = urlUtil.getArchivedURL();
					break;
				case UNPUBLISHED:
					text = "Edit";
					url = urlUtil.getUnpublishedURL();
					break;
				case MISSING:
					Language language = APILocator.getLanguageAPI().getLanguage(contentlet.getLanguageId());
					StringBuilder sb = new StringBuilder("Create from ");
					sb.append("<img title=\"Get key and value from " + language.getLanguage() + "\"");
					sb.append("src=\"/html/images/languages/" + language.getLanguageCode() + "_" + language.getCountryCode() + ".gif\"");
					sb.append(">");
					text = sb.toString();
					url = urlUtil.getMissingURL();
					break;
				case NOT_FOUND:
					text = "Create";
					url = urlUtil.getNewURL();
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getText() {
			return text;
		}
		
	}
	
	public class URLUtil {
		
		private String referer;
		private String hostIdentifier;
		
		public URLUtil(String referer, String hostIdentifier) {
			super();
			this.referer = referer;
			this.hostIdentifier = hostIdentifier;
		}
		
		public String getArchivedURL() {
			return "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view"
					+ "&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=edit&inode="
					+ contentlet.getInode()
					+ "&referer=" + referer;
		}
		
		public String getUnpublishedURL() {
			return "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view"
					+ "&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=edit&inode="
					+ contentlet.getInode()
					+ "&referer=" + referer;
		}
		
		public String getMissingURL() {
			Structure structure = StructureFactory.getStructureByVelocityVarName(Configuration.getStructureVelocityVarName());
			
			if (UtilMethods.isSet(structure.getName())) {
			   return "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&_EXT_11_referer="
					   + referer
					   + "&_EXT_11_sibbling="
					   + contentlet.getInode()
					   + "&_EXT_11_cmd=edit&_EXT_11_sibblingStructure="
					   + structure.getInode()
					   + "&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&inode="
					   + "&lang="
					   + languageId
					   + "&host="
					   + hostIdentifier + "&folder="
					   + "&reuseLastLang=true&populateaccept=true";
			}
			
			return null;
		}
		
		public String getNewURL() {
			Structure structure = StructureFactory.getStructureByVelocityVarName(Configuration.getStructureVelocityVarName());
			
			if (UtilMethods.isSet(structure.getName())) {
				return "/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view"
						+ "&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=new&_EXT_11_referer="
						+ referer
						+ "&_EXT_11_inode="
						+ "&selectedStructure="
						+ structure.getInode()
						+ "&lang=" + languageId;
			}
			
			return null;
		}
		
	}
	
}
