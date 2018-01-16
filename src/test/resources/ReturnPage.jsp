<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>eIDAS SP Return</title>
</head>
<%@ page import="eu.eidas.sp.SpEidasSamlTools" %>
<%@ page import="eu.eidas.sp.SpAuthenticationResponseData" %>
<%@ page import="java.util.ArrayList" %>

<body>

<%
    out.clear();
	SpAuthenticationResponseData data = SpEidasSamlTools.processResponse(request.getParameter("SAMLResponse"), request.getRemoteHost());
	ArrayList<String []> pal = data.getAttributes();
	out.println("Reponse ID: "+data.getID());
	out.println("<br>");
	out.println("ReponseToID: "+data.getResponseToID());
	out.println("<br>");
	out.println("Status Code: "+data.getStatusCode());
	out.println("<br>");
	out.println("Status Message: "+data.getStatusMessage());
	out.println("<br>");
	
  	out.println("<table>");
  	out.println("<tr>");
    out.println("<th>");
	out.print("Attribute");
	out.println("</th>");
	out.println("<th>");
	out.print("Mandatory");
	out.println("</th>");
	out.println("<th>");
	out.print("Value");
	out.println("</th>");

	out.println("</tr>");
	for (int i = 0; i < pal.size(); i++) {
		String[] t = pal.get(i);
		out.println("<tr>");
		out.println("<td>");
		out.print(t[0]);
		out.println("</td>");
		out.println("<td>");
		out.print(t[1]);
		out.println("</td>");
		out.println("<td>");
		out.print(t[2]);
		out.println("</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	out.println("<br>");
	out.println("Response XML: "+data.getResponseXML());
%>


</body>
</html>

