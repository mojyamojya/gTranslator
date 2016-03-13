package asia.live_cast.translator.android.models;

import android.net.Uri;
import asia.live_cast.translator.android.managers.TTSManager.TTSState;

public class TTSModel {
	private String text;
	private String language;
	private TTSState state;
	private boolean downloaded;
	private final static String G_TRANSLATE_BING_TRANSLATE_API_URL = "http://api.microsofttranslator.com/V2/Http.svc/Speak";
	private final static String G_TRANSLATE_BING_TRANSLATE_API_APPID = "Your own apiid.";
	public final static String MEDIA_FILE_NAME = "gTranslator.wav";
	
	public TTSModel() {
		this.text = "";
		this.language = "";
		this.state = TTSState.IDLE;
		this.downloaded = false;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setLanguage(String language) {
		if (language.equals("zh-CN")) {
			this.language = "zh-CHS";
		}
		else if (language.equals("zh-TW")) {
			this.language = "zh-CHT";
		}
		else {
			this.language = language;
		}
	}
	
	public String getLanguage() {
		return language;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setState(TTSState state) {
		this.state = state;
	}

	public TTSState getState() {
		return state;
	}

	public String getURL() {
		String url ="";
		
		if (!text.equals("") && !language.equals("")) {
			url = G_TRANSLATE_BING_TRANSLATE_API_URL + 
			"?appid=" + G_TRANSLATE_BING_TRANSLATE_API_APPID +
			"&text=" + Uri.encode(text) +
			"&language=" + language;
		}
		
		return url;
	}
}
