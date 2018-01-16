package eu.eidas.sp;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

public class SpEidasSamlTools 
{
	static final Logger logger = LoggerFactory.getLogger(SpEidasSamlTools.class.getName());
	
	public static SpAuthenticationRequestData generateEIDASRequest(ArrayList<String> pal, String citizenCountry, String serviceProviderCountry, int qaaLvl)
	{
		System.out.println(Paths.get(".").toAbsolutePath().normalize().toString());
		Properties configs = SPUtil.loadSPConfigs();
        String spId = configs.getProperty(Constants.PROVIDER_NAME);
        String providerName = configs.getProperty(Constants.PROVIDER_NAME);
        
		String nodeUrl = configs.getProperty("country1.url");
		//ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine(Constants.SP_CONF);
		ProtocolEngineI protocolEngine = SpProtocolEngineFactory.getSpProtocolEngine(Constants.SP_CONF);
		
		EidasAuthenticationRequest.Builder reqBuilder = new EidasAuthenticationRequest.Builder();
		
		final ImmutableSortedSet<AttributeDefinition<?>> allSupportedAttributesSet = protocolEngine.getProtocolProcessor().getAllSupportedAttributes();
		List<AttributeDefinition<?>> reqAttrList = new ArrayList<AttributeDefinition<?>>(allSupportedAttributesSet);
        //remove attributes the SP decides not to send (for testing purpose)
        for (AttributeDefinition<?> attributeDefinition : allSupportedAttributesSet) 
        {
        	String attributeName = attributeDefinition.getNameUri().toASCIIString();
        	/*
        	attributeName = attributeName.substring(attributeName.lastIndexOf("/")+1, attributeName.length());
        	*/
        	System.out.println("Checking "+attributeName);
            if (!pal.contains(attributeName))
                reqAttrList.remove(attributeDefinition);
        }
        ImmutableAttributeMap reqAttrMap = new ImmutableAttributeMap.Builder().putAll(reqAttrList).build();
        reqBuilder.requestedAttributes(reqAttrMap);

		reqBuilder.destination(nodeUrl);
		reqBuilder.providerName(providerName);
		//int qaaLvl = Integer.parseInt(configs.getProperty(Constants.SP_QAALEVEL));
		reqBuilder.levelOfAssurance(LevelOfAssurance.LOW.stringValue());
		if (qaaLvl == 3) reqBuilder.levelOfAssurance(LevelOfAssurance.SUBSTANTIAL.stringValue());
		else if (qaaLvl == 4) reqBuilder.levelOfAssurance(LevelOfAssurance.HIGH.stringValue());

		reqBuilder.spType("public");
        reqBuilder.levelOfAssuranceComparison(LevelOfAssuranceComparison.fromString("minimum").stringValue());
        reqBuilder.nameIdFormat("urn:oasis:names:tc:saml:1.1:nameid-format:unspecified");
        reqBuilder.binding(EidasSamlBinding.EMPTY.getName());
        
        //reqBuilder.issuer(System.getenv().get("METADATA_URL"));
        reqBuilder.issuer(configs.getProperty(Constants.SP_METADATA_URL));
        //reqBuilder.serviceProviderCountryCode(configs.getProperty(Constants.SP_COUNTRY));
        reqBuilder.serviceProviderCountryCode(serviceProviderCountry);
        reqBuilder.citizenCountryCode(citizenCountry);
        //reqBuilder.originCountryCode(configs.getProperty(Constants.SP_COUNTRY));
        //reqBuilder.assertionConsumerServiceURL(configs.getProperty(Constants.SP_RETURN));
        IRequestMessage binaryRequestMessage = null;
        String ncName = null;
        try {
        	ncName = SAMLEngineUtils.generateNCName();
            reqBuilder.id(ncName);
            EidasAuthenticationRequest authnRequest = reqBuilder.build();
            binaryRequestMessage = protocolEngine.generateRequestMessage(authnRequest, configs.getProperty("country1.metadata.url"));
            
          //Store in session in order to validate them in the response
			//session.setAttribute(Constants.SAML_IN_RESPONSE_TO_SP, authnRequest.getId());
			//session.setAttribute(Constants.ISSUER_SP, authnRequest.getIssuer());
			System.out.println(">>>>>>>>>>>> "+authnRequest.getId() +"-"+authnRequest.getIssuer());
        } catch (EIDASSAMLEngineException e) {
            String errorMessage = "Could not generate token for Saml Request: "+e.getMessage();
            System.out.println(errorMessage);
        }
        catch (Exception e) {
            String errorMessage = "Could not generate token for Saml Request: "+e.getMessage();
            System.out.println(errorMessage);
            e.printStackTrace();
        }
        byte [] token = binaryRequestMessage.getMessageBytes();
        //String samlRequestXML = EidasStringUtil.toString(token);
        
        SpAuthenticationRequestData data = new SpAuthenticationRequestData(EidasStringUtil.encodeToBase64(token), binaryRequestMessage.getRequest().getId());
        return data;
	}
	
