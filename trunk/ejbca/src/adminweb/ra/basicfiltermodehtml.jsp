
  <p><%= ejbcawebbean.getText("FINDENDENTITYWITHUSERNAME") %>
    <input type="text" name="<%=TEXTFIELD_USERNAME %>" size="40" maxlength="255" 
     <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_FINDUSER))
          out.write("value='"+oldactionvalue+"'"); %>
     >
    <input type="submit" name="<%=BUTTON_FIND %>" value="<%= ejbcawebbean.getText("FIND") %>">
  </p>
  <p><%= ejbcawebbean.getText("ORIFCERTIFICATSERIAL") %>
    <input type="text" name="<%=TEXTFIELD_SERIALNUMBER %>" size="33" maxlength="255" 
     <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_ISREVOKED))
          out.write("value='"+oldactionvalue+"'"); %>
     >
    <input type="submit" name="<%=BUTTON_ISREVOKED %>" value="<%= ejbcawebbean.getText("FIND") %>" 
           onclick='return checkfieldforhexadecimalnumbers("document.form.<%=TEXTFIELD_SERIALNUMBER %>","<%= ejbcawebbean.getText("ONLYHEXNUMBERS") %>")'>
  </p>
  <% if(globalconfiguration.getIssueHardwareTokens()){ %>
  <p><%= ejbcawebbean.getText("ORTOKENSERIAL") %>
    <input type="text" name="<%=TEXTFIELD_TOKENSERIALNUMBER %>" size="33" maxlength="255" 
     <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_FINDTOKEN))
          out.write("value='"+oldactionvalue+"'"); %>
     >
    <input type="submit" name="<%=BUTTON_FINDTOKEN %>" value="<%= ejbcawebbean.getText("FIND") %>" 
           onclick='return checkfieldforlegalchars("document.form.<%=TEXTFIELD_TOKENSERIALNUMBER %>","<%= ejbcawebbean.getText("ONLYLETTERSANDNUMBERS") %>")'>
  </p>
  <% } %>
  <p><%= ejbcawebbean.getText("ORWITHSTATUS") %>
    <select name="<%=SELECT_LIST_STATUS %>">
      <option value=''>--</option> 
      <option <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_LISTUSERS))
                   if(oldactionvalue.equals(Integer.toString(ALL_STATUS)))
                     out.write("selected"); %>
              value='<%= ALL_STATUS %>'><%= ejbcawebbean.getText("ALL") %></option>
      <% for(int i=0; i<availablestatuses.length; i++){ %>
      <option <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_LISTUSERS))
                   if(oldactionvalue.equals(Integer.toString(availablestatuses[i])))
                     out.write("selected"); %>
              value='<%= availablestatuses[i] %>'><%= ejbcawebbean.getText(availablestatustexts[i]) %></option>
      <% } %>
    </select>
    <input type="submit" name="<%=BUTTON_LIST %>" value="<%= ejbcawebbean.getText("LIST") %>">
  </p>
  <p><%= ejbcawebbean.getText("ORLISTEXPIRING") %>
    <input type="text" name="<%=TEXTFIELD_DAYS %>" size="3" maxlength="5" 
     <% if(oldaction != null && oldactionvalue!= null && oldaction.equals(OLD_ACTION_LISTEXPIRED))
          out.write("value='"+oldactionvalue+"'"); %>
     > <%= ejbcawebbean.getText("DAYS") %>
    <input type="submit" name="<%=BUTTON_LISTEXPIRED %>" value="<%= ejbcawebbean.getText("LIST") %>"
           onclick='return checkfieldfordecimalnumbers("document.form.<%=TEXTFIELD_DAYS %>","<%= ejbcawebbean.getText("ONLYDECNUMBERS") %>")'>
  </p>
