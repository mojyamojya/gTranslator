package asia.live_cast.translator.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
 
//import com.facebook.android.DialogError;
//import com.facebook.android.Facebook;
//import com.facebook.android.FacebookError;
//import com.facebook.android.Facebook.DialogListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import net.gimite.jatts.JapaneseTextToSpeech;

//import com.admob.android.ads.AdManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
//import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import asia.live_cast.translator.android.dto.TranslateDto;
import asia.live_cast.translator.android.managers.HistoryManager;
import asia.live_cast.translator.android.managers.LocationManager;
import asia.live_cast.translator.android.managers.TTSManager;
import asia.live_cast.translator.android.models.ConfigModel;
import asia.live_cast.translator.android.models.LanguageModel;
import asia.live_cast.translator.android.models.LocationModel;
import asia.live_cast.translator.android.schemas.ITranslate;
import asia.live_cast.translator.android.services.gTranslateService;
//import asia.live_cast.translator.android.threads.ReverseGeocodeThread;
import asia.live_cast.translator.android.threads.BingTranslateThread;
//import asia.live_cast.translator.android.threads.TranslateThread;
//import asia.live_cast.translator.android.threads.TranslateThreadSecond;
import asia.live_cast.translator.android.views.ConfigActivity;
//import asia.live_cast.translator.android.views.FacebookActivity;
import asia.live_cast.translator.android.views.FacebookActivity;
import asia.live_cast.translator.android.views.HistoryActivity;
import asia.live_cast.translator.android.views.TweetActivity;

public class gTranslator extends Activity implements OnInitListener, Runnable, OnSharedPreferenceChangeListener {
    private ArrayAdapter<LanguageModel> languages;
    private TranslateDto tdto;
    private ConfigModel cmodel;
    private LocationModel lmodel;
    private TextToSpeech tts;
    private JapaneseTextToSpeech jatts;
    private boolean recordable;
    private boolean playable;
    private boolean playable_ja;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 8180;
    private static final int TTS_DATA_CHECK_REQUEST_CODE = 8181;
	private gTranslateService service;
	private TTSManager tmanager;
	private LocationManager lmanager;
    private ProgressDialog dialog;
    private String extraLanguage;
    private boolean binded;
    private Intent tintent;
    private AdView adView;
    
    private static final int MENU_ID_MENU1 = Menu.FIRST + 1;
    private static final int MENU_ID_MENU2 = Menu.FIRST + 2;
    private static final int MENU_ID_MENU3 = Menu.FIRST + 3;
    private static final int MENU_ID_MENU4 = Menu.FIRST + 4;

	private ServiceConnection connection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((gTranslateService.gTranslateBinder)binder).getService();
	    	service.setClipboard(
	    			((ClipboardManager)(getApplicationContext().getSystemService(CLIPBOARD_SERVICE))).getText().toString());
	    	binded = true;
		}
		
		public void onServiceDisconnected(ComponentName name) {
			service = null;
	    	binded = false;
		}
		
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_RING, 100, 0);
       
        // タイトルバーのカスタマイズ（ActionBar風）
        setTitle();
        
        // 初期化処理
        initDto();
    	initModel();
    	initTTS();
        initControl();
        
        if (cmodel.isClipmode()) {
        	initService();
        }
        
        adView = new AdView(this, AdSize.BANNER, "Your own admob publisher id.");
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        layout.addView(adView);
        adView.loadAd(new AdRequest());
        
        initLocationStatus();
        
