<%               
  HardTokenProfile profiledata = helper.profiledata;
   

  TreeMap authorizedcas = ejbcawebbean.getInformationMemory().getCANames();
  TreeMap authorizedcertprofiles = ejbcawebbean.getInformationMemory().getAuthorizedEndEntityCertificateProfileNames();
 
  HashMap caidtonamemap = ejbcawebbean.getInformationMemory().getCAIdToNameMap();
    
//  int[] hardtokentypes = {SwedishEIDProfile.TYPE_SWEDISHEID, EnhancedEIDProfile.TYPE_ENHANCEDEID};
//  String[] hardtokentypetexts = {"SWEDISHEID", "ENHANCEDEID"};

  int[] hardtokentypes = {SwedishEIDProfile.TYPE_SWEDISHEID};
  String[] hardtokentypetexts = {"SWEDISHEID"};

  int row = 0;
%>
<SCRIPT language="JavaScript">
<!--  

function checkallfields(){
    var illegalfields = 0;

    <% if(helper.profiledata instanceof IPINEnvelopeSettings){ %>
   if((document.editprofile.<%= EditHardTokenProfileJSPHelper.TEXTFIELD_VISUALVALIDITY %>.value == "")){
      alert("<%= ejbcawebbean.getText("VISUALVALCANNOTBEEMPTY") %>");
      illegalfields++;
   } 
   if(!checkfieldfordecimalnumbers("document.editprofile.<%=EditHardTokenProfileJSPHelper.TEXTFIELD_VISUALVALIDITY%>","<%= ejbcawebbean.getText("ONLYDIGITSINVISUALVALIDITY") %>"))
      illegalfields++;
   <% } %>
   if((document.editprofile.<%= EditHardTokenProfileJSPHelper.TEXTFIELD_SNPREFIX %>.value.length != 6)){
      alert("<%= ejbcawebbean.getText("HARDTOKENSNMUSTBESIX") %>");
      illegalfields++;
   } 
   if(!checkfieldfordecimalnumbers("document.editprofile.<%=EditHardTokenProfileJSPHelper.TEXTFIELD_SNPREFIX%>","<%= ejbcawebbean.getText("ONLYDIGITSINHARDTOKENSN") %>"))
      illegalfields++;
   
     return illegalfields == 0;  
   } 
-->

</SCRIPT>
<div align="center"> 
  <h2><%= ejbcawebbean.getText("EDITHARDTOKENPROF") %><br>
  </h2>
  <h3><%= ejbcawebbean.getText("HARDTOKENPROFILE")+ " : " + helper.profilename %> </h3>
  <% if(helper.fileuploadsuccess){
     helper.fileuploadsuccess = false;%>
  <h3><%= ejbcawebbean.getText("TEMPLATEUPLOADSUCCESSFUL")%> </h3>
  <% }if(helper.fileuploadfailed){ 
        helper.fileuploadfailed = false;%> 
  <h3><%= ejbcawebbean.getText("TEMPLATEUPLOADFAILED") %> </h3>
  <% } %>
