package asia.live_cast.translator.android.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Handler;
import asia.live_cast.translator.android.dto.TranslateDto;

public class TranslateThreadSecond extends Thread {
	private final Handler handler;
	private final Runnable runnable;
	private final HttpClient client;
	private final static String G_TRANSLATE_API_URL = "https://www.googleapis.com/language/translate/v2";
	private final static String G_TRANSLATE_API_KEY = "Your own api key.";
	private TranslateDto dto;
	
	public TranslateThreadSecond(Handler handler, Runnable runnable, TranslateDto dto) {
		this.handler = handler;
		this.runnable = runnable;
		this.dto = dto;
		this.client = new DefaultHttpClient();
	}
	
	private String Translate(String q, String source, String target) {
		String translated = "";
		
		HttpGet get = new HttpGet(
				G_TRANSLATE_API_URL + 
				"?q=" + Uri.encode(q) + 
				"&key=" + G_TRANSLATE_API_KEY + 
				"&source=" + source + 
				"&target=" + target);
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
				
				translated = builder.toString();
			}
		} catch (ClientProtocolException e) {
			dto.setSuccess(false);
		} catch (IOException e) {
			dto.setSuccess(false);
		}
		
		return translated;
	}
	
	@Override
	public void run() {
		if (dto != null) {
			String translated = Translate(dto.getOriginal(), dto.getLanguageFrom(), dto.getLanguageTo());
			
			try {
				JSONObject json = new JSONObject(translated);
				JSONArray translations = null;
				
				if (json.has("data")) {
					if (json.getJSONObject("data").has("translations")) {
						translations = json.getJSONObject("data").getJSONArray("translations");
					}
				}
				
				if (translations != null) {
					int len = translations.length();
					if (len > 0) {
						if (len == 1) {
							// 翻訳結果が1件しかない場合
							dto.setTranslated(translations.getJSONObject(0).getString("translatedText"));
							dto.setDt(new Date().toLocaleString());
							dto.setSuccess(true);
						}
						else {
							// 翻訳結果が複数ある場合
		    				final AlertDialog.Builder builder = new AlertDialog.Builder((Context)runnable);
		    				final CharSequence[] labels = new CharSequence[len];
		    				
		    				for (int i = 0; i < len; i++) {
		    					labels[i] = (CharSequence)translations.getJSONObject(i).get("translatedText");
		    				}
		    				
		    				builder.setItems(labels, new OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dto.setTranslated((String)labels[which]);
									dto.setDt(new Date().toLocaleString());
									dto.setSuccess(true);
								}
		    					
		    				});
						}
					}
					else {
						dto.setSuccess(false);
					}
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
