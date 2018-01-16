<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>eIDAS SP Login</title>
</head>
<%@ page import="eu.eidas.sp.SpEidasSamlTools" %>
<%@ page import="eu.eidas.sp.SpAuthenticationRequestData" %>
<%@ page import="java.util.ArrayList" %>

<body>

 <% 
 String nodeUrl="";
 String samlToken = ""; 
 String citizenCountry = "CA";
 String serviceProviderCountry = "CA";
 //if(request.getParameterNames() != null && request.getParameter("submit") != null)
 //{
	 ArrayList<String> pal = new ArrayList<String>();
	 pal.add("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName");
	 pal.add("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName");
	 pal.add("http://eidas.europa.eu/attributes/naturalperson/DateOfBirth");
	 pal.add("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier");
	 nodeUrl = SpEidasSamlTools.getNodeUrl();
	 SpAuthenticationRequestData data = SpEidasSamlTools.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry);
	 samlToken = data.getSaml();
	 out.println("Request ID: "+data.getID());
 //}
 %>
  
<form name="redirectForm" method="post" action="<%=nodeUrl%>">
        <input type="hidden" name="SAMLRequest" value="<%=samlToken%>" />
        <input type="hidden" id="country" name="country" value="<%=citizenCountry%>" />
        <input type="submit" value="Authenticate" name="Login" />
</form>

  
</body>
</html>