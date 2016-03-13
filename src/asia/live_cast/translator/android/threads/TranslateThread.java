package asia.live_cast.translator.android.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import asia.live_cast.translator.android.dto.TranslateDto;

public class TranslateThread extends Thread {
	private final Handler handler;
	private final Runnable runnable;
	private final HttpClient client;
	private final HttpPost post;
	private final static String G_TRANSLATE_API_URL = "http://ajax.googleapis.com/ajax/services/language/translate";
	private final static String G_TRANSLATE_API_VERSION = "1.0";
	private TranslateDto dto;
	
	public TranslateThread(Handler handler, Runnable runnable, TranslateDto dto) {
		this.handler = handler;
		this.runnable = runnable;
		this.dto = dto;
		this.client = new DefaultHttpClient();
		this.post = new HttpPost(G_TRANSLATE_API_URL);
	}
	
	private String Translate(String q, String languageFrom, String languageTo) {
		String translated = "";
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(4);
		params.add(new BasicNameValuePair("v", G_TRANSLATE_API_VERSION));
		params.add(new BasicNameValuePair("q", q));
		params.add(new BasicNameValuePair("format", "text"));
		params.add(new BasicNameValuePair("langpair", languageFrom + "|" + languageTo));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			final HttpResponse response = client.execute(post);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				
				String line;
				StringBuilder builder = new StringBuilder();
				
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				
				stream.close();
				
				translated = builder.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return translated;
	}
	
	@Override
	public void run() {
		if (dto != null) {
			String translated = Translate(
					dto.getOriginal(),
					dto.getLanguageFrom(),
					dto.getLanguageTo());
	
			try {
				JSONObject json = new JSONObject(translated);
				
				if (json.has("responseStatus") && json.getInt("responseStatus") == 200) {
					dto.setTranslated(json.getJSONObject("responseData").getString("translatedText"));
					dto.setDt(new Date().toLocaleString());
					dto.setSuccess(true);
				}
				else {
					dto.setSuccess(false);
				}
			} catch (JSONException e) {
				dto.setSuccess(false);
			}
		}
		
		handler.post(runnable);
	}
}
