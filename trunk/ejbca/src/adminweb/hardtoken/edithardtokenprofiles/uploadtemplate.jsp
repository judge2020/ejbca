
<% 
  String[] headlines = {"UPLOADENVELOPETEMP","UPLOADVISUALTEMP"};  
  String[]  actions     = { EditHardTokenProfileJSPHelper.ACTION_UPLOADENVELOPETEMP, EditHardTokenProfileJSPHelper.ACTION_UPLOADVISUALTEMP};

  int row = 0;
%>
<body > 
<SCRIPT language="JavaScript">
<!--  

function check()
{  
  
  if(document.uploadfile.<%= EditHardTokenProfileJSPHelper.FILE_TEMPLATE %>.value == ''){   
     alert("<%= ejbcawebbean.getText("YOUMUSTSELECT") %>"); 
   }else{  
     return true;  
   }
  
   return false;
}
-->
</SCRIPT>
<div align="center">   
   <h2><%= ejbcawebbean.getText(headlines[helper.uploadmode]) %><br></h2>
   <h3><%= ejbcawebbean.getText("PROFILE")+ " : " + helper.profilename %> </h3>
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
   <!--     <A  onclick='displayHelpWindow("<%= ejbcawebbean.getHelpfileInfix("hardtoken_help.html") + "#cas"%>")'>
        <u><%= ejbcawebbean.getText("HELP") %></u> </A></div> -->
      </td>
    </tr>
    <form name="uploadfile" action="<%= THIS_FILENAME %>" method="post" enctype='multipart/form-data' >
      <input type="hidden" name='<%= EditHardTokenProfileJSPHelper.ACTION %>' value='<%=actions[helper.uploadmode] %>'>            
    <tr  id="Row<%=row++%2%>"> 
      <td width="49%" valign="top" align="right"><%= ejbcawebbean.getText("PATHTOTEMPLATE") %></td>
      <td width="51%" valign="top">     
        <input TYPE="FILE" NAME="<%= EditHardTokenProfileJSPHelper.FILE_TEMPLATE %>">            
        <input type="submit" name="<%= EditHardTokenProfileJSPHelper.BUTTON_UPLOADFILE %>" onClick='return check()' value="<%= ejbcawebbean.getText("UPLOADTEMPLATE") %>" ><br><br>
        <input type="submit" name="<%= EditHardTokenProfileJSPHelper.BUTTON_CANCEL %>" value="<%= ejbcawebbean.getText("CANCEL") %>">     
      </td>
    </tr>
    </form>
  </table>