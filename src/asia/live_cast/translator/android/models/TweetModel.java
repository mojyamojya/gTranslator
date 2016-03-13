package asia.live_cast.translator.android.models;

public class TweetModel {
	private String status;
	private String account;
	private String password;
	private boolean success;
	private String lat;
	private String lng;
	private boolean authorize;
	
	public TweetModel() {
		status = "";
		account = "";
		password = "";
		success = false;
		lat = "";
		lng = "";
		authorize = true;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
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
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLat() {
		return lat;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLng() {
		return lng;
	}

	public boolean isAuthorized() {
		return authorize;
	}

	public void setAuthorized(boolean authorize) {
		this.authorize = authorize;
	}
}
