package asia.live_cast.translator.android.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import asia.live_cast.translator.android.models.LocationModel;

public class ReverseGeocodeThread extends Thread {
	private final Handler handler;
	private final Runnable runnable;
	private final HttpClient client;
	private final String G_GEOCODING_API_URL = "http://maps.googleapis.com/maps/api/geocode/json";
	private LocationModel model;
	
	public ReverseGeocodeThread(Handler handler, Runnable runnable, LocationModel model) {
		this.handler = handler;
		this.runnable = runnable;
		this.model = model;
		this.client = new DefaultHttpClient();
	}
	
	public String Reverse(double lat, double lng) {
		String result = "";
		
		HttpGet get = new HttpGet(
				G_GEOCODING_API_URL +
				"?latlng=" + lat + "," + lng +
				"&sensor=true");
		
		try {
			final HttpResponse response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				
				String line;
				StringBuilder builder = new StringBuilder();
				
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				
				stream.close();
				
				result = builder.toString();
			}
		} catch (ClientProtocolException e) {
			model.setSuccess(false);
		} catch (IOException e) {
			model.setSuccess(false);
		}
		
		return result;
	}
	
	@Override
	public void run() {
		if (model != null) {
			String result = Reverse(model.getLat(), model.getLng());
			
			try {
				JSONObject json = new JSONObject(result);
				JSONArray results = null;

				if (json.has("results")) {
					results = json.getJSONArray("results");
				}
				
				model.setAddress(results.getJSONObject(0).getString("formatted_address"));
				model.setSuccess(true);
				
			} catch (JSONException e) {
				model.setSuccess(false);
			}
			
		}
		
		handler.post(runnable);
	}
}
