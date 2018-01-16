<%@ page language="java" contentType="application/xml; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="eu.eidas.sp.metadata.GenerateMetadataAction" %>
<%@ page import="java.util.ArrayList" %>
<%
   out.clear();
   out.println(new GenerateMetadataAction().generateMetadata().trim());
%>
