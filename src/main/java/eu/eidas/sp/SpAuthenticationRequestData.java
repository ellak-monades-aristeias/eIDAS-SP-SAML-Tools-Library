package eu.eidas.sp;

public class SpAuthenticationRequestData 
{
	String saml, id;
	
	public SpAuthenticationRequestData(String saml, String id)
	{
		this.saml = saml;
		this.id = id;
	}
	
	public String getSaml()
	{
		return saml;
	}
	
	public String getID()
	{
		return id;
	}
}