//		AdManager.setTestDevices( new String[] {                  
//			AdManager.TEST_EMULATOR,
//			"C4F8A52CBC9F6F3834BDD4A96D639FCA", 
//		});
    }
    
	private void setTitle() {
		// タイトルバーのカスタマイズ（ActionBar風）
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_title);
		TextView title = (TextView)findViewById(R.id.main_title);
		title.setText(getText(R.string.app_name));
		
		// 位置情報取得ボタン
		ImageView location = (ImageView)findViewById(R.id.main_location);
		location.setImageResource(android.R.drawable.ic_menu_mylocation);
		location.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				if (lmanager != null) {
					setAddress(getString(R.string.translate_location_getting_message));
					
					if (!lmanager.requestLocationUpdates()) {
						Toast.makeText(
								gTranslator.this,
								getText(R.string.location_provider_error_messae), 
								Toast.LENGTH_LONG).show();
						setAddress(getString(R.string.location_failed_message));
					}
					
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(gTranslator.this);
					
					Editor editor = preferences.edit();
					editor.putBoolean("translate_location", true);
					editor.commit();
				}
			}
			
		});
		
		// 履歴ボタン
    	ImageView history = (ImageView)findViewById(R.id.main_history);
    	history.setImageResource(android.R.drawable.ic_menu_recent_history);
    	history.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 履歴画面（Activity）を開く
	        	Intent history = new Intent(gTranslator.this, HistoryActivity.class);
	        	history.setAction(Intent.ACTION_VIEW);
	        	startActivity(history);
			}
    		
    	});
    	
    	// 設定ボタン
    	ImageView config = (ImageView)findViewById(R.id.main_config);
    	config.setImageResource(android.R.drawable.ic_menu_preferences);
    	config.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 設定画面（Activity）を開く
	        	Intent config = new Intent(gTranslator.this, ConfigActivity.class);
	        	config.setAction(Intent.ACTION_VIEW);
	        	startActivity(config);
			}
    		
    	});
	}
	
    private void initDto() {
    	Intent intent = getIntent();
    	
    	if (intent != null) {
    		tdto = (TranslateDto)intent.getSerializableExtra("TranslateDto");
    	}
    }
    
    private void initModel() {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	preferences.registerOnSharedPreferenceChangeListener(this);
    	
    	if (cmodel == null) {
    		cmodel = new ConfigModel();
    		setConfigModel(preferences);
    	}
    	
    	if (lmodel == null) {
    		lmodel = new LocationModel();
    	}
    }
    
    private void setConfigModel(SharedPreferences preferences) {
    	cmodel.setConfirm(preferences.getBoolean("confirm", true));
		cmodel.setSrcLanguage(preferences.getString("list_src_language", "en"));
		cmodel.setDstLanguage(preferences.getString("list_dst_language", "ja"));
		cmodel.setStore(preferences.getBoolean("store", true));
		cmodel.setClipmode(preferences.getBoolean("clipmode", false));
		cmodel.setTransEngine(preferences.getString("list_translation_engines", "0"));
		cmodel.setTranslationLocation(preferences.getBoolean("translate_location", false));
		cmodel.setTranslationLocationConfirm(preferences.getBoolean("translate_location_confirm", true));
		cmodel.setRecognition(preferences.getBoolean("voice_recognition", false));
		cmodel.setVoiceLanguage(preferences.getString("list_voice_recognition", "ja"));
		cmodel.setTweet(preferences.getBoolean("tweet", false));
		cmodel.setAccount(preferences.getString("twitter_account", ""));
		cmodel.setPassword(preferences.getString("twitter_password", ""));
		cmodel.setOAuthToken(preferences.getString("oauth_token", ""));
		cmodel.setOAuthTokenSecret(preferences.getString("oauth_token_secret", ""));
		cmodel.setTweetLocation(preferences.getBoolean("tweet_location", false));
		cmodel.setTweetLocationConfirm(preferences.getBoolean("tweet_location_confirm", true));
    }
    
    private void initTTS() {
    	// TextToSpeechの初期化
    	try {
    		Intent intent = new Intent();
    		intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    		startActivityForResult(intent, TTS_DATA_CHECK_REQUEST_CODE);
    	}
    	catch (Exception ex) {
    		playable = false;
    	}
    	
    	if (jatts == null) {
        	try {
        		jatts = new JapaneseTextToSpeech(getApplicationContext(), this);
         	   	playable_ja = true;
    		}
    		catch (Exception ex) {
         	   	playable_ja = false;
    		}
    	}
    }
    
    private void initControl() {
    	// 画面コントロールを初期化
    	initTexts();
    	initSpinners();
    	initButtons();
    }
    
    private void initTexts() {
    	// テキストボックスの初期化
    	EditText txtOriginal = (EditText)findViewById(R.id.srctext);
    	EditText txtTranslated = (EditText)findViewById(R.id.dsttext);
    	
    	Drawable icon = getResources().getDrawable(R.drawable.clear);
    	icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    	txtOriginal.setCompoundDrawables(null, null, icon, null);
    	txtTranslated.setCompoundDrawables(null, null, icon, null);
    	
    	OnTouchListener tlistener = new OnTouchListener() {
    		
    		public boolean onTouch(View v, MotionEvent event) {
    			boolean result = false;
    			
    			EditText text = (EditText)v;
    			Drawable icon = text.getCompoundDrawables()[2];
    			
    			if (icon != null) {
	    			if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    				if (event.getX() >=
	    					(text.getMeasuredWidth() - text.getPaddingRight() - icon.getIntrinsicWidth()) &&
	    					event.getY() >=
	    					(text.getMeasuredHeight() - icon.getIntrinsicHeight()) / 2 &&
	    					event.getY() <=
	    					(text.getMeasuredHeight() + icon.getIntrinsicHeight()) / 2) {
	    					text.setText("");
	    					
	    					if (tdto != null) {
	    	    	    		tdto.setOriginal("");
	    	    	    		tdto.setTranslated("");
	    	    	    		
		    	    	    	// 翻訳結果再生ボタン
		    	    	    	ImageButton btnPlay = (ImageButton)findViewById(R.id.play);
		    	    	    	btnPlay.setEnabled(false);
		    	    	    	
//		    	    	    	// SMS送信ボタン
//		    	    	    	ImageButton btnSMS = (ImageButton)findViewById(R.id.sms);
//		    	    	    	btnSMS.setEnabled(false);
//		    	    	    	
//		    	    	    	// メール送信ボタン
//		    	    	    	ImageButton btnMailto = (ImageButton)findViewById(R.id.mailto);
//		    	    	    	btnMailto.setEnabled(false);

		    	    	    	// 送信ボタン
		    	    	    	ImageButton btnSend = (ImageButton)findViewById(R.id.send);
		    	    	    	btnSend.setEnabled(false);
		    	    	    	
		    	    	    	// facebookボタン
		    	    	    	ImageButton btnFacebook = (ImageButton)findViewById(R.id.facebook);
		    	    	    	btnFacebook.setEnabled(false);
		    	    	    	
		    	    	    	// Tweetボタン
		    	    	    	ImageButton btnTweet = (ImageButton)findViewById(R.id.tweet);
		    	    	    	btnTweet.setEnabled(false);
	    					}
	    					
	    					result = true;
	    				}
	    			}
    			}
    			
    			return result;
    		}

    	};
    	
    	txtOriginal.setOnTouchListener(tlistener);
    	txtTranslated.setOnTouchListener(tlistener);
    	
    	if (tdto != null) {
	    	txtOriginal.setText(tdto.getOriginal()); 
	    	txtTranslated.setText(tdto.getTranslated());
    	}
    }
    
    private void initSpinners() {
    	// コンボボックスの初期化
    	Spinner spSrc = (Spinner)findViewById(R.id.srclanguage);
    	Spinner spDst = (Spinner)findViewById(R.id.dstlanguage);
    	String[] labels = getResources().getStringArray(R.array.language_label);
    	String[] values = getResources().getStringArray(R.array.language_data);
    	languages = new ArrayAdapter<LanguageModel>(this, android.R.layout.simple_spinner_item);
        languages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        for (int i = 0; i < labels.length; i++) {
        	languages.add(new LanguageModel(labels[i], values[i]));
        }
        
    	spSrc.setAdapter(languages);
    	spDst.setAdapter(languages);
    	
    	if (tdto != null) {
	    	setSelectedItem(spSrc, tdto.getLanguageFrom());
	    	setSelectedItem(spDst, tdto.getLanguageTo());
    	}
    	else {
	    	setSelectedItem(spSrc, cmodel.getSrcLanguage());
	    	setSelectedItem(spDst, cmodel.getDstLanguage());
    	}
    }
    
    private void setSelectedItem(Spinner spinner, String value) {
    	int i;
    	int len = languages.getCount();
    	if (value.equals("zh")) {
    		value = "zh-CN";
    	}
    	for (i = 0; i < len; i++) {
    		if (((LanguageModel)languages.getItem(i)).getValue().equals(value)) {
    			break;
    		}
        }
    	spinner.setSelection(i);
    }
    
    private void initButtons() {
    	// 音声認識の初期化
    	try {
    		PackageManager manager = getPackageManager();
    		List<ResolveInfo> activities = manager.queryIntentActivities(
    			new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    		
        	if (activities.size() != 0) {
    			recordable = true;
    		} else {
    			recordable = false;
    		}
    	}
    	catch (Exception ex) {
    		recordable = false;
    	}
    	
    	// 位置情報更新ボタン
//    	ImageView btnLocation = (ImageView)findViewById(R.id.location);
//    	btnLocation.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View view) {
//				if (lmanager != null) {
//					setAddress(getString(R.string.translate_location_getting_message));
//					
//					if (!lmanager.requestLocationUpdates()) {
//						Toast.makeText(
//								gTranslator.this,
//								getText(R.string.location_provider_error_messae), 
//								Toast.LENGTH_LONG).show();
//						setAddress(getString(R.string.location_failed_message));
//					}
//					
//					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(gTranslator.this);
//					
//					Editor editor = preferences.edit();
//					editor.putBoolean("translate_location", true);
//					editor.commit();
//				}
//			}
//    		
//    	});
    	
        // 翻訳方向ボタン
        ToggleButton btnVector = (ToggleButton)findViewById(R.id.vector);
        btnVector.setTextOff(">>");
        btnVector.setTextOn("<<");
        btnVector.setBackgroundResource(android.R.drawable.btn_default);
    	
    	// 音声認識ボタン
    	ImageButton btnRec = (ImageButton)findViewById(R.id.rec);
//    	btnRec.setImageResource(R.drawable.voice);
    	btnRec.setImageResource(android.R.drawable.ic_btn_speak_now);
    	btnRec.setEnabled(recordable);
    	
    	// 翻訳結果再生ボタン
    	ImageButton btnPlay = (ImageButton)findViewById(R.id.play);
//    	btnPlay.setImageResource(R.drawable.tts);
    	btnPlay.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
    	
//    	// SMS送信ボタン
//    	ImageButton btnSMS = (ImageButton)findViewById(R.id.sms);
//    	btnSMS.setImageResource(R.drawable.sms);
//    	
//    	// メール送信ボタン
//    	ImageButton btnMailto = (ImageButton)findViewById(R.id.mailto);
//    	btnMailto.setImageResource(R.drawable.mail);
    	
    	// 送信ボタン
    	ImageButton btnSend = (ImageButton)findViewById(R.id.send);
    	btnSend.setImageResource(R.drawable.ic_menu_send);
    	
    	//　facebookボタン
    	ImageButton btnFacebook = (ImageButton)findViewById(R.id.facebook);
    	btnFacebook.setImageResource(R.drawable.f_logo);
    	
    	// Tweetボタン
    	ImageButton btnTweet = (ImageButton)findViewById(R.id.tweet);
    	btnTweet.setImageResource(R.drawable.tweet);
    	
    	// 翻訳ボタン
    	ImageButton btnTrans = (ImageButton)findViewById(R.id.translate);
    	btnTrans.setImageResource(R.drawable.translate);
    	
		if (tdto == null || tdto.getTranslated().equals("")) {
	    	btnPlay.setEnabled(false);
//	    	btnSMS.setEnabled(false);
//	    	btnMailto.setEnabled(false);
	    	btnSend.setEnabled(false);
	    	btnFacebook.setEnabled(false);
    		btnTweet.setEnabled(false);
    	}
		else {
	    	btnPlay.setEnabled(playable || playable_ja);
		}
		
    	// 音声認識可能な場合
    	if (recordable) {
    		btnRec.setOnClickListener(new OnClickListener() {
    			// 音声認識ボタンクリック時の処理
    			public void onClick(View v) {
    				if (!cmodel.isRecognition()) {
	    				final AlertDialog.Builder builder = new AlertDialog.Builder(gTranslator.this);
	    		    	final CharSequence[] labels = getResources().getStringArray(R.array.language_label);
	    				
	    				builder.setTitle(getText(R.string.translate_lang_dialog));
	    				builder.setItems(labels, new DialogInterface.OnClickListener() {
	    		    		
							public void onClick(DialogInterface dialog, int which) {
								showVoiceRecognitionDialog(languages.getItem(which));
							}
	    		    	});
	    				
	    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
	    					
	    				});
	    				
	    				builder.setCancelable(true);
	    				builder.create().show();
    				}
    				else {
						showVoiceRecognitionDialog(new LanguageModel("", cmodel.getVoiceLanguage()));
    				}
    			}
	    	});
    	}
    	
    	// TextToSpeech可能な場合
		btnPlay.setOnClickListener(new OnClickListener() {
			// 翻訳結果再生ボタンクリック時の処理
			public void onClick(View v) {
				if (tdto != null) {
    				if (!tdto.getTranslated().equals("")) {
    					if (tts != null &&
    						tts.isSpeaking()) {
    						tts.stop();
    					}
    					else if (jatts.isSpeaking()) {
    						jatts.stop();
    					}
    					else {
    						if (tmanager != null) {
    							if (tmanager.isSpeaking()) {
    								tmanager.stop();
    							}
    						}
    					}
    					
    					if (tts != null &&
    						tts.isLanguageAvailable(getLocale(tdto.getLanguageTo())) == TextToSpeech.LANG_AVAILABLE) {
    						// AndroidのTextToSpeechエンジンが利用できる言語の場合
	    					tts.setLanguage(getLocale(tdto.getLanguageTo()));
	    					tts.speak(tdto.getTranslated(), TextToSpeech.QUEUE_FLUSH, null);
    					}
    					else {
    						// AndroidのTextToSpeechエンジンが利用できない言語場合
    						if (tdto.getLanguageTo().equals("ja")) {
    							// 日本語の場合
    							if (playable_ja) {
    								// JapaneseTextToSpeechが利用できる場合は利用する（精度が高いから）
    	    						HashMap<String, String> params = new HashMap<String, String>();
    	    						params.put(JapaneseTextToSpeech.KEY_PARAM_SPEAKER, "female01");
    		    					jatts.speak(tdto.getTranslated(), TextToSpeech.QUEUE_FLUSH, params);
    		    					return;
    							}
    						}
    						// 日本語以外、または、JapaneseTextToSpeechが利用できない場合
    						// Bing translateを利用する
	    					if (tmanager == null) {
	    						tmanager = new TTSManager(gTranslator.this);
	    					}
							tmanager.speak(tdto.getTranslated(), tdto.getLanguageTo());
    					}
    				}
				}
			}
		});
    	
		btnSend.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (tdto != null) {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, tdto.getTranslated());
					startActivity(intent);
				}
			}
			
		});
		
