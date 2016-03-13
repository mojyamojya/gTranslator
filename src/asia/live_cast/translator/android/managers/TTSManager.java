package asia.live_cast.translator.android.managers;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import asia.live_cast.translator.android.models.TTSModel;
import asia.live_cast.translator.android.threads.TTSSpeakThread;
import asia.live_cast.translator.android.threads.TTSLoadThread;

public class TTSManager implements Runnable {
	private final Context context;
	private final MediaPlayer player;
	private TTSModel model;
	private TTSLoadThread thread;
	
	public enum TTSState {
		IDLE,
		LOADING,
		SPEAKING
	}
	
	public TTSManager(Context context) {
		this.context = context;
		this.player = new MediaPlayer();
		
		this.player.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				model.setState(TTSState.IDLE);
			}
			
		});
		
		this.model = new TTSModel();
	}
	
	public synchronized void speak(String text, String language) {
		model.setText(text);
		model.setLanguage(language);
		
		thread = new TTSLoadThread(context, new Handler(), this, model);
		thread.start();
	}
	
	public boolean isSpeaking() {
		return model.getState() != TTSState.IDLE; 
	}

	public void run() {
		if (model.isDownloaded()) {
			new TTSSpeakThread(context, player, model).start();
		}
	}
	
	public void stop() {
		if (thread != null) {
			thread.interrupt();
		}
		if (player != null) {
			player.stop();
		}
	}
}
