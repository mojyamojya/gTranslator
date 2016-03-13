package asia.live_cast.translator.android.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Xml;
import asia.live_cast.translator.android.dto.TranslateDto;

public class BingTranslateThread extends Thread {
	private Handler handler;
	private Runnable runnable;
	private HttpClient client;
	private final static String BING_TRANSLATE_API_URL = "http://api.microsofttranslator.com/V2/Http.svc/Translate";
	private final static String BING_TRANSLATE_API_APPID = "Your own api id.";
	private TranslateDto dto;
	
	public BingTranslateThread(Handler handler, Runnable runnable, TranslateDto dto) {
		this.handler = handler;
		this.runnable = runnable;
		this.dto = dto;
		this.client = new DefaultHttpClient();
	}
	
	private List<String> Translate(String text, String from, String to) {
		List<String> translations = new ArrayList<String>();
		
		HttpGet get = new HttpGet(
				BING_TRANSLATE_API_URL + 
				"?appId=" + BING_TRANSLATE_API_APPID +
				"&text=" + Uri.encode(text) +
				"&from=" + from +
				"&to=" + to + 
				"&contentType=text/plain");
		
		try {
			final HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final InputStream stream = response.getEntity().getContent();
				final XmlPullParser parser = Xml.newPullParser();
				parser.setInput(new InputStreamReader(stream));
				
				for (int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()) {
					if (e == XmlPullParser.TEXT) {
						translations.add(parser.getText());
					}
				}
				
				stream.close();
			}
		} catch (XmlPullParserException e) {
			dto.setSuccess(false);
		} catch (ClientProtocolException e) {
			dto.setSuccess(false);
		} catch (IOException e) {
			dto.setSuccess(false);
		}
		
		return translations;
	}
	
	@Override
	public void run() {
		if (dto != null) {
			List<String> translations = Translate(dto.getOriginal(), dto.getLanguageFrom(), dto.getLanguageTo());

			if (translations != null) {
				int len = translations.size();
				if (len > 0) {
					if (len == 1) {
						// 翻訳結果が1件しかない場合
						String translated = translations.get(0);
						
						if (!translations.equals("")) {
							dto.setTranslated(translated);
							dto.setDt(new Date().toLocaleString());
							dto.setSuccess(true);
						}
						else {
							dto.setSuccess(false);
						}
					}
					else {
						// 翻訳結果が複数ある場合
	    				final AlertDialog.Builder builder = new AlertDialog.Builder((Context)runnable);
	    				final CharSequence[] labels = new CharSequence[len];
	    				
	    				for (int i = 0; i < len; i++) {
	    					labels[i] = (CharSequence)translations.get(i);
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
		}
		
		handler.post(runnable);
	}
}
