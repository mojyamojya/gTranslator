package asia.live_cast.translator.android.services;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.ClipboardManager;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.gTranslator;
import asia.live_cast.translator.android.dto.TranslateDto;
import asia.live_cast.translator.android.models.ConfigModel;
import asia.live_cast.translator.android.schemas.ITranslate;
import asia.live_cast.translator.android.threads.BingTranslateThread;
//import asia.live_cast.translator.android.threads.TranslateThread;
//import asia.live_cast.translator.android.threads.TranslateThreadSecond;
import asia.live_cast.translator.android.views.HistoryActivity;

public class gTranslateService extends Service implements Runnable {
	private ConfigModel cmodel;
	private TranslateDto tdto;
	private boolean watch;
	private String clipboard;
	private static final int REPEAT_INTERVAL = 1000;
	
	public class gTranslateBinder extends Binder {
		public gTranslateService getService() {
			return gTranslateService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		watch = false;
		return new gTranslateBinder();
	}
	
	public Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if (watch) {
				if (cmodel.isClipmode()) {
					watch = false;
					chkClipboard();
				}
			}
			
			handler.sendMessageDelayed(obtainMessage(), REPEAT_INTERVAL);
		}
		
	};

	@Override
	public void onCreate() {
		super.onCreate();
		handler.sendMessageDelayed(new Message(), REPEAT_INTERVAL);
	}
	
	private void Translate() {
		new BingTranslateThread(new Handler(), this, tdto).start();
//		String mode = cmodel.getTransEngine();
//		
//		if (mode.equals("0")) {
//			// Google Translate v1
//			new TranslateThread(new Handler(), gTranslateService.this, tdto).start();
//		}
//		else {
//    		new BingTranslateThread(new Handler(), this, tdto).start();
//		}
//		else if (mode.equals("1")) {
//			// Google Translate v2
//			new TranslateThreadSecond(new Handler(), gTranslateService.this, tdto).start();
//		}
//		else if (mode.equals("2")) {
//			// Microsoft Bing Translate
//    		new BingTranslateThread(new Handler(), this, tdto).start();
//		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		if (intent != null) {
			cmodel = (ConfigModel)intent.getSerializableExtra("ConfigModel");
		}
	}
	
	@Override
	public void onRebind(Intent intent) {
		if (intent != null) {
			cmodel = (ConfigModel)intent.getSerializableExtra("ConfigModel");
		}
		watch = false;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		watch = true;
		return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
	}
	
	public void run() {
		if (tdto.isSuccess()) {
			NotificationManager nmanager =
				(NotificationManager)(getApplicationContext().getSystemService(NOTIFICATION_SERVICE));
			
			PendingIntent sender;
			if (cmodel.isStore()) {
				sender = PendingIntent.getActivity(this, 0, new Intent(this, HistoryActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			else {
				Intent intent = new Intent(this, gTranslator.class);
				intent.putExtra("TranslateDto", tdto);
				sender = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			Notification notification = new Notification(R.drawable.translate, getText(R.string.app_name), System.currentTimeMillis());
			notification.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name), "translated!", sender);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nmanager.notify(R.string.app_name, notification);
			
			if (cmodel.isStore()) {
				ContentValues values = new ContentValues();
				values.put(ITranslate.ORIGINAL, tdto.getOriginal());
				values.put(ITranslate.TRANSLATED, tdto.getTranslated());
				values.put(ITranslate.LANGUAGE_FROM, tdto.getLanguageFrom());
				values.put(ITranslate.LANGUAGE_TO, tdto.getLanguageTo());
				values.put(ITranslate.LAT, "");
				values.put(ITranslate.LNG, "");
				values.put(ITranslate.DATE_TIME, tdto.getDt());
				getContentResolver().insert(Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider"), values);
			}
		}
		
		watch = true;
	}	
	
	public void setClipboard(String clipboard) {
		this.clipboard = clipboard;
	}
	
	private void chkClipboard() {
		String tmpstr =
			((ClipboardManager)(getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE)))
				.getText().toString();
		
		if (!tmpstr.equals("")) {
			if (!tmpstr.equals(clipboard)) {
				clipboard = tmpstr;
				
				if (tdto == null) {
					tdto = new TranslateDto();
				}
				tdto.setOriginal(clipboard);
				tdto.setLanguageFrom(cmodel.getSrcLanguage());
				tdto.setLanguageTo(cmodel.getDstLanguage());
				tdto.setDt(new Date().toLocaleString());
				
				Translate();
			}
		}
		
		watch = true;
	}
	
	public void setConfig(ConfigModel cmodel) {
		this.cmodel = cmodel;
	}
}
