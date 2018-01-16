# eIDAS SP SAML Tools Library
## Introduction
This project was developed in the context of (and for the needs of) the Greek eIDAS Node. It is comprised of a Java library, which provides funtionalities which simplify the connection of a Service Provider (SP) to the Greek eIDAS Node. It provides the necessary functions for creating eIDAS SAML Authentication Requests as well as parsing incoming eIDAS SAML Authentication Responses. In addition, it creates the mandatory metadata XML document to be published by each SP, along with signing, encrypting and decrypting the aforementioned documents and messages, as required by the eIDAS specifications.

It was developed by the "Information Management Lab (i4M Lab)", participant of the Atlantis Group (http://www.atlantis-group.gr/) of the University of the Aegean (https://www.aegean.gr/). 

## Project Purpose

The purpose of this project is to facilitate the development of SPs, by simplifing the developement of the necessary interaction of SPs with the Greek eIDAS Node. It is based on the sample code provided by CEF. By following the instructions below, a developing team can develop a new SP and connect it to the eIDAS infrustructure without the need to implement the necessary specifications regarding SP-eIDAS Node interactions, as dictated by CEF.

## Repository contents
  - src folder: contains the source code of the library
  - README.md: this file
  - pom.xml: the maven pom.xml file of the project 

## Setting up

### Setting up Java
Perform the following steps: 
If Oracle provided JVM is going to be used, then it is necessary to apply the JCE  Unlimited Strength Jurisdiction Policy Files, which contain no restriction on cryptographic strengths: 
a.  Download the Java Cryptography Extension (JCE) Unlimited Strength Policy  Files from Oracle: 
  - For Java 7: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
  - For Java 8: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
b.  Uncompress and extract the downloaded zip file (it contains README.txt and two jar files). 
c.  For the installation, please follow the instructions in the README.txt file. 

### Setting up Tomcat
Some already provided jars need to be added to the libraries of the Tomcat web-server. These jars may be found under AdditionalFiles directory in the binary for your application server, in the eIDAS release bundle. 
If you are using Tomcat 7: 
1.  Create a folder named shared in $TOMCAT_HOME
2.  Create a subfolder named  lib in $TOMCAT_HOME/shared.
3.  Edit the file $TOMCAT_HOME/conf/catalina.properties and change the property shared.loader so that it reads: 
shared.loader=${catalina.home}/shared/lib/*.jar 
4.  Copy the files below to the new shared/lib directory: 
xml-apis-1.4.01.jar 
resolver-2.9.1.jar 
serializer-2.7.2.jar 
xalan-2.7.2.jar 
endorsed/xercesImpl-2.11.0.jar 

If you are using Tomcat 8:  
1.  Copy the files below to the existing lib directory on the application server. 
xml-apis-1.4.01.jar 
resolver-2.9.1.jar 
serializer-2.7.2.jar (rename this file to serializer.jar) 
xalan-2.7.2.jar 
xercesImpl-2.11.0.jar

## Connecting using the eIDAS SP SAML Tools Library

### Deploying the Library 
The contents of the configEidas.zip file must be extracted and copied to a directory in the local file system. After deployment is complete, the following environmental variable needs to be set in the tomcat execution environment (either as OS/AS environment variable or command-line parameter): SP_CONFIG_REPOSITORY. The variable must point to the location of the file system where the contents of the configEidas.zip file were copied.

### Setting up the keystore 
The aforementioned directory contains the file eidasKeystore.jks, which must contain all the necessary certificates for secure and trusted communication with the eIDAS Node. The following steps need to be executed in order to prepare the keystore for operation.
1.	Change the keystore password (current password: “local-demo”)
2.	Obtain a certificate which identifies the SP. The certificate must satisfy the criteria described in the eIDAS - Cryptographic requirements for the Interoperability Framework document , regarding SAML signing certificates.
3.	Import the certificate in the keystore as a PrivateKeyEntry
4.	Provide the Greek eIDAS Node team with the public certificate of the SP, to be added to the Greek eIDAS Node list of trusted SPs.

### Configuring the eIDAS SP SAML Tools Library
The first step in configuring the eIDAS SP SAML Tools Library requires the modification of a few configuration files. These files are located in the root folder of the SPEIDASSAMLTools.jar. In each of the following configuration files, replace the existing entries with the following information:

SignModule_SP.xml

        <entry key="response.sign.assertions">true</entry>
        <entry key="keyStorePath"> eidasKeystore.jks</entry>
        <entry key="keyStorePassword">keystore_password</entry>
        <entry key="keyPassword">SP_certificate_password</entry>
        <entry key="issuer">SP_certificate_issuer</entry>
        <entry key="serialNumber">SP_certificate_serial_number</entry>
        <entry key="keyStoreType">JKS</entry>

        <entry key="metadata.keyStorePath"> eidasKeystore.jks</entry>
        <entry key="metadata.keyStorePassword">keystore_password</entry>
        <entry key="metadata.keyPassword">SP_certificate_password</entry>
        <entry key="metadata.issuer">SP_certificate_issuer</entry>
        <entry key="metadata.serialNumber">SP_certificate_serial_number</entry>
        <entry key="metadata.keyStoreType">JKS</entry>


EncryptModule_SP.xml

    <!-- Key Encryption algorithm -->
    <entry key="key.encryption.algorithm">http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p</entry>
    <entry key="keyStorePath"> eidasKeystore.jks</entry>
    <entry key="keyStorePassword">keystore_password</entry>
    <entry key="keyPassword">SP_certificate_password</entry>

    ...

    <!--  If not present then no decryption will be applied on response -->
    <!-- Certificate containing instance private key-->
    <entry key="responseDecryptionIssuer">SP_certificate_issuer</entry>
    <entry key="serialNumber">SP_certificate_serial_number</entry>
    <entry key="keyStoreType">JKS</entry>


sp.properties

    sp.return=URL of the return page (e.g.: http://84.205.248.180:80/ReturnPage.jsp). 
    This page receives and processes the authentication response data from the eIDAS Infrastructure.
    ….
    sp.metadata.url=URL of the metadata page (e.g.:http://84.205.248.180:80/metadata.jsp). This is the URL under which the already provided metadata.jsp page is located
    …..
    sp.qaalevel=#
    The level of Assurance required by this SP for the provided authentication data.
    1=Non-existent
    2=Low
    3=Substantial
    4=High

Now that the eIDAS SP SAML Tools Library is configured, it needs to be integrated with the SP. The next step in doing so is to store the Library JAR file in a folder located inside the CLASSPATH of the web-server, so that the web-server can locate the library at run-time.

### Integrating the eIDAS SP SAML Tools Library 
The final step in the integration procedure is the use of the eIDAS SP SAML Tools Library in order to be able to request and receive Authentication services. For this reason, the Library provides the following API classes and methods:

eu.eidas.sp.SpAuthenticationRequestData

Class containing the bundle of information fully describing a Authentication Request.

Provides the following methods:

    - String getSaml():
    Returns the encrypted, signed SAML document representing the request, to be sent to the eIDAS Node.
    Parameters: none
    Returns: the string containing the SAML document
    - String getID()
    Returns the ID representing this request. This information is mainly used to match the request with its corresponding response.
    Parameters: none
    Returns: the string representing the Request ID

eu.eidas.sp.SpAuthenticationResponseData

Class containing the bundle of information fully describing an Authentication Response.

Provides the following methods:

    -ArrayList<String[]> getAttributes()
    Returns a list with the requested authentication attributes and their values.
    Parameters: none
    Returns: the list of Attributes and their values. Each entry in the list represents a single Attribute and contains an array of Strings, which in turn contains 3 cells:
    Cell 0: Attribute name
    Cell 1: true/false. Depending on whether the attribute is mandatory or optional.
    Cell 2: Attribute value
    -String getID()
    Returns the ID representing this response.
    Parameters: none
    Returns: the string representing the Response ID
    -String getResponseToID()
    Returns the ID of the request that triggered this response. This information is mainly used to match the request with its corresponding response.
    Parameters: none
    Returns: the string representing the corresponding Request ID
    -String getResponseXML()
    Returns the entire decrypted, response data, in XML form.
    Parameters: none
    Returns: the string representing the XML representing the Response data.
    -String getStatusCode()
    Returns the Status Code of the response.
    Parameters: none
    Returns: the string representing the Status Code of the response.
    “urn:oasis:names:tc:SAML:2.0:status:Success”: Authentication success
    “urn:oasis:names:tc:SAML:2.0:status:Requester”: Error occurred
    “urn:oasis:names:tc:SAML:2.0:status:Responder”: IdP Authentication error
    -String getStatusMessage()
    Returns a Human readable message representing the status (and reason of) of the response.
    Parameters: none
    Returns: the string representing a Human readable message representing the status (and the reason for that status) of the response.
    -String getLevelOfAssurance()
    Returns the Level of Assurance (Low, Substantial, High) provided in this response.
    Parameters: none
    Returns: the string representing the LOA of this response.


eu.eidas.sp.SpEidasSamlTools

Class which provides the main Library methods for processing Authentication Requests and Responses.
Provides the following methods:

    -SpAuthenticationRequestData generateEIDASRequest(ArrayList<String> pal, String citizenCountry, String serviceProviderCountry)
	Constructs a new eIDAS SAML Authentication Request.
	Parameters: 
	pal: the list of eIDAS Attribute names to be requested
	citizenCountry: a String representing the Country Code of the citizen to be authenticated
	serviceProviderCountry: a String representing the Country Code the SP operated in. In this case, this value should always be “GR”.
	Returns: a SpAuthenticationRequestData object representing the constructed Request.
    -SpAuthenticationResponseData processResponse(String SAMLResponse, String remoteHost)
	Processes the Authentication Response.
	Parameters: 
	SAMLResponse: The String containing the eIDAS Node provided Authentication Response. The String is contained as a parameter in the HTTP request used by the eIDAS Node to redirect the response to the SP’s Return Page.
	remoteHost: the URL representing the host which provided the response. This is also contained as a parameter in the HTTP request. 
	Returns: a SpAuthenticationResponseData object representing the Response data.
    -String getNodeUrl()
	Provides the SP with the URL of the eIDAS Node. This is the URL the Authentication Request is sent to, via HTTP POST or GET.
	Parameters: None
	Returns: the URL of the eIDAS Node.
?

### Sample SP Integration code


metadata.jsp

    <%@ page language="java" contentType="application/xml; charset=US-ASCII" pageEncoding="US-ASCII"%>
    <%@ page import="eu.eidas.sp.metadata.GenerateMetadataAction" %>
    <%
       out.clear();
       out.println(new GenerateMetadataAction().generateMetadata().trim());
    %>

Login.jsp
    
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
     String citizenCountry = "GR";
     String serviceProviderCountry = "GR";

     ArrayList<String> pal = new ArrayList<String>();
     pal.add("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName");
     pal.add("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName");
     pal.add("http://eidas.europa.eu/attributes/naturalperson/DateOfBirth");
     pal.add("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier");
     SpAuthenticationRequestData data = SpEidasSamlTools.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry);
     samlToken = data.getSaml();
     nodeUrl = SpEidasSamlTools.getNodeUrl();
     out.println("Request ID: "+data.getID());
     %>
  
    <form name="redirectForm" method="post" action="<%=nodeUrl%>">
            <input type="hidden" name="SAMLRequest" value="<%=samlToken%>" />
            <input type="hidden" id="country" name="country" value="<%=citizenCountry%>" />
            <input type="submit" value="Authenticate" name="Login" />
    </form>
  
    </body>
    </html>


ReturnPage.jsp
    
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
    out.println("ReponseToID: "+data.getResponseToID());
    out.println("Status Code: "+data.getStatusCode());
    out.println("Status Message: "+data.getStatusMessage());
    for (int i = 0; i < pal.size(); i++) {
    	String[] t = pal.get(i);
	out.print(“Attribute Name: ”+t[0]);
	out.print(“Mandatory: ”+t[1]);
	out.print(“Attribute Value: ”+t[2]);
    }
    out.println("Response XML: "+data.getResponseXML());
    %>

    </body>
    </html>
