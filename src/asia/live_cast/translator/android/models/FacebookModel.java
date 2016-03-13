package asia.live_cast.translator.android.models;

import android.content.SharedPreferences;

public class FacebookModel {
	private String message;
	private boolean success;
	private SharedPreferences preferences;
	
	public FacebookModel(SharedPreferences preferences) {
		this.preferences = preferences;
		setMessage("");
		setSuccess(false);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getToken() {
		return preferences.getString("token", null);
	}

	public void setToken(String token) {
		this.preferences.edit().putString("token", token).commit();
	}

	public long getExpires() {
		return preferences.getLong("expires", -1);
	}

	public void setExpires(long expires) {
		this.preferences.edit().putLong("expires", expires).commit();
	}
}
