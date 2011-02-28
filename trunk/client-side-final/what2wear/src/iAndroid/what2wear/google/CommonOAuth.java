package	iAndroid.what2wear.google;

import java.net.URLEncoder;

public class CommonOAuth {

	/**
	 * This class holds the constants for the Google sign-in process
	 */
	public static class OAuth {
		final protected static String   Contacts_Photos			= "ContactsPhotosFile";
		final protected static String	APP_NAME				= "what2wear";
		final public static String	CONSUMER_KEY			= "what-2-wear.appspot.com";
		final public static String	CONSUMER_SECRET			= "PWjCZ9z2AiRtp1V4iDfy3lFU";
		final protected static String	CALLBACK_SCHEME			= "x-what-2-wear";
		final protected static String	CALLBACK_URL			= CALLBACK_SCHEME + "://callback";
		final protected static String	SCOPE					= "https://www.google.com/m8/feeds/";
		final protected static String	GET_REQUEST_TOKEN_URL	= "https://www.google.com/accounts/OAuthGetRequestToken?scope=" + URLEncoder.encode(SCOPE)
																		+ "&xoauth_displayname=" + URLEncoder.encode(APP_NAME);
		final protected static String	GET_ACCESS_TOKEN_URL	= "https://www.google.com/accounts/OAuthGetAccessToken?scope=" + URLEncoder.encode(SCOPE);
		final protected static String	TOKEN_AUTHORIZATION_URL	= "https://www.google.com/accounts/OAuthAuthorizeToken?btmpl=mobile&oauth_version=1.0";
		final public static String   ALL_CONTACTS_REQUEST = "https://www.google.com/m8/feeds/contacts/default/full?max-results=2500"; 
	}
}
