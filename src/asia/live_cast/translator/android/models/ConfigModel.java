package asia.live_cast.translator.android.models;

import java.io.Serializable;

public class ConfigModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean confirm;
	private String srcLanguage;
	private String dstLanguage;
	private boolean store;
	private boolean clipmode;
	private String transEngine;
	private boolean recognition;
	private String voiceLanguage;
	private boolean tweet;
	private String account;
	private String password;
	private String oauthToken;
	private String oauthTokenSecret;
	private boolean tweetLocation;
	private boolean tweetLocationConfirm;
	private boolean translationLocation;
	private boolean translationLocationConfirm;

	public ConfigModel() {
		confirm = true;
		srcLanguage = "en";
		dstLanguage = "ja";
		store = true;
		clipmode = true;
		transEngine = "0";
		recognition = false;
		voiceLanguage = "ja";
		oauthToken = "";
		oauthTokenSecret = "";
		tweetLocation = false;
		tweetLocationConfirm = true;
	}

	public void setConfirm(boolean confirm) {
		this.confirm = confirm;
	}
	
	public boolean isConfirm() {
		return confirm;
	}
	
	public void setSrcLanguage(String srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	public String getSrcLanguage() {
		return srcLanguage;
	}

	public void setDstLanguage(String dstLanguage) {
		this.dstLanguage = dstLanguage;
	}

	public String getDstLanguage() {
		return dstLanguage;
	}

	public void setRecognition(boolean recognition) {
		this.recognition = recognition;
	}

	public boolean isRecognition() {
		return recognition;
	}

	public void setClipmode(boolean clipmode) {
		this.clipmode = clipmode;
	}

	public boolean isClipmode() {
		return clipmode;
	}

	public void setVoiceLanguage(String voiceLanguage) {
		this.voiceLanguage = voiceLanguage;
	}

	public String getVoiceLanguage() {
		return voiceLanguage;
	}

	public void setTweet(boolean tweet) {
		this.tweet = tweet;
	}

	public boolean isTweet() {
		return tweet;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccount() {
		return account;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	
	public void setOAuthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
	
	public String getOAuthToken() {
		return oauthToken;
	}
	
	public void setOAuthTokenSecret(String oauthTokenSecret) {
		this.oauthTokenSecret = oauthTokenSecret;
	}
	
	public String getOAuthTokenSecret() {
		return oauthTokenSecret;
	}

	public void setTweetLocation(boolean location) {
		this.tweetLocation = location;
	}

	public boolean isTweetLocation() {
		return tweetLocation;
	}

	public void setTweetLocationConfirm(boolean locationConfirm) {
		this.tweetLocationConfirm = locationConfirm;
	}

	public boolean isTweetLocationConfirm() {
		return tweetLocationConfirm;
	}

	public void setStore(boolean store) {
		this.store = store;
	}

	public boolean isStore() {
		return store;
	}

	public void setTransEngine(String transEngine) {
		this.transEngine = transEngine;
	}

	public String getTransEngine() {
		return transEngine;
	}

	public void setTranslationLocation(boolean translationLocation) {
		this.translationLocation = translationLocation;
	}

	public boolean isTranslationLocation() {
		return translationLocation;
	}

	public void setTranslationLocationConfirm(boolean translationLocationConfirm) {
		this.translationLocationConfirm = translationLocationConfirm;
	}

	public boolean isTranslationLocationConfirm() {
		return translationLocationConfirm;
	}
}
