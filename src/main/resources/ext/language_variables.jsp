<%@page import="com.dotmarketing.business.web.WebAPILocator"%>
<%@page import="com.dotmarketing.business.web.HostWebAPIImpl"%>
<%@page import="com.dotmarketing.business.web.HostWebAPI"%>
<%@page import="com.dotmarketing.portlets.contentlet.model.Contentlet"%>
<%@page import="nl.isaac.dotcms.languagevariables.viewtool.LanguageVariablesWebAPI"%>
<%@ page import="com.dotmarketing.util.Logger"%>
<%@ page import="com.dotmarketing.business.web.LanguageWebAPI"%>
<%@ page import="com.liferay.portal.util.PortalUtil"%>
<%@ page import="com.dotmarketing.business.Layout"%>
<%@ page import="com.dotmarketing.business.UserAPI"%>
<%@ page import="javax.portlet.WindowState"%>
<%@ page import="java.util.*" %>
<%@ page import="com.dotmarketing.portlets.languagesmanager.model.Language" %>
<%@ page import="com.dotmarketing.portlets.structure.model.Structure" %>
<%@ page import="com.dotmarketing.portlets.structure.factories.StructureFactory" %>
<%@ page import="com.liferay.portal.model.User" %>
<%@ page import="com.dotmarketing.portlets.languagesmanager.business.*" %>
<%@ page import="com.dotmarketing.business.APILocator" %>
<%@ page import="com.dotmarketing.util.Config" %>
<%@ page import="com.dotmarketing.util.UtilMethods" %>
<%@ page import="com.dotmarketing.util.InodeUtils" %>
<%@ page import="com.dotmarketing.cache.StructureCache"%>
<%@ page import="com.liferay.portal.language.LanguageUtil"%> 
<%@ page import="com.dotmarketing.business.Role"%> 
<%@ page import="com.dotmarketing.business.RoleAPI"%> 
<%@ page import="com.dotmarketing.business.RoleAPIImpl"%>
<%@ page import="com.dotmarketing.portlets.folders.model.Folder"%>
<%@ page import="com.dotmarketing.beans.Host"%>
<%@ page import="com.dotmarketing.cache.FieldsCache"%>
<%@ page import="com.dotmarketing.portlets.structure.model.Field"%>
<%@ page import="com.dotmarketing.business.PermissionAPI"%>
 
<%
 	List<Language> languages = APILocator.getLanguageAPI().getLanguages();
          Map lastSearch = (Map)session.getAttribute(com.dotmarketing.util.WebKeys.CONTENTLET_LAST_SEARCH);
  		Structure structure = StructureCache.getStructureByVelocityVarName("LanguageVariables");
          User user = null;
      	user = PortalUtil.getUser(request);
  		Host host = WebAPILocator.getHostWebAPI().getCurrentHost(request);
          
      	String orderBy = "modDate desc";
      	int currpage = 1;
      	
      	Language selectedLanguage = new Language();
          Language defaultLang = APILocator.getLanguageAPI().getDefaultLanguage();
          String languageId = String.valueOf(defaultLang.getId());
          if(request.getAttribute(com.dotmarketing.util.WebKeys.LANGUAGE_SEARCHED)!= null){
              selectedLanguage = (Language)request.getAttribute(com.dotmarketing.util.WebKeys.LANGUAGE_SEARCHED);
          }
          long selectedLanguageId = selectedLanguage.getId();
          
          boolean showDeleted = false;
          boolean filterSystemHost = false;
          boolean filterLocked = false;
          
          java.util.Map params = new java.util.HashMap();
  		String referer = com.dotmarketing.util.PortletURLUtil.getActionURL(request,WindowState.MAXIMIZED.toString(),params);
 %>

<jsp:include page="/html/portlet/ext/folders/context_menus_js.jsp" />
<script type='text/javascript' src='/dwr/interface/StructureAjax.js'></script>
<script type='text/javascript' src='/dwr/interface/CategoryAjax.js'></script>
<script type='text/javascript' src='/dwr/interface/ContentletAjax.js'></script>
<script type='text/javascript' src='/dwr/engine.js'></script>
<script type='text/javascript' src='/dwr/util.js'></script>
<script type='text/javascript' src='/dwr/interface/TagAjax.js'></script>
<script type='text/javascript' src='/dwr/interface/LanguageAPI.js'></script>