//    	btnSMS.setOnClickListener(new OnClickListener() {
//    		// SMS送信ボタンクリック時の処理
//			public void onClick(View v) {
//    			if (tdto != null) {
//	    			if (!tdto.getTranslated().equals("")) {
//	    				try {
//		    				final Intent intent = new Intent();
//		    				intent.setAction(Intent.ACTION_VIEW);
//		    				intent.putExtra("sms_body", tdto.getTranslated());
//		    				intent.setType("vnd.android-dir/mms-sms");
//	    					startActivity(intent);
//	    				}
//	    				catch (ActivityNotFoundException ex) {
//	    					Toast.makeText(
//	    							gTranslator.this,
//	    							getText(R.string.translate_sms_failed_message), 
//	    							Toast.LENGTH_LONG).show();
//	    				}
//	    			}
//    			}
//			}
//    		
//    	});
//    	
//    	btnMailto.setOnClickListener(new OnClickListener() {
//    		// メール送信ボタンクリック時の処理
//    		public void onClick(View v) {
//    			if (tdto != null) {
//	    			if (!tdto.getTranslated().equals("")) {
//	    				final Intent intent = new Intent();
//	    				intent.setAction(Intent.ACTION_SENDTO);
//	    				intent.setData(Uri.parse("mailto:"));
//	    				intent.putExtra(Intent.EXTRA_TEXT, tdto.getTranslated());
//	    				startActivity(intent);
//	    			}
//    			}
//    		}
//    	});
    			
