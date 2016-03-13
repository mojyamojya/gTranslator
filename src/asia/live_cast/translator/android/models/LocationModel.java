package asia.live_cast.translator.android.models;

public class LocationModel {
	private double lat;
	private double lng;
	private String address;
	private boolean success;
	
	public LocationModel() {
		address = "";
		success = false;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public double getLat() {
		return lat;
	}
	
	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public double getLng() {
		return lng;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setSuccess(boolean reverseSuccess) {
		this.success = reverseSuccess;
	}

	public boolean isSuccess() {
		return success;
	}
}
