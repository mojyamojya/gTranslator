package asia.live_cast.translator.android.threads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

import android.os.Bundle;
import android.os.Handler;
import asia.live_cast.translator.android.models.FacebookModel;

public class FacebookThread extends Thread {
	private final Handler handler;
	private final Runnable runnable;
	private final Facebook facebook;
	private final FacebookModel fmodel;
	
	public FacebookThread(Handler handler, Runnable runnable, Facebook facebook, FacebookModel fmodel) {
		this.handler = handler;
		this.runnable = runnable;
		this.facebook = facebook;
		this.fmodel = fmodel;
	}
	
	@Override
	public void run() {
		AsyncFacebookRunner runner = new AsyncFacebookRunner(facebook);
    	
		// 翻訳結果をウォールに投稿
		Bundle bundle = new Bundle();
    	bundle.putString("message", fmodel.getMessage());
    	runner.request("me/feed", bundle, "POST", new RequestListener() {

			public void onComplete(String response, Object state) {
				boolean success = true;
				
				try {
					JSONObject json = new JSONObject(response);
					
					if (json.has("error")) {
						success = false;
					}
				} catch (JSONException e) {
					success = false;
				}
				
				postRunnable(success);
			}

			public void onIOException(IOException e, Object state) {
				postRunnable(false);
			}

			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				postRunnable(false);
			}

			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				postRunnable(false);
			}

			public void onFacebookError(FacebookError e, Object state) {
				postRunnable(false);
			}

    		
    	}, null);
	}
	
	private void postRunnable(boolean success) {
		fmodel.setSuccess(success);
		handler.post(runnable);
	}
}
