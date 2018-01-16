package eu.eidas.sp;

import java.util.ArrayList;

public class SpAuthenticationResponseData 
{
	ArrayList<String[]> pal;
	String id, responseToId, responseXML, statusCode, statusMessage, levelOfAssurance, audienceRestriction;
	
	public SpAuthenticationResponseData(ArrayList<String[]> pal, String id, String responseToId, String responseXML, String statusCode, String statusMessage, String levelOfAssurance, String audienceRestriction)
	{
		this.pal = pal;
		this.id = id;
		this.responseToId = responseToId;
		this.responseXML = responseXML;
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.levelOfAssurance = levelOfAssurance;
		this.audienceRestriction = audienceRestriction;
	}
	
	public ArrayList<String[]> getAttributes()
	{
		return pal;
	}
	
	public String getID()
	{
		return id;
	}
	
	public String getResponseToID()
	{
		return responseToId;
	}

	public String getResponseXML()
	{
		return responseXML;
	}
	
	public String getStatusCode()
	{
		return statusCode;
	}
	
	public String getStatusMessage()
	{
		return statusMessage;
	}
	
	public String getLevelOfAssurance()
	{
		return levelOfAssurance;
	}
	
	public String getAudienceRestriction()
	{
		return audienceRestriction;
	}
}
