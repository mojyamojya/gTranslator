package asia.live_cast.translator.android.threads;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.Handler;
import asia.live_cast.translator.android.managers.TTSManager.TTSState;
import asia.live_cast.translator.android.models.TTSModel;

public class TTSLoadThread extends Thread {
	private final Context context;
	private final Handler handler;
	private final Runnable runnable;
	private final HttpClient client;
	private TTSModel model;
	private FileOutputStream file;
	
	public TTSLoadThread(Context context, Handler handler, Runnable runnable, TTSModel model) {
		this.context = context;
		this.handler = handler;
		this.runnable = runnable;
		this.model = model;
		this.client = new DefaultHttpClient();
		
		try {
			this.context.deleteFile(TTSModel.MEDIA_FILE_NAME);
			this.file = context.openFileOutput(TTSModel.MEDIA_FILE_NAME, Context.MODE_WORLD_READABLE);
		} catch (FileNotFoundException e) {
			model.setDownloaded(false);
		}
	}
	
	@Override
	public void start() {
		model.setState(TTSState.LOADING);
		super.start();
	}
	
	@Override
	public void run() {
		String url = model.getURL();
		
		if (!url.equals("")) {
			model.setDownloaded(false);
			
			HttpGet get = new HttpGet(url);
			
			try {
				final HttpResponse response = client.execute(get);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					InputStream input = response.getEntity().getContent();
					
					int read;
					byte[] buffer = new byte[4096];
					
					while ((read = input.read(buffer)) > 0) {
						file.write(buffer, 0, read);
					}
					
					file.close();
					input.close();
					model.setDownloaded(true);
				}
			} catch (ClientProtocolException e) {
				model.setDownloaded(false);
			} catch (IOException e) {
				model.setDownloaded(false);
			}
		}
		else {
			model.setDownloaded(false);
		}
		
		handler.post(runnable);
	}
}