<!-- START Host wrapper -->
<div class="portlet-wrapper">
<jsp:include page="/html/portlet/ext/browser/sub_nav.jsp"></jsp:include>
</div>
<!-- END Host wrapper -->

<!-- START Button Row -->
        <div class="buttonBoxLeft">
	        <b><%=LanguageUtil.get(pageContext, "Type")%>: Language variables</b>
        </div>

 <!--       <div class="buttonBoxRight">
             <div id="addNewMenu"></div>
        </div> -->

<!-- END Button Row -->

<!-- START Split Screen -->
<div dojoType="dijit.layout.BorderContainer" design="sidebar" gutters="false" liveSplitters="true" style="height:400px;" id="borderContainer" class="shadowBox headerBox">

<!-- START Left Column -->
        <div dojoType="dijit.layout.ContentPane" splitter="false" region="leading" style="width: 350px;" class="lineRight">

                <div id="filterWrapper" style="overflow-y:auto; overflow-x:hidden;margin:43px 0 0 5px;">


                        <%
                        	List<Structure> readStructs = StructureFactory.getStructuresWithReadPermissions(user, true);
                        %>
                        <%
                        	if((readStructs.size() == 0)){
                        %>
                                <div align="center" style="text-align:center;">
                                        <dt><FONT COLOR="#FF0000"><%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "No-Structure-Read-Permissions" ))%></FONT></dt>
                                </div>
                        <%
                        	}
                        %>

                        <!-- START Advanced Search-->
                        <div id="advancedSearch">
                            <dl>
                                <dt><%=LanguageUtil.get(pageContext, "Key")%>:</dt>
                                <dd>
                                   	<input id="keyTextBox" data-dojo-type="dijit.form.TextBox" data-dojo-props="intermediateChanges:true"></input>
                                </dd>
                            </dl>
							<div class="clear"></div>
							<%
								if (languages.size() > 1) {
							%>
								<dl>
                                <dt>Missing values for:</dt>
                                <dd>
									<div id="combo_zone2" style="width:215px; height:20px;">
                                        <input id="language_id" data-dojo-props="intermediateChanges:true" />
                                    </div>
                                    <script>
										<%StringBuffer buff = new StringBuffer();
										// http://jira.dotmarketing.net/browse/DOTCMS-6148
										buff.append("{identifier:'id',imageurl:'imageurl',label:'label',items:[");

										String imageURL="/html/images/languages/all.gif";
									    String style="background-image:url(URLHERE);width:16px;height:11px;display:inline-block;vertical-align:middle;margin:3px 5px 3px 2px;";
										buff.append("{id:'0',value:'',lang:'All',imageurl:'"+imageURL+"',label:'<span style=\""+style.replaceAll("URLHERE",imageURL)+"\"></span>All'}");
												
										for (Language lang : languages) {
										    imageURL="/html/images/languages/" + lang.getLanguageCode()  + "_" + lang.getCountryCode() +".gif";
											final String display=lang.getLanguage() + " - " + lang.getCountry().trim();
											buff.append(",{id:'"+lang.getId()+"',");
											buff.append("value:'"+lang.getId()+"',");
											buff.append("imageurl:'"+imageURL+"',");
											buff.append("lang:'"+display+"',");
											buff.append("label:'<span style=\""+style.replaceAll("URLHERE",imageURL)+"\"></span>"+display+"'}");
										}
												    
										buff.append("]}");%>

										function updateSelectBoxImage(myselect) {
											var imagestyle = "url('" + myselect.item.imageurl + "')";
											var selField = dojo.query('#combo_zone2 div.dijitInputField')[0];
											dojo.style(selField, "backgroundImage", imagestyle);
											dojo.style(selField, "backgroundRepeat", "no-repeat");
											dojo.style(selField, "padding", "0px 0px 0px 25px");
											dojo.style(selField, "backgroundColor", "transparent");
											dojo.style(selField, "backgroundPosition", "3px 6px");
										}

										var storeData=<%=buff.toString()%>;
										var langStore = new dojo.data.ItemFileReadStore({data: storeData});
											
										var myselect = new dijit.form.FilteringSelect({
											id: "language_id",
											name: "language_id",
											value: '',
											required: true,
											store: langStore,
											searchAttr: "lang",
											labelAttr: "label",
											labelType: "html",
											onChange: function() {
												var el=dijit.byId('language_id');
												updateSelectBoxImage(el);
											},
											labelFunc: function(item, store) { return store.getValue(item, "label"); }
										},
											
										dojo.byId("language_id"));

										<%if(languageId.equals("0")) {%>
											myselect.setValue('<%=languages.get(0).getId()%>');
										<%} else {%>
											myselect.setValue('<%=languageId%>');
										<%}%>

									</script>
									
                                </dd>
								</dl>
								<div class="clear"></div>
                            <%
                            	} else {
                            %>
								<%
									long langId = languages.get(0).getId();
								%>
                                <input type="hidden" name="language_id" id="language_id" value="<%=langId%>">
                            <%
                            	}
                            %>
							<dl>
								<dt><%=LanguageUtil.get(pageContext, "Archived-only")%>:</dt>
                                <dd><input type="checkbox" dojoType="dijit.form.CheckBox" id="showDeletedCB" onclick="displayArchiveButton();" <%=showDeleted?"checked=\"checked\"":""%>></dd>
							</dl>
							<!-- Ajax built search fields  --->
                            <div id="search_fields_table"></div>
							<div class="clear"></div>
							<!-- /Ajax built search fields  --->
						</div>
						<!-- END Advanced Search-->
                </div>
        </div>