	public static SpAuthenticationResponseData processResponse(String SAMLResponse, String remoteHost)
	{
		ArrayList<String[]> pal = new ArrayList<String[]>();
		ApplicationSpecificServiceException exception = null;
		Properties configs = SPUtil.loadSPConfigs();

		String providerName = configs.getProperty(Constants.PROVIDER_NAME);
        String metadataUrl = configs.getProperty(Constants.SP_METADATA_URL);
        //String metadataUrl = System.getenv().get("METADATA_URL");
        byte[] decSamlToken = EidasStringUtil.decodeBytesFromBase64(SAMLResponse);
        String samlResponseXML = EidasStringUtil.toString(decSamlToken);
        System.out.println("-----samlResponseXML------");
        System.out.println(samlResponseXML);
        IAuthenticationResponse response = null;
        try 
        {
            SpProtocolEngineI engine = SpProtocolEngineFactory.getSpProtocolEngine("SP");
        	//validate SAML Token
            response = engine.unmarshallResponseAndValidate(decSamlToken, remoteHost, 0, 0,  metadataUrl);
            System.out.println("-----response------");
	        System.out.println(response);
	        
	        pal = eIDAS2PAL(response.getAttributes());
        } 
        /*catch (UnmarshallException e) 
        {
            exception =  new ApplicationSpecificServiceException("UnmarshallException", e.getMessage());
        } */
        catch (EIDASSAMLEngineException e) 
        {
            if (StringUtils.isEmpty(e.getErrorDetail())) 
            	exception =  new ApplicationSpecificServiceException("EIDASSAMLEngineException", e.getErrorMessage());
            else 
            	exception =  new ApplicationSpecificServiceException("Exception", e.getErrorDetail());
        }
        if ( exception!=null ) {
			logger.error(exception.getMessage());
			throw exception;
		}
        
        SpAuthenticationResponseData data = new SpAuthenticationResponseData(pal, response.getId(), response.getInResponseToId(), response.toString(), response.getStatusCode(), response.getStatusMessage(), response.getLevelOfAssurance(), response.getAudienceRestriction());
        return data;
	}
	
	public static String getNodeUrl()
	{
		Properties configs = SPUtil.loadSPConfigs();
		return configs.getProperty("country1.url");
	}
	
	private static ArrayList<String[]> eIDAS2PAL(ImmutableAttributeMap eidasPal)
	{
		ArrayList<String[]> pal = new ArrayList<String[]>();
    	ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> map = eidasPal.getAttributeMap();
    	for (AttributeDefinition<?> key : map.keySet())
    	{
    		ArrayList<String> pa = new ArrayList<String>();

    		String attrName = key.getNameUri().toString();
    		ImmutableList<? extends AttributeValue<?>> vals = map.get(key).asList();
    		System.out.println(attrName);
    		pa.add(attrName);
    		pa.add(""+key.isRequired());
    		for (AttributeValue<?> val: vals) pa.add(val.toString());

    		pal.add(pa.toArray(new String[0]));
    		System.out.print("Library\t");
    		for( String ss : pa.toArray(new String[0])) System.out.print(ss+"\t");
    		System.out.println();
    	}
    	return pal;
	}
}
