package asia.live_cast.translator.android.dto;

import java.io.Serializable;

public class TranslateDto implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String original;
	private String translated;
	private String languageFrom;
	private String languageTo;
	private String dt;
	private boolean success;
	
	public TranslateDto() {
		setOriginal("");
		setTranslated("");
		setLanguageFrom("");
		setLanguageTo("");
		setDt("");
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getOriginal() {
		return original;
	}

	public void setTranslated(String translated) {
		this.translated = translated;
	}

	public String getTranslated() {
		return translated;
	}

	public void setLanguageFrom(String languageFrom) {
		this.languageFrom = languageFrom;
	}

	public String getLanguageFrom() {
		return languageFrom;
	}

	public void setLanguageTo(String languageTo) {
		this.languageTo = languageTo;
	}

	public String getLanguageTo() {
		return languageTo;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public String getDt() {
		return dt;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}
}