<!-- END Left Column -->

<!-- START center Column -->
<div dojoType="dijit.layout.ContentPane" splitter="false" region="left" style="width: 350px;" class="lineRight">

	<div id="contentWrapper" style="overflow-y:auto; overflow-x:auto;margin:35px 0 0 0; height: 80%;">

	<!--Start form for post actions of the center -->
	<form method="Post" action="" id="search_form" onsubmit="doSearch();return false;">
		<input type="hidden" name="fullCommand" id="fullCommand" value="">
		<input type="hidden" name="luceneQuery" id="luceneQuery" value="+structureName:LanguageVariables +(conhost:<%=host.getIdentifier()%> conhost:SYSTEM_HOST) +deleted:false  +working:true">
		<input type="hidden" name="structureInode" id="structureInode" value="<%=structure.getInode()%>">
		<input type="hidden" name="fieldsValues" id="fieldsValues" value="conHost,<%=host.getIdentifier()%>,LanguageVariables.key,,LanguageVariables.value,">
		<input type="hidden" name="categoriesValues" id="categoriesValues" value="">
		<input type="hidden" name="showDeleted" id="showDeleted" value="false">
		<input type="hidden" name="filterSystemHost" id="filterSystemHost" value="false">
		<input type="hidden" name="filterLocked" id="filterLocked" value="false">
		<input type="hidden" name="currentPage" id="currentPage" value="">
		<input type="hidden" name="currentSortBy" id="currentSortBy" value="modDate desc">
		<input type="hidden" value="" name="lastModDateFrom" id="lastModDateFrom" size="10" maxlength="10" readonly="true">
		<input type="hidden" value="" name="lastModDateTo" id="lastModDateTo" size="10" maxlength="10" readonly="true">
		<input type="hidden" name="structureVelocityVarNames" id="structureVelocityVarNames" value="">
		<input type="hidden" name="structureInodesList" id="structureInodesList" value="">
		<input type="hidden" name="hostField" id="hostField" value="<%=host.getIdentifier()%>">
		<input type="hidden" name="LanguageVariables.key" id="LanguageVariables.key" value=""/>
		<input type="hidden" name="LanguageVariables.value" id="LanguageVariables.value" value=""/>
		<input type="hidden" name="allSearchedContentsInodes" id="allSearchedContentsInodes" value=""/>
		<input type="hidden" name="allUncheckedContentsInodes" id="allUncheckedContentsInodes" value=""/>
		<input type="hidden" name="folderField" id="folderField" value=""/>
		<input type="hidden" name="language_id" value="0">
		
		<div id="metaMatchingResultsDiv" style="display:none;padding-top:7px;">
			<!-- START Listing Results -->
			<input type="hidden" name="referer" value="<%=referer%>"> 
			<input type="hidden" name="cmd" value="prepublish">
			<div id="matchingResultsDiv" style="display: none"></div>
			<table id="results_table" class="listingTable"></table>
			<div id="results_table_popup_menus"></div>
			<div class="clear"></div>
			<!-- END Listing Results -->
		</div>
		<!-- END Pagination -->
	
		<!-- START Listing Results -->
		<table class="listingTable" >
			<tr>	
				<th width="25%">
				</th>
				<th width="75%">
					<input type="checkbox" dojoType="dijit.form.CheckBox" name="checkAll" id="checkAll" onclick="checkUncheckAll()"> <%=LanguageUtil.get(pageContext, "Key")%>
				</th>
			</tr>
	 
			<%
	 				ContentGlossaryAPI languageAPI = new ContentGlossaryAPI();
	 				 					int count = 0;
	 				 					List<Contentlet> contentlets = new ArrayList<Contentlet>();
	 				 					for (Contentlet contentlet : languageAPI.getLanguageKeysTemp()) { count++;
	 			%>

			<tr id="tr<%=contentlet.getInode()%>" class="alternate_1" onclick="viewLanguageValues('<%=contentlet.getStringProperty("key")%>', <%=contentlet.isArchived()%>);" >
				<td>
					<%
						if(contentlet.isWorking() && (!contentlet.isLive() && !contentlet.isArchived())){
					%>
						<span class="workingIcon"></span>
					<%
						} else {
					%>
						<span class="greyDotIcon"></span>
					<%
						} if(contentlet.isLive()){
					%>
						<span class="liveIcon"></span>
					<%
						} else if(contentlet.isArchived()) {
					%>
						<span class="archivedIcon"></span>
					<%
						} else {
					%> 
						<span class="greyDotIcon"></span>
					<%
						} if(contentlet.isLocked()){
					%>
						<span class="lockIcon"></span>
					<%
						}
					%>
				</td>
				<td> <input dojoType="dijit.form.CheckBox" type="checkbox" name="publishInode" id="checkbox<%=count%>" value="<%=contentlet.getInode()%>" onClick="togglePublish();updateUnCheckedList('<%=contentlet.getInode()%>','checkbox<%=count%>')" /><%=contentlet.getStringProperty("key")%></td>
			</tr>
			
			<%
							}
						%> 
		</table>
	<!-- END Listing Results -->
		</form> <!-- END Form -->	
	</div>
	
	<div class="clear"></div>

	<!-- START Buton Row -->
	<div class="buttonRow" style="max-height:10px">
		<div id="archiveButtonDiv" style="display:none;">
			<button dojoType="dijit.form.Button" id="unArchiveButton" onClick="unArchiveSelectedContentlets()" iconClass="unarchiveIconDis" disabled="true" >
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Un-Archive"))%>
			</button>

			<button dojoType="dijit.form.Button" id="deleteButton" onClick="deleteSelectedContentlets()" iconClass="deleteIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Delete"))%>
			</button>

			 <!--<button dojoType="dijit.form.Button" id="archiveUnlockButton" onClick="unlockSelectedContentlets()" iconClass="unlockIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Unlock"))%>
			</button> -->
		</div>

		<div id="unArchiveButtonDiv">
			<button dojoType="dijit.form.Button" id="publishButton"  onClick="publishSelectedContentlets()" iconClass="publishIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Publish"))%>
			</button>

            <button dojoType="dijit.form.Button"  id="unPublishButton" onClick="unPublishSelectedContentlets()" iconClass="unpublishIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Unpublish"))%>
            </button>

            <button dojoType="dijit.form.Button" id="archiveButton" onClick="archiveSelectedContentlets()" iconClass="archiveIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Archive"))%>
            </button>

           <!-- <button dojoType="dijit.form.Button" id="unlockButton" onClick="fillResultsTable()" iconClass="unlockIconDis" disabled="true">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Unlock"))%>
			</button> -->

		</div>
    </div>
	<!-- END Buton Row -->
