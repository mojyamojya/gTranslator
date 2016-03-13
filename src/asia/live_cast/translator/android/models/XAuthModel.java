package asia.live_cast.translator.android.models;

public class XAuthModel {
	public static final String consumer_key = "Your own consumer key.";
	public static final String consumer_secret = "Your own consumer secret key.";
	private String oauth_token;
	private String oauth_token_secret;
	
	public XAuthModel() {
		oauth_token = "";
		oauth_token_secret = "";
	}
	
	public void setOAuthToken(String oauth_token) {
		this.oauth_token = oauth_token;
	}
	
	public String getOAuthToken() {
		return oauth_token;
	}
	
	public void setOAuthTokenSecret(String oauth_token_secret) {
		this.oauth_token_secret = oauth_token_secret;
	}
	
	public String getOAuthTokenSecret() {
		return oauth_token_secret;
	}
}