</div>
  <table width="100%" border="0" cellspacing="3" cellpadding="3">
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="left"> 
          <h3>&nbsp;</h3>
        </div>
      </td>
      <td width="50%" valign="top"> 
        <div align="right">
        <A href="<%=THIS_FILENAME %>"><u><%= ejbcawebbean.getText("BACKTOHARDTOKENPROFILES") %></u></A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   <!--     <A  onclick='displayHelpWindow("<%= ejbcawebbean.getHelpfileInfix("hardtoken_help.html") + "#hardtokenprofiles"%>")'>
        <u><%= ejbcawebbean.getText("HELP") %></u> </A></div> -->
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("NAME") %>
      </td>
      <td width="50%"> 
        <%= helper.profilename%>
      </td>
    </tr>
    <form name="profiletype" method="post" action="<%=THIS_FILENAME %>">
      <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.ACTION %>' value='<%=EditHardTokenProfileJSPHelper.ACTION_CHANGE_PROFILETYPE %>'>
      <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.HIDDEN_HARDTOKENPROFILENAME %>' value='<%=helper.profilename %>'>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("HARDTOKENTYPE") %>
      </td>
      <td width="50%">
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_HARDTOKENTYPE%>" size="1" onchange='document.profiletype.submit()'>
           <%  int currenttype = helper.getProfileType(); 
               for(int i=0; i<hardtokentypes.length;i++){ %>
           <option  value="<%= hardtokentypes[i] %>" 
              <% if(hardtokentypes[i] == currenttype) out.write(" selected "); %>> 
              <%= ejbcawebbean.getText(hardtokentypetexts[i]) %>
           </option>
           <%   } %> 
        </select>
      </td>
    </tr>
   </form>
   </table>
   <table width="100%" border="0" cellspacing="3" cellpadding="3">
   <form name="editprofile" method="post" action="<%=THIS_FILENAME %>">
    <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.ACTION %>' value='<%=EditHardTokenProfileJSPHelper.ACTION_EDIT_HARDTOKENPROFILE %>'>
    <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.HIDDEN_HARDTOKENPROFILENAME %>' value='<%=helper.profilename %>'>
    <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.HIDDEN_HARDTOKENTYPE %>' value='<%=helper.getProfileType() %>'>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right">         
         &nbsp;
      </td>
      <td width="50%">
         &nbsp;  
      </td>
    </tr>
    <% if(helper.profiledata instanceof SwedishEIDProfile){%>
         <%@ include file="swedisheidpage.jsp" %> 
    <% }
       if(helper.profiledata instanceof EnhancedEIDProfile){%>
         <%@ include file="enhancedeidpage.jsp" %> 
    <% }
       if(helper.profiledata instanceof IPINEnvelopeSettings){%>
         <%@ include file="pinenvelopepage.jsp" %> 
    <% }
       if(helper.profiledata instanceof IVisualLayoutSettings){%>
       <%@ include file="visuallayoutpage.jsp" %> 
    <% }      
       if(helper.profiledata instanceof IPINEnvelopeSettings){%>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("VISUALVALIDITY") %>(<%= ejbcawebbean.getText("DAYS") %>)
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditHardTokenProfileJSPHelper.TEXTFIELD_VISUALVALIDITY%>" size="5" maxlength="5" 
                value='<%= ((IPINEnvelopeSettings) helper.profiledata).getVisualValidity()%>'>                    
      </td>
    </tr>
    <% } %>     
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right">         
         &nbsp;
      </td>
      <td width="50%">
         &nbsp;  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right">         
         <%= ejbcawebbean.getText("GENERALSETTINGS") %>:
      </td>
      <td width="50%">
         &nbsp;  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
         <%= ejbcawebbean.getText("HARDTOKENSNPREFIX") %> 
      </td>
      <td width="50%">
        <input type="text" name="<%=EditHardTokenProfileJSPHelper.TEXTFIELD_SNPREFIX%>" size="6" maxlength="6" 
               value='<%= helper.profiledata.getHardTokenSNPrefix()%>'>
      </td>
    </tr>
    <!--<tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("NUMBEROFCOPIES") %> 
      </td>
      <td width="50%">
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_NUMOFTOKENCOPIES%>" size="1"  >
           <%   for(int i=1; i<5;i++){ %>
           <option  value="<%= i %>" 
              <% if(helper.profiledata.getNumberOfCopies() == i) out.write(" selected "); %>> 
              <%= i %>
           </option>
           <%   } %> 
        </select> &nbsp;<%= ejbcawebbean.getText("USEIDENTICALPINS") %> &nbsp;
          <input type="checkbox" name="<%= EditHardTokenProfileJSPHelper.CHECKBOX_USEIDENTICALPINS %>" value="<%=EditHardTokenProfileJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(helper.profiledata.getGenerateIdenticalPINForCopies())
                 out.write("CHECKED");
           %>> 
      </td>
    </tr> -->
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("EREASABLE") %> 
      </td>
      <td width="50%">
          <input type="checkbox" name="<%= EditHardTokenProfileJSPHelper.CHECKBOX_EREASBLE %>" value="<%=EditHardTokenProfileJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(helper.profiledata.getEreasableToken())
                 out.write("CHECKED");
           %>>  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="49%" valign="top">&nbsp;</td>
      <td width="51%" valign="top"> 
        <input type="submit" name="<%= EditHardTokenProfileJSPHelper.BUTTON_SAVE %>" onClick='return checkallfields()' value="<%= ejbcawebbean.getText("SAVE") %>">
        <input type="submit" name="<%= EditHardTokenProfileJSPHelper.BUTTON_CANCEL %>" value="<%= ejbcawebbean.getText("CANCEL") %>">
      </td>
    </tr>
  </table>
 </form>