</div>
<!-- END center Column -->


<!-- START Right Column -->
<div dojoType="dijit.layout.ContentPane" splitter="false" region="center">
		
	<div id="contentWrapper" style="overflow-y:auto; overflow-x:hidden;margin:35px 0 0 0;">
		<%
			LanguageVariablesWebAPI languageviewtool = new LanguageVariablesWebAPI();
		%>

		<!-- START Listing Table -->
		<div id="fieldsTable" style="width:100%;">

			<!-- START Table Header -->
			<div id="fieldsTableHeader" style="verflow-x: hidden;">
				<div id="fieldsTableHeaderCell00" class="item_header_cell" style="width:30px;">
					<input type="hidden" id="hiddenKey" name="hiddenKey" value=""/>
				</div>
				<div id="fieldsTableHeaderCell01" class="item_header_cell structureFieldLabelClass" style="width:150px;"><b><%= LanguageUtil.get(pageContext, "Language") %></b></div>
				<div id="fieldsTableHeaderCell02" class="item_header_cell"><b><%= LanguageUtil.get(pageContext, "Value") %></b></div>
				<div style="clear:both;"></div>
			</div>
			<!-- END Table Header -->

			<!-- START Table Results -->
			<div dojoType="dojo.dnd.Source">

			<% 	int i = 0;
				for (Language language : languages) {
					String color = (i % 2 == 0 ? "#FFFFFF" : "#EEEEEE");
					i++;
			%>

				<div style="white-space:no-wrap; background-color: <%=color %>;" id="">
					<span class="hiddenInodeField" style="display:none; width: 0px;"></span>
					<input class="orderBox" type="hidden" value="<%=i+1%>">

					<div class="item_cell" style="width:30px; height: auto;">
			           	<img src="/html/images/languages/<%=language.getLanguageCode()%>_<%=language.getCountryCode()%>.gif"  border="0" />
					</div>
					<div class="item_cell" style="width:150px; height: auto;">
						<%= language.getLanguage() %>
					</div>
					<div class="item_cell" style="height: auto; width: 57%">
						<textarea class="dijitTextArea" name="textArea<%=language.getLanguageCode() %>_<%=language.getCountryCode() %>" id="textArea<%=language.getLanguageCode() %>_<%=language.getCountryCode() %>" data-dojo-type="dijit.form.Textarea" rows="2" cols="50" ></textarea>
					</div>
					<div style="clear:both;"></div>
				</div>
					
			<% }//end for %>
		</div>
		<!-- END Table Results -->

		</div>
	</div>

