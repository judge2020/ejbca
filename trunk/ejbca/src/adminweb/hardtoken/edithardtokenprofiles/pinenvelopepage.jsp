<%               
  int[] envelopetypes = {IPINEnvelopeSettings.PINENVELOPETYPE_NONE, IPINEnvelopeSettings.PINENVELOPETYPE_GENERALENVELOBE};
  String[] envelopetypetexts = {"NONE","GENERALENVELOPE"};

  IPINEnvelopeSettings envprofile = (IPINEnvelopeSettings) helper.profiledata;
%>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
         &nbsp;
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
   </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("PINENVELOPESETTINGS") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("ENVELOPETYPE") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_ENVELOPETYPE%>" size="1"  >       
            <% int currentenvtype = envprofile.getPINEnvelopeType();
               for(int i=0; i < envelopetypes.length ; i ++){%>
              <option value="<%=envelopetypes[i]%>" <% if(envelopetypes[i] == currentenvtype) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(envelopetypetexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CURRENTTEMPLATE") %>
        </div>
      </td>
      <td width="50%" valign="top">          
         <% if(envprofile.getPINEnvelopeTemplateFilename() == null || envprofile.getPINEnvelopeTemplateFilename().equals("")){
              out.write("NONE");
            }else{
              out.write(envprofile.getPINEnvelopeTemplateFilename());
            }
         %> 
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("UPLOADTEMPLATE") %>
        </div>
      </td>
      <td width="50%" valign="top">          
        <input type="submit" name="<%= EditHardTokenProfileJSPHelper.BUTTON_UPLOADENVELOPETEMP %>" onClick='return checkallfields()' value="<%= ejbcawebbean.getText("UPLOADTEMPLATE") %>">
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("NUMOFPINENVCOPIES") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_NUMOFENVELOPECOPIES%>" size="1"  >
           <%   for(int i=1; i<5;i++){ %>
           <option  value="<%= i %>" 
              <% if(envprofile.getNumberOfPINEnvelopeCopies() == i) out.write(" selected "); %>> 
              <%= i %>
           </option>
           <%   } %> 
        </select>
      </td>
    </tr>
