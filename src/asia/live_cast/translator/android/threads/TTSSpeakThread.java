package asia.live_cast.translator.android.threads;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import asia.live_cast.translator.android.managers.TTSManager.TTSState;
import asia.live_cast.translator.android.models.TTSModel;

public class TTSSpeakThread extends Thread {
	private final Context context;
	private final MediaPlayer player;
	private final TTSModel model;
	
	public TTSSpeakThread(Context context, MediaPlayer player, TTSModel model) {
		this.context = context;
		this.player = player;
		this.model = model;
	}
	
	@Override
	public void start() {
		model.setState(TTSState.SPEAKING);
		super.start();
	}
	
	@Override
	public void run() {
		if (player != null) {
			try {
				player.reset();
				player.setAudioStreamType(TextToSpeech.Engine.DEFAULT_STREAM);
				player.setDataSource(context.getFilesDir() + "/" + TTSModel.MEDIA_FILE_NAME);
				player.prepare();
				player.start();
			} catch (IllegalArgumentException e) {
				model.setState(TTSState.IDLE);
			} catch (IllegalStateException e) {
				model.setState(TTSState.IDLE);
			} catch (IOException e) {
				model.setState(TTSState.IDLE);
			}
		}
	}
}