//    	final Facebook facebook = new Facebook("Your own developer's id");
    	
    	btnFacebook.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				if (tdto != null) {
	    			if (!tdto.getTranslated().equals("")) {
						final Intent intent = new Intent(gTranslator.this, FacebookActivity.class);
						intent.putExtra("Translated", tdto.getTranslated());
						intent.putExtra("ConfigModel", cmodel);
						intent.setAction(Intent.ACTION_VIEW);
						startActivity(intent);
//				        Bundle bundle = new Bundle();
//				        bundle.putString("message", tdto.getTranslated());
//				        
//				        facebook.dialog(gTranslator.this, "feed", bundle, new DialogListener() {
//		
//							public void onCancel() {
//								
//							}
//		
//							public void onComplete(Bundle values) {
//								if (values.getString("post_id") != null) {
//									Toast.makeText(
//											gTranslator.this, 
//											getText(R.string.facebook_posted_message), 
//											Toast.LENGTH_LONG).show();
//								}
//							}
//		
//							public void onError(DialogError e) {
//								Toast.makeText(
//										gTranslator.this, 
//										getText(R.string.facebook_error_message), 
//										Toast.LENGTH_LONG).show();
//							}
//		
//							public void onFacebookError(FacebookError e) {
//								Toast.makeText(
//										gTranslator.this, 
//										getText(R.string.facebook_error_message), 
//										Toast.LENGTH_LONG).show();
//							}
//				        	
//				        });
	    			}
				}
			}
    	});
    	
    	btnTweet.setOnClickListener(new OnClickListener() {
    		// Tweetボタンクリック時の処理
    		public void onClick(View v) {
    			if (tdto != null) {
	    			if (!tdto.getTranslated().equals("")) {
						final Intent intent = new Intent(gTranslator.this, TweetActivity.class);
						intent.putExtra("Translated", tdto.getTranslated());
						intent.putExtra("ConfigModel", cmodel);
						intent.setAction(Intent.ACTION_VIEW);
						startActivity(intent);
	    			}
    			}
    		}
    	});
    	
    	btnTrans.setOnClickListener(new OnClickListener() {
    		// 翻訳ボタンクリック時の処理
    		public void onClick(View v) {
    			EditText text;
    			Spinner spSrc;
    			Spinner spDst;
    			
    			ToggleButton vector = (ToggleButton)findViewById(R.id.vector);
    			
    			if (vector.isChecked()) {
    				text = (EditText)findViewById(R.id.dsttext);
    				spSrc = (Spinner)findViewById(R.id.dstlanguage);
    				spDst = (Spinner)findViewById(R.id.srclanguage);
    			}
    			else {
    				text = (EditText)findViewById(R.id.srctext);
    				spSrc = (Spinner)findViewById(R.id.srclanguage);
    				spDst = (Spinner)findViewById(R.id.dstlanguage);
    			}
    			
    			if (!text.getText().toString().equals("")) {
    				if (tdto == null) {
    					tdto = new TranslateDto();
    				}
    				tdto.setOriginal(text.getText().toString());
    				tdto.setLanguageFrom(((LanguageModel)spSrc.getSelectedItem()).getValue());
    				tdto.setLanguageTo(((LanguageModel)spDst.getSelectedItem()).getValue());
    				
    				ImageButton btnPlay = (ImageButton)findViewById(R.id.play);
			    	btnPlay.setEnabled(false);
			    	
    				Translate();
    			}
    		}
    	});
    }
    
    private void initLocationStatus() {
        // 位置情報取得のための準備
        if (lmanager == null) {
        	lmanager = new LocationManager(this, new GPSLocationListener(), new NetworkLocationListener());
        }
        
        if (cmodel.isTranslationLocationConfirm() && !cmodel.isTranslationLocation()) {
        	setAddress(getString(R.string.translate_location_getting_message));
        }
        else {
        	if (!cmodel.isTranslationLocation()) {
        		setAddress(getString(R.string.location_not_available_message));
        	}
        	else {
        		setAddress(getString(R.string.translate_location_getting_message));
        	}
        }
        
        // 確認メッセージ表示
        showLocationProgressDialog(
        		this, 
        		cmodel.isTranslationLocationConfirm(), 
        		cmodel.isTranslationLocation());
    }
    
	private void showLocationProgressDialog(Activity activity, boolean isLocationConfirm, boolean isLocation) {
		if (isLocationConfirm && !isLocation) {
			LayoutInflater inflater = LayoutInflater.from(gTranslator.this);
			final View view = inflater.inflate(R.layout.confirm, (ViewGroup)activity.findViewById(R.id.layout_confirm_root));
			final AlertDialog.Builder builder = new AlertDialog.Builder(gTranslator.this);
			
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
				public void onClick(DialogInterface dialog, int which) {
					if (!lmanager.requestLocationUpdates()) {
						Toast.makeText(
								gTranslator.this,
								getText(R.string.location_provider_error_messae), 
								Toast.LENGTH_LONG).show();
						setAddress(getString(R.string.location_failed_message));
					}
					setLocationConfig(
							view, 
							"translate_location", 
							"translate_location_confirm", 
							true);
				}
				
			});
			
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
				public void onClick(DialogInterface dialog, int which) {
					setLocationConfig(
							view, 
							"translate_location", 
							"translate_location_confirm", 
							false);
					
					setAddress(getString(R.string.location_not_available_message));
				}
			
			});
		
	    	builder.setTitle(getText(R.string.tweet_location_dialog_title));
	    	builder.setMessage(getText(R.string.tweet_location_dialog_message));
	    	builder.setView(view);
			builder.setCancelable(true);
	    	builder.show();
		}
		else {
			if (isLocation) {
				if (!lmanager.requestLocationUpdates()) {
					Toast.makeText(
							gTranslator.this,
							getText(R.string.location_provider_error_messae), 
							Toast.LENGTH_LONG).show();
					setAddress(getString(R.string.location_failed_message));
				}
			}
		}
	}

	private void setLocationConfig(View view, String locationKey, String locationConfirmKey, boolean isLocation) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(gTranslator.this);
		
		boolean checked = ((CheckBox)view.findViewById(R.id.confirm)).isChecked();
		
		Editor editor = preferences.edit();
		if (checked) {
			editor.putBoolean(locationKey, isLocation);
		}
		editor.putBoolean(locationConfirmKey, !checked);
		editor.commit();
	}

	private void showProgressDialog(int id) {
    	if (dialog == null) {
    		dialog = new ProgressDialog(gTranslator.this);
    		dialog.setIndeterminate(true);
    	}
		dialog.setMessage(getText(id));
    	dialog.show();
    }
    
    private void showVoiceRecognitionDialog(LanguageModel locale) {
    	extraLanguage = getLocale(locale);
    	
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "gTranslator");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, extraLanguage);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    
    private void initService() {
    	if (tintent == null) {
    		tintent = new Intent(this, gTranslateService.class);
    	}
		tintent.putExtra("ConfigModel", cmodel);
		startService(tintent);
        
        if (!binded) {
        	bindService(tintent, connection, Context.BIND_AUTO_CREATE);
        }
    }
    
    private void Translate() {
    	showProgressDialog(R.string.translate_progress_message);
    	
//    	String mode = cmodel.getTransEngine();
    	
		new BingTranslateThread(new Handler(), this, tdto).start();
//    	if (mode.equals("0")) {
//    		// Google Translate API v1
//    		new TranslateThread(new Handler(), this, tdto).start();
//    	}
//    	else {
//    		new BingTranslateThread(new Handler(), this, tdto).start();
//    	}
//    	else if (mode.equals("1")) {
//    		// Google Translate API v2
//    		new TranslateThreadSecond(new Handler(), this, tdto).start();
//    	}
//    	else if (mode.equals("2")) {
//    		// Microsoft Bing Translate
//    		new BingTranslateThread(new Handler(), this, tdto).start();
//    	}
    }
    
    // TextToSpeech用
    private Locale getLocale(String value) {
    	Locale locale;
    	
    	if (value.equals("en")) {
    		locale = Locale.ENGLISH;
    	}
    	else if (value.equals("ja")) {
    		locale = Locale.JAPANESE;
    	}
    	else if (value.equals("zh-CN")) {
    		// 簡体字中国語（Simplified Chinese）
    		locale = Locale.SIMPLIFIED_CHINESE;
    	}
    	else if (value.equals("zh-TW")) {
    		// 繁体字中国語（Traditional Chinese）
    		locale = Locale.TRADITIONAL_CHINESE;
    	}
    	else if (value.equals("zh")) {
    		locale = Locale.CHINESE;
    	}
    	else if (value.equals("it")) {
    		locale = Locale.ITALIAN;
    	}
    	else if (value.equals("es")) {
    		locale = new Locale("spa");
    	}
    	else if (value.equals("fr")) {
    		locale = Locale.FRENCH;
    	}
    	else if (value.equals("de")) {
    		locale = Locale.GERMAN;
    	}
    	else if (value.equals("ko")) {
    		locale = Locale.KOREAN;
    	}
    	else if (value.equals("ru")) {
    		locale = new Locale("ru");
    	}
    	else {
    		locale = new Locale("OTHER");
    	}
    	
    	return locale;
    }
    
    // 音声認識用
    private String getLocale(LanguageModel model) {
    	String locale;
    	
    	if (model.getValue().equals("en")) {
    		locale = Locale.ENGLISH.toString();
    	}
    	else if (model.getValue().equals("ja")) {
    		locale = Locale.JAPAN.toString();
    	}
    	else if (model.getValue().equals("zh-CN")) {
    		// 簡体字中国語（Simplified Chinese）
    		locale = Locale.PRC.toString();
    	}
    	else if (model.getValue().equals("zh-TW")) {
    		// 繁体字中国語（Traditional Chinese）
    		locale = Locale.TAIWAN.toString();
    	}
    	else if (model.getValue().equals("zh")) {
    		locale = Locale.CHINA.toString();
    	}
    	else if (model.getValue().equals("it")) {
    		locale = Locale.ITALY.toString();
    	}
    	else if (model.getValue().equals("es")) {
    		locale = new Locale("spa", "ESP").toString();
    	}
    	else if (model.getValue().equals("fr")) {
    		locale = Locale.FRANCE.toString();
    	}
    	else if (model.getValue().equals("de")) {
    		locale = Locale.GERMAN.toString();
    	}
    	else if (model.getValue().equals("ko")) {
    		locale = Locale.KOREA.toString();
    	}
    	else if (model.getValue().equals("ru")) {
    		locale = new Locale("ru", "RUS").toString();
    	}
    	else {
    		locale = new Locale("OTHER").toString();
    	}
    	
    	return locale;
    }
    
    private String getLanguageValue(String locale) {
    	String value;
    	
		if (locale.equals(Locale.ENGLISH.toString())) {
    		value = "en";
    	}
    	else if (locale.equals(Locale.JAPAN.toString())) {
    		value = "ja";
    	}
    	else if (locale.equals(Locale.PRC.toString())) {
    		value = "zh-CN";
    	}
    	else if (locale.equals(Locale.TAIWAN.toString())) {
    		value = "zh-TW";
    	}
    	else if (locale.equals(Locale.CHINA.toString())) {
    		value = "zh";
    	}
    	else if (locale.equals(Locale.ITALY.toString())) {
    		value = "it";
    	}
    	else if (locale.equals(new Locale("spa", "ESP").toString())) {
    		value = "es";
    	}
    	else if (locale.equals(Locale.FRANCE.toString())) {
    		value = "fr";
    	}
    	else if (locale.equals(Locale.GERMAN.toString())) {
    		value = "de";
    	}
       	else if (locale.equals(Locale.KOREA.toString())) {
    		value = "ko";
    	}
    	else if (locale.equals(new Locale("ru", "RUS").toString())) {
    		value = "ru";
    	}
    	else {
    		value = "";
    	}
		
    	return value;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ID_MENU1, Menu.NONE, "History").setIcon(android.R.drawable.ic_menu_recent_history);
    	menu.add(Menu.NONE, MENU_ID_MENU2, Menu.NONE, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
    	menu.add(Menu.NONE, MENU_ID_MENU3, Menu.NONE, "Clipboard").setIcon(android.R.drawable.ic_menu_set_as);
    	menu.add(Menu.NONE, MENU_ID_MENU4, Menu.NONE, "Information").setIcon(android.R.drawable.ic_menu_info_details);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	HistoryManager manager = new HistoryManager(this);
    	
    	menu.getItem(0).setEnabled(manager.getHistoriesCount() > 0);
    	
    	if (tdto != null) {
    		menu.getItem(2).setEnabled(!tdto.getTranslated().equals(""));
    	}
    	else {
    		menu.getItem(2).setEnabled(false);
    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	boolean result = true;
    	
    	switch (item.getItemId()) {
    	case MENU_ID_MENU1:
        	Intent history = new Intent(this, HistoryActivity.class);
        	history.setAction(Intent.ACTION_VIEW);
        	startActivity(history);
    		break;
    	case MENU_ID_MENU2:
        	Intent config = new Intent(this, ConfigActivity.class);
        	config.setAction(Intent.ACTION_VIEW);
        	startActivity(config);
    		break;
    	case MENU_ID_MENU3:
    		if (service != null) {
    			if (tdto != null) {
    				service.setClipboard(tdto.getTranslated());
    			}
    		}
    		
			((ClipboardManager)(this.getSystemService(CLIPBOARD_SERVICE)))
				.setText(tdto.getTranslated());
			
			if (service != null) {
				service.setClipboard(tdto.getTranslated());
			}
			
			Toast.makeText(
					this,
					getText(R.string.translate_clipboard_message), 
					Toast.LENGTH_LONG).show();
    		break;
    	case MENU_ID_MENU4:
        	LayoutInflater inflater = LayoutInflater.from(this);
        	final View view = inflater.inflate(R.layout.info, (ViewGroup)findViewById(R.id.info_layout_root));
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					return;
				}
				
			});
			
			builder.setView(view);
			builder.show();
    		break;
    	default:
    		result = false;
    		break;
    	}
    	
    	return result;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_DATA_CHECK_REQUEST_CODE) {
        	// TextToSpeech初期化の結果
    		if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
    			// TextToSpeechが利用できる場合
    			if (tts == null) {
    				try {
    					tts = new TextToSpeech(getApplicationContext(), this);
    					playable = true;
    				}
    				catch (Exception ex) {
    					playable = false;
    				}
    			}
    		}
    		else {
    			// TextToSpeechのリソースがインストールされていない場合
    			if (cmodel.isConfirm()) {
    				// 「次から確認しない」が選択されていない場合
	            	LayoutInflater inflater = LayoutInflater.from(this);
	    	    	final View view = inflater.inflate(R.layout.confirm, (ViewGroup)findViewById(R.id.layout_confirm_root));
			    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	
			    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
			    			try {
	    	    		    	Intent intent = new Intent();
	    		    			intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	    		    			startActivity(intent);
			    			}
			    			catch (Exception ex) {
			    				playable = false;
			    			}
						}
						
					});
			    	
			    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
					    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(gTranslator.this);
	
					    	Editor editor = preferences.edit();
							editor.putBoolean("confirm", !((CheckBox)view.findViewById(R.id.confirm)).isChecked());
							editor.commit();
							
							return;
						}
						
					});
					
			    	builder.setTitle(getText(R.string.install_voice_data_dialog_title));
			    	builder.setMessage(getText(R.string.install_voice_data_dialog_message));
			    	builder.setView(view);
					builder.setCancelable(true);
			    	builder.show();
    			}
    		}
        }
        else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
        	// 音声認識初期化の結果
    		if (resultCode == RESULT_OK) {
    			ArrayList<String> matches = data.getStringArrayListExtra(
    				RecognizerIntent.EXTRA_RESULTS);
    			
    			if (matches.size() > 0) {
    				if (matches.size() == 1) {
    					setVoiceRecognition(matches.get(0));
    				}
    				else {
	    				final AlertDialog.Builder builder = new AlertDialog.Builder(gTranslator.this);
	    		    	final CharSequence[] labels = matches.toArray(new CharSequence[matches.size()]);
	    				
	    				builder.setTitle(getText(R.string.translate_voice_recognition_dialog));
	    				builder.setItems(labels, new DialogInterface.OnClickListener() {
	    		    		
							public void onClick(DialogInterface dialog, int which) {
								setVoiceRecognition((String)labels[which]);
							}
	    		    	});
	    				
	    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
	    					
	    				});
	    				
	    				builder.setCancelable(true);
	    				builder.create().show();
    				}
	    		}
    		}
        }
    	
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void setVoiceRecognition(String recognition) {
		ToggleButton vector = (ToggleButton)findViewById(R.id.vector);
		String language = getLanguageValue(extraLanguage);
		
//		if (language.equals("ja") ||
//			language.equals("zh-CN") || 
//			language.equals("zh-TW") || 
//			language.equals("zh") || 
//			language.equals("ko")) {
//			recognition.replaceAll(" ", "");
//			recognition.replaceAll("　", "");
//		}
		
		if (vector.isChecked()) {
			((EditText)findViewById(R.id.dsttext)).setText(recognition);
			setSelectedItem((Spinner)findViewById(R.id.dstlanguage), language);
		}
		else {
			((EditText)findViewById(R.id.srctext)).setText(recognition); 
			setSelectedItem((Spinner)findViewById(R.id.srclanguage), language);
		}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (tts != null) {
    		tts.shutdown();
    	}
    	
    	if (binded) {
    		unbindService(connection);
        	binded = false;
    	}
    	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	preferences.unregisterOnSharedPreferenceChangeListener(this);
    }
    
    public void onInit(int status) {
    	ImageButton btnPlay = (ImageButton)findViewById(R.id.play);
    	
    	if (status == TextToSpeech.SUCCESS) {
    		btnPlay.setEnabled(true);
    	}
    	else {
    		btnPlay.setEnabled(false);
    	}
    	
    	if (tdto == null) {
    		btnPlay.setEnabled(false);
    	}
    	else {
    		btnPlay.setEnabled(!tdto.getTranslated().equals(""));
    	}
    }
    
    public void run() {
    	dialog.dismiss();
    	
		if (tdto.isSuccess()) {
			if (cmodel.isStore()) {
				ContentValues values = new ContentValues();
				values.put(ITranslate.ORIGINAL, tdto.getOriginal());
				values.put(ITranslate.TRANSLATED, tdto.getTranslated());
				values.put(ITranslate.LANGUAGE_FROM, tdto.getLanguageFrom());
				values.put(ITranslate.LANGUAGE_TO, tdto.getLanguageTo());
				values.put(ITranslate.LAT, lmodel.getLng());
				values.put(ITranslate.LNG, lmodel.getLng());
				values.put(ITranslate.DATE_TIME, tdto.getDt());
				getContentResolver().insert(Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider"), values);
			}
			
			ToggleButton vector = (ToggleButton)findViewById(R.id.vector);
			
			((EditText)findViewById((!vector.isChecked()) ? R.id.dsttext : R.id.srctext))
				.setText(tdto.getTranslated());
			
			// 音声認識ボタン
			if (recordable) {
				ImageButton btnRec = (ImageButton)findViewById(R.id.rec);
				btnRec.setEnabled(true);
			}
			
			// 翻訳結果再生ボタン
			ImageButton btnPlay = (ImageButton)findViewById(R.id.play);
			
			if (tts != null &&
				tts.isLanguageAvailable(getLocale(tdto.getLanguageTo())) == TextToSpeech.LANG_AVAILABLE) {
				// AndroidのTextToSpeechエンジンが利用できる言語の場合
				btnPlay.setEnabled(playable); 
			}
			else {
				// AndroidのTextToSpeechエンジンが利用できない言語の場合
				if (tdto.getLanguageTo().equals("ja")) {
					// 日本語の場合
					if (playable_ja) {
						// JapaneseTextToSpeechエンジンが利用できればそちら利用する（精度が高いから）
						btnPlay.setEnabled(true); 
					}
					else {
						btnPlay.setEnabled(false); 
					}
				}
				else {
					btnPlay.setEnabled(true); 
				}
			}
			
//			// SMS送信ボタン
//			ImageButton btnSMS = (ImageButton)findViewById(R.id.sms);
//			btnSMS.setEnabled(true);
//			
//			// メール送信ボタン
//			ImageButton btnMailto = (ImageButton)findViewById(R.id.mailto);
//			btnMailto.setEnabled(true);
			
			// 送信ボタン
			ImageButton btnSend = (ImageButton)findViewById(R.id.send);
			btnSend.setEnabled(true);
			
			// SMS送信ボタン
			ImageButton btnFacebook = (ImageButton)findViewById(R.id.facebook);
			btnFacebook.setEnabled(true);
			
			// Tweetボタン
			ImageButton btnTweet = (ImageButton)findViewById(R.id.tweet);
			btnTweet.setEnabled(true);
    	}
		else {
			Toast.makeText(
					this, 
					getText(R.string.translate_failed_message), 
					Toast.LENGTH_LONG).show();
		}
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		setConfigModel(sharedPreferences);
		
		if (key.equals("twitter_account") || key.equals("twitter_password")) {
			// twitterのアカウントとパスワード
			if (cmodel.isTweet()) {
				if (key.equals("twitter_account")) {
					// アカウント
					if (cmodel.getAccount().equals("")) {
						Toast.makeText(
								this,
								getText(R.string.conf_twitter_account_empty_message),
								Toast.LENGTH_LONG).show();
					}
				}
				else if (key.equals("twitter_password")) {
					// パスワード
					if (cmodel.getPassword().equals("")) {
						Toast.makeText(
								this,
								getText(R.string.conf_twitter_password_empty_message),
								Toast.LENGTH_LONG).show();
					}
				}
			}
			
    		Editor editor = sharedPreferences.edit();
    		editor.putString("oauth_token", "");
    		editor.putString("oauth_token_secret", "");
    		editor.commit();
    		
			cmodel.setOAuthToken(sharedPreferences.getString("oauth_token", ""));
			cmodel.setOAuthTokenSecret(sharedPreferences.getString("oauth_token_secret", ""));
		}
		else if (key.equals("list_src_language") || key.equals("list_dst_language")) {
			// 言語設定
			ToggleButton vector = (ToggleButton)findViewById(R.id.vector);
			
	    	Spinner spSrc = (Spinner)findViewById((!vector.isChecked()) ? R.id.srclanguage : R.id.dstlanguage);
	    	Spinner spDst = (Spinner)findViewById((!vector.isChecked()) ? R.id.dstlanguage : R.id.srclanguage);
	    	
	    	boolean change = false;
	    	
	    	if (tdto != null) {
	    		if (tdto.getTranslated().equals("")) {
	    			change = true;
	    		}
	    	}
	    	else {
	    		change = true;
	    	}
	    	
	    	if (change) {
		    	setSelectedItem(spSrc, cmodel.getSrcLanguage());
		    	setSelectedItem(spDst, cmodel.getDstLanguage());
	    	}
		}
		else if (key.equals("clipmode")) {
			// クリップボードモード
	        if (cmodel.isClipmode()) {
        		initService();
	        }
	        else {
				if (binded) {
					unbindService(connection);
					stopService(tintent);
					binded = false;
				}
	        }
		}
		else if (key.equals("tweet")) {
			// ツイート設定
			if (!cmodel.isTweet()) {
				cmodel.setAccount("");
				cmodel.setPassword("");
				
				Editor editor = sharedPreferences.edit();
				editor.putString("oauth_token", "");
				editor.putString("oauth_token_secret", "");
				editor.commit();
			}
		}
		
    	if (service != null) {
    		service.setConfig(cmodel);
    	}
	}
	
    public class GPSLocationListener implements LocationListener, Runnable {
		public void onLocationChanged(Location location) {
			lmodel.setLat(location.getLatitude());
			lmodel.setLng(location.getLongitude());
			
			AdRequest request = new AdRequest();
			adView.loadAd(request);
			
			lmanager.removeAllUpdates();

//			new ReverseGeocodeThread(new Handler(), gTranslator.this, lmodel).start();
			setAddress(getString(R.string.location_available_message));
		}

		public void onProviderDisabled(String provider) {
			lmanager.removeAllUpdates();

			Toast.makeText(
				gTranslator.this,
				getText(R.string.location_provider_error_messae), 
				Toast.LENGTH_LONG).show();
			
			setAddress(getString(R.string.location_failed_message));
		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				lmanager.removeAllUpdates();
				
				Toast.makeText(
					gTranslator.this,
					getText(R.string.location_failed_message), 
					Toast.LENGTH_LONG).show();

				setAddress(getString(R.string.location_failed_message));
			}
		}

		public void run() {
			lmanager.removeAllUpdates();
			setAddress(lmodel.getAddress());
		}
	}
    
	public class NetworkLocationListener implements LocationListener, Runnable {
		public void onLocationChanged(Location location) {
			lmodel.setLat(location.getLatitude());
			lmodel.setLng(location.getLongitude());
			
			AdRequest request = new AdRequest();
			adView.loadAd(request);
			
			lmanager.removeAllUpdates();

//			new ReverseGeocodeThread(new Handler(), gTranslator.this, lmodel).start();
			setAddress(getString(R.string.location_available_message));
		}

		public void onProviderDisabled(String provider) {
			lmanager.removeAllUpdates();
			
			Toast.makeText(
				gTranslator.this,
				getText(R.string.location_provider_error_messae), 
				Toast.LENGTH_LONG).show();
			
			setAddress(getString(R.string.location_failed_message));
		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				lmanager.removeAllUpdates();
				
				Toast.makeText(
						gTranslator.this,
						getText(R.string.location_failed_message), 
						Toast.LENGTH_LONG).show();

				setAddress(getString(R.string.location_failed_message));
			}
		}

		public void run() {
			lmanager.removeAllUpdates();
			setAddress(lmodel.getAddress());
		}
	}
	
	public void setAddress(String address) {
		TextView txtAddress = (TextView)findViewById(R.id.address);
		txtAddress.setText(address);
	}
}