<!-- END Listing Table -->
	<div class="clear"></div>
			
	<!-- START Buton Row -->
    <div class="buttonRow">
						
		<div id="rightArchiveButtonDiv" style="display:none">
			<button dojoType="dijit.form.Button" id="unArchiveButton" onClick="unarchiveContentlet()" iconClass="unarchiveIconDis" disabled="true" >
				<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Un-Archive")) %>
				<a id="myLink" href="#" onclick="unarchiveContentlet();">Un-archive</a>

            </button>

			<button dojoType="dijit.form.Button" id="deleteButton" onClick="deleteSelectedContentlets()" iconClass="deleteIconDis" disabled="true">
				<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Delete"))%>
            </button>
        </div>

        <div id="rightUnArchiveButtonDiv">
			<button dojoType="dijit.form.Button" id="publishButton"  onClick="publishContentlet()" iconClass="publishIconDis" disabled="false">
				<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Publish")) %>
				<a id="myLink" href="#" onclick="publishContentlet();">Publish</a>
            </button>

            <button dojoType="dijit.form.Button"  id="unPublishButton" onClick="unpublishContentlet()" iconClass="unpublishIconDis" disabled="true">
				<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Unpublish")) %>
				<a id="myLink" href="#" onclick="unpublishContentlet();">Unpublish</a>
            </button>

            <button dojoType="dijit.form.Button" id="archiveButton" onClick="archiveContentlet()" iconClass="archiveIconDis" disabled="true">
				<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "Archive"))%>
				<a id="myLink" href="#" onclick="archiveContentlet();">Archive</a>
			</button>
        </div>
    </div>
	
    <!-- END Buton Row -->
</div>
<!-- END Right Column -->

</div>
<!-- END split screen -->
<script type="text/javascript">


	var myHandler_oldOnload = window.onload;  
	function myHandler()  
	{  
		var box0 = dijit.byId("keyTextBox");
		dojo.connect(box0, "onChange", "filterKeyList");
		
		var select0 = dijit.byId("language_id");
		dojo.connect(select0, "onChange", "selectMissingValues");
		
		if(myHandler_oldOnload)myHandler_oldOnload();  
	}  
	window.onload = function(){myHandler();}  

    <!-- START Functions for left screen -->

    <!-- Filter list on key -->
    function filterKeyList(){
		var textbox = dijit.byId("keyTextBox"); 
		console.log("Filter key list: " + textbox.get("value"));
	}  
	
	<!-- Filter list on missing values per language id -->
	<!-- Value of selectbox is Language id -->
	function selectMissingValues() {
		var selectBox = dijit.byId("language_id"); 
		console.log("Filter key list: " + selectBox.get("value"));
	}  
	
	<!-- END Functions for left screen -->
	
	<!-- START Functions for center screen -->
	<!-- Publish selected contentlets from the center screen  -->
	function publishSelectedContentlets () {
		disableButtonRow();
        var form = document.getElementById("search_form");
        form.cmd.value = 'full_publish_list';
        form.action ='/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=full_publish_list';
        form.action+= "&structure_id=<%=structure.getInode()%>";
        form.action += "&selected_lang=0";
        submitForm(form);
	}

	<!-- Unpublish selected contentlets from the center screen -->
	function unPublishSelectedContentlets() {
		disableButtonRow();
		var form = document.getElementById("search_form");
        form.cmd.value = 'full_unpublish_list';
        form.action ='/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=full_publish_list';
        form.action+= "&structure_id=<%=structure.getInode()%>";
        form.action += "&selected_lang=0";
        submitForm(form);
	}

	<!-- Archive selected contentlets from the center screen -->
	function archiveSelectedContentlets() {
		disableButtonRow();
		var form = document.getElementById("search_form");
        form.cmd.value = 'full_archive_list';
        form.action ='/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=full_publish_list';
        form.action+= "&structure_id=<%=structure.getInode()%>";
        form.action += "&selected_lang=0";
        submitForm(form);
	} 
	
	<!-- Unarchive selected contentlets from the center screen -->
	function unArchiveSelectedContentlets() {
		disableButtonRow();
		var form = document.getElementById("search_form");
        form.cmd.value = 'full_unarchive_list';
        form.action ='/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=full_publish_list';
        form.action+= "&structure_id=<%=structure.getInode()%>";
        form.action += "&selected_lang=0";
        submitForm(form);
	} 
	
	<!-- Delete selected contentlets from the center screen -->
	function deleteSelectedContentlets(){
		disableButtonRow();
		if(confirm('<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "message.contentlet.confirm.delete")) %>')){
			var form = document.getElementById("search_form");
			form.cmd.value = 'full_delete_list';
			form.action ='/c/portal/layout?p_l_id=71b8a1ca-37b6-4b6e-a43b-c7482f28db6c&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet&_EXT_11_cmd=full_publish_list';
			form.action+= "&structure_id=<%=structure.getInode()%>";
            submitForm(form);
        }
    }
	
	<!-- Get language values for the right screen -->
    function viewLanguageValues(key, archived){
	if (archived) {
        document.getElementById("rightArchiveButtonDiv").style.display="";
        document.getElementById("rightUnArchiveButtonDiv").style.display="none";
    } else {
        document.getElementById("rightArchiveButtonDiv").style.display="none";
        document.getElementById("rightUnArchiveButtonDiv").style.display="";
    }
		
	dijit.byId(hiddenKey).value = key;
		<% for(Language language : languages) { %>

			LanguageAPI.getStringKey(<%=language.getId()%>, key, { 
                   callback : function(str) { 
				   	dijit.byId(textArea<%=language.getLanguageCode() %>_<%=language.getCountryCode() %>).value = str;
                   } 
                 });
		<% } %>   	
    }
		
	function disableButtonRow() {
        
        if(dijit.byId("unArchiveButton"))
            dijit.byId("unArchiveButton").attr("disabled", true);
                        
        if(dijit.byId("deleteButton"))
            dijit.byId("deleteButton").attr("disabled", true);
                        
        if(dijit.byId("archiveReindexButton"))
            dijit.byId("archiveReindexButton").attr("disabled", true);
                        
        if(dijit.byId("archiveUnlockButton"))
            dijit.byId("archiveUnlockButton").attr("disabled", true);
                        
        if(dijit.byId("publishButton"))
            dijit.byId("publishButton").attr("disabled", true);
                        
        if(dijit.byId("unPublishButton"))
            dijit.byId("unPublishButton").attr("disabled", true);
                        
        if(dijit.byId("archiveButton"))
            dijit.byId("archiveButton").attr("disabled", true);
                        
        if(dijit.byId("reindexButton"))
            dijit.byId("reindexButton").attr("disabled", true);
                        
        if(dijit.byId("unlockButton"))
            dijit.byId("unlockButton").attr("disabled", true);            
        }
		
	function displayArchiveButton(){

		LanguageAPI.getAllUniqueKeys({ 
            callback : function(str) { 
			   	console.log("str: " + str);
            } 
          });

		
		var showArchive = document.getElementById("showDeletedCB").checked;
        if (showArchive) {
            document.getElementById("archiveButtonDiv").style.display="";
            document.getElementById("unArchiveButtonDiv").style.display="none";
        } else {
            document.getElementById("archiveButtonDiv").style.display="none";
            document.getElementById("unArchiveButtonDiv").style.display="";
        }
        togglePublish();
    }
	
	function checkUncheckAll() {
		var checkAll = dijit.byId("checkAll");
		var check;              
		
		togglePublish();
	}
		<!--	for (var i = 0; i <= ContentletsLIst; ++i) { -->
		<!--		check = dijit.byId("checkbox" + i); -->
		<!--		if(check) { -->
		<!--			check.setChecked(checkAll.checked); -->
		<!--		}	 -->			
		<!--	} -->
	
	function togglePublish(){
        var cbArray = document.getElementsByName("publishInode");
        var showArchive = document.getElementById("showDeletedCB").checked;
        var cbCount = cbArray.length;
        for(i = 0;i< cbCount ;i++){
            if (cbArray[i].checked) {

                if (showArchive) {
					enableFields([
						dijit.byId('unArchiveButton').setAttribute("disabled", false),
						dijit.byId('deleteButton').setAttribute("disabled", false),
					]);
				} else {
					enableFields([
						dijit.byId('archiveButton').setAttribute("disabled", false),
						dijit.byId('publishButton').setAttribute("disabled", false),
						dijit.byId('unPublishButton').setAttribute("disabled", false),
                    ]);
				}       
				break;
			}
                 
			if (showArchive) {
				disableFields([
					dijit.byId("unArchiveButton").setAttribute("disabled", true),                                           
					dijit.byId("deleteButton").setAttribute("disabled", true),
				]);
			} else {
				disableFields([
					dijit.byId('archiveButton').setAttribute("disabled", true),
					dijit.byId('publishButton').setAttribute("disabled", true),
					dijit.byId('unPublishButton').setAttribute("disabled", true),
				]);
			}
		}
    }
	
	function updateUnCheckedList(inode,checkId){    
                                       
		if(!document.getElementById(checkId).checked){
            unCheckedInodes = document.getElementById('allUncheckedContentsInodes').value;
                                
			if(unCheckedInodes == "")
				unCheckedInodes = inode;
			else
				unCheckedInodes = unCheckedInodes + ","+ inode;                         
		} else{
			unCheckedInodes = unCheckedInodes.replace(inode,"-");
		}
		document.getElementById('allUncheckedContentsInodes').value = unCheckedInodes;
	}
	<!-- END Functions for center screen -->
    
	<!-- START Functions for right screen -->
	function publishContentlet () {
		publishContent(true);
	}
	
	function unpublishContentlet () {
		publishContent(false);
	}
	
	function archiveContentlet () {
		archiveContent(true);
	}
	
	function unarchiveContentlet () {
		archiveContent(false);
	}
	
	function publishContent(publish) {
		var languageArray = new Array(<%=APILocator.getLanguageAPI().getLanguages().size()%>);
		var languagesCount = 0;

		var key = dojo.byId(hiddenKey).value;
		if(!key){
			alert("Please select a key!");
			return;			
		}
		<% for(Language language : languages) { %>
			languageArray [languagesCount] = new Array(2);
			languageArray [languagesCount][0] = <%=language.getId()%>;
			languageArray [languagesCount][1] = dojo.byId(textArea<%=language.getLanguageCode() %>_<%=language.getCountryCode() %>).value;
			languagesCount++;
		<% } %>
		
		LanguageAPI.publishContentlet(key, languageArray, publish);
	}
	
	function archiveContent(archive) {
		var languageArray = new Array(<%=APILocator.getLanguageAPI().getLanguages().size()%>);
		var languagesCount = 0;

		var key = dojo.byId(hiddenKey).value;
		if(!key){
			alert("Please select a key!");
			return;			
		}
		<% for(Language language : languages) { %>
			languageArray [languagesCount] = new Array(2);
			languageArray [languagesCount][0] = <%=language.getId()%>;
			languageArray [languagesCount][1] = dojo.byId(textArea<%=language.getLanguageCode() %>_<%=language.getCountryCode() %>).value;
			languagesCount++;
		<% } %>
		LanguageAPI.archiveContentlets(key, archive);
	}

	<!-- END Functions for right screen -->
	
	function getSelectedLanguageId () {
		var obj=dijit.byId('language_id');
		if(!obj)
			obj=dojo.byId('language_id');
		return obj.value;
	}

</script>