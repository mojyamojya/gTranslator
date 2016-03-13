package asia.live_cast.translator.android.views;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.managers.LocationManager;
import asia.live_cast.translator.android.models.ConfigModel;
import asia.live_cast.translator.android.models.TweetModel;
import asia.live_cast.translator.android.models.XAuthModel;
import asia.live_cast.translator.android.threads.TweetThread;

public class TweetActivity extends Activity implements Runnable {
    private XAuthModel xmodel;
    private TweetModel tmodel;
    private ConfigModel cmodel;
    private ProgressDialog dialog;
    private LocationManager manager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.tweet);
        
        // タイトルバーのカスタマイズ（ActionBar風）
        setTitle();
        
        Intent intent = getIntent();
        
		// 初期化処理
        initModel(intent);
        initText(intent);
        initButtons();
        
        AdView adView = new AdView(this, AdSize.BANNER, "Your own admob publisher id.");
        LinearLayout layout = (LinearLayout)findViewById(R.id.tweetLayout);
        layout.addView(adView);
        adView.loadAd(new AdRequest());

        // 緯度経度取得処理
        initLocationStatus();
//        showLocationProgressDialog();
	}
	
//	@Override
//	protected void onPause() {
//		if (manager != null) {
//			manager.removeUpdates(glistener);
//			manager.removeUpdates(nlistener);
//		}
//		super.onPause();
//	}
	
	private void setTitle() {
		// タイトルバーのカスタマイズ（ActionBar風）
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.twitter_title);
		TextView title = (TextView)findViewById(R.id.twit_title);
		title.setText(getText(R.string.tweet_header_title));
		
		// アイコン
		ImageView icon = (ImageView)findViewById(R.id.twit_icon);
		icon.setImageResource(R.drawable.tweet);
		
		// 履歴ボタン
    	ImageView history = (ImageView)findViewById(R.id.twit_history);
    	history.setImageResource(android.R.drawable.ic_menu_recent_history);
    	history.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 履歴画面（Activity）を開く
	        	Intent history = new Intent(TweetActivity.this, HistoryActivity.class);
	        	history.setAction(Intent.ACTION_VIEW);
	        	startActivity(history);
			}
    		
    	});
    	
    	// 設定ボタン
    	ImageView config = (ImageView)findViewById(R.id.twit_config);
    	config.setImageResource(android.R.drawable.ic_menu_preferences);
    	config.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 設定画面（Activity）を開く
	        	Intent config = new Intent(TweetActivity.this, ConfigActivity.class);
	        	config.setAction(Intent.ACTION_VIEW);
	        	startActivity(config);
			}
    		
    	});
	}
	
	private void initModel(Intent intent) {
    	if (xmodel == null) {
    		xmodel = new XAuthModel();
    	}
    	if (tmodel == null) {
    		tmodel = new TweetModel();
    	}
    	if (intent != null) {
    		cmodel = (ConfigModel)intent.getSerializableExtra("ConfigModel");
    	}
	}
	
	private void initText(Intent intent) {
    	String translated = "";
    	
    	if (intent != null) {
    		translated = (intent.getStringExtra("Translated") + " #gTranslator");
    	}
    	
		final EditText txtStatus = (EditText)findViewById(R.id.status);
		final TextView lblCounter = (TextView)findViewById(R.id.counter);
		final Button btnTweet = (Button)findViewById(R.id.tweet);
		
    	if (!translated.equals("")) {
    		txtStatus.setText(translated);
    		
    		if (translated.length() > 140) {
    			lblCounter.setTextColor(Color.RED);
    			btnTweet.setEnabled(false);
    		}
    		
    		lblCounter.setText(String.valueOf(140 - translated.length()));
    	}
    	
    	txtStatus.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (txtStatus.getText().length() > 140) {
					lblCounter.setTextColor(Color.RED);
	    			btnTweet.setEnabled(false);
				}
				else {
					lblCounter.setTextColor(Color.rgb(124, 13, 13));
	    			btnTweet.setEnabled(true);
				}
				
				lblCounter.setText(String.valueOf(140 - txtStatus.getText().length()));
			}
    		
    	});
	}
	
	private void initButtons() {
		Button btnTweet = (Button)findViewById(R.id.tweet);
		Button btnCancel = (Button)findViewById(R.id.cancel);
		
		btnTweet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EditText txtStatus = (EditText)findViewById(R.id.status);
				
				if (!txtStatus.getText().equals("")) {
					tmodel.setStatus(txtStatus.getText().toString());
					
					if (cmodel.isTweet()) {
						setTwitterInfo(
								cmodel.getOAuthToken(),
								cmodel.getOAuthTokenSecret(),
								cmodel.getAccount(),
								cmodel.getPassword());
						
    					if (xmodel.getOAuthToken().equals("") || xmodel.getOAuthTokenSecret().equals("")) {
							if (tmodel.getAccount().equals("") || tmodel.getPassword().equals("")) {
								// ちゃんと入力されていない場合、再入力させる
	    						showSigninDialog();
	    						return;
							}
    					}
					}
					else {
						setTwitterInfo("", "", "", "");
						showSigninDialog();
						return;
					}
					
					Tweet();
				}
			}
			
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
	}
    
    private void initLocationStatus() {
        // 位置情報取得のための準備
        if (manager == null) {
        	manager = new LocationManager(this, new GPSLocationListener(), new NetworkLocationListener());
        }
        
        // 確認メッセージ表示
        showLocationProgressDialog();
    }
    	
	private void showLocationProgressDialog() {
		if (cmodel.isTweetLocationConfirm() && !cmodel.isTweetLocation()) {
			LayoutInflater inflater = LayoutInflater.from(this);
			final View view = inflater.inflate(R.layout.confirm, (ViewGroup)findViewById(R.id.layout_confirm_root));
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
				public void onClick(DialogInterface dialog, int which) {
					if (!manager.requestLocationUpdates()) {
						Toast.makeText(
								TweetActivity.this,
								getText(R.string.location_provider_error_messae), 
								Toast.LENGTH_LONG).show();
					}
					setLocationConfig(view, true);
				}
				
			});
			
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
				public void onClick(DialogInterface dialog, int which) {
					setLocationConfig(view, false);
				}
			
			});
		
	    	builder.setTitle(getText(R.string.tweet_location_dialog_title));
	    	builder.setMessage(getText(R.string.tweet_location_dialog_message));
	    	builder.setView(view);
			builder.setCancelable(true);
	    	builder.show();
		}
		else {
			if (cmodel.isTweetLocation()) {
				if (!manager.requestLocationUpdates()) {
					Toast.makeText(
							TweetActivity.this,
							getText(R.string.location_provider_error_messae), 
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	private void setLocationConfig(View view, boolean isLocation) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TweetActivity.this);
		
		boolean checked = ((CheckBox)view.findViewById(R.id.confirm)).isChecked();
		
		Editor editor = preferences.edit();
		if (checked) {
			editor.putBoolean("tweet_location", isLocation);
		}
		editor.putBoolean("tweet_location_confirm", !checked);
		editor.commit();
	}
	
    private void setTwitterInfo(String token, String secret, String account, String password) {
		xmodel.setOAuthToken(token);
		xmodel.setOAuthTokenSecret(secret);
		tmodel.setAccount(account);
		tmodel.setPassword(password);
    }
    
    private void showProgressDialog(int id) {
    	if (dialog == null) {
    		dialog = new ProgressDialog(TweetActivity.this);
    		dialog.setIndeterminate(true);
    	}
		dialog.setMessage(getText(id));
    	dialog.show();
    }
    
    private void showSigninDialog() {
    	LayoutInflater inflater = LayoutInflater.from(this);
    	final View view = inflater.inflate(R.layout.signin, (ViewGroup)findViewById(R.id.signin_layout_root));
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String account = ((EditText)view.findViewById(R.id.account)).getText().toString();
				String password = ((EditText)view.findViewById(R.id.password)).getText().toString();
				boolean checked = ((CheckBox)view.findViewById(R.id.rememberme)).isChecked();
				
				if (account.equals("") || password.equals("")) {
					Toast.makeText(
						TweetActivity.this,
						getText(R.string.twitter_signin_empty_message), 
						Toast.LENGTH_LONG).show();
				}
				else {
					tmodel.setAccount(account);
					tmodel.setPassword(password);
					
					if (checked) {
						setTweetConfig(account, password);
					}
					Tweet();
				}
			}
			
		});
    	
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
			
		});
		
    	builder.setTitle(getText(R.string.translate_signin_dialog_title));
		builder.setCancelable(true);
    	builder.setView(view);
    	builder.show();
    }
    
    private void setTweetConfig(String account, String password) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		editor.putBoolean("tweet", true);
		editor.putString("twitter_account", account);
		editor.putString("twitter_password", password);
		editor.commit();
    }
    
    private void Tweet() {
		showProgressDialog(R.string.tweet_progress_message);
    	new TweetThread(new Handler(), this, tmodel, xmodel).start();
    }
    
	public void run() {
    	dialog.dismiss();
		
		if (tmodel.isSuccess()) {
			Toast.makeText(
					this,
					getText(R.string.tweet_complete_message), 
					Toast.LENGTH_LONG).show();
			
			finish();
		}
		else {
			CharSequence msg = getText(R.string.tweet_failed_message); 
			
			if (!tmodel.isAuthorized()) {
				cmodel.setAccount("");
				cmodel.setPassword("");
				
				Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
				editor.putBoolean("tweet", false);
				editor.putString("twitter_account", "");
				editor.putString("twitter_password", "");
				editor.commit();
				
				msg = getText(R.string.tweet_authentication_failed_message);
			}
			
			Toast.makeText(
					this, 
					msg, 
					Toast.LENGTH_LONG).show();
		}
	}
	
	private class GPSLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			tmodel.setLat(String.valueOf(location.getLatitude()));
			tmodel.setLng(String.valueOf(location.getLongitude()));
			
			manager.removeAllUpdates();
		}

		public void onProviderDisabled(String provider) {
			manager.removeAllUpdates();
			Toast.makeText(
				TweetActivity.this,
				getText(R.string.location_provider_error_messae), 
				Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				manager.removeAllUpdates();
				Toast.makeText(
					TweetActivity.this,
					getText(R.string.location_failed_message), 
					Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private class NetworkLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			tmodel.setLat(String.valueOf(location.getLatitude()));
			tmodel.setLng(String.valueOf(location.getLongitude()));
			
			manager.removeAllUpdates();
		}

		public void onProviderDisabled(String provider) {
			manager.removeAllUpdates();
			Toast.makeText(
				TweetActivity.this,
				getText(R.string.location_provider_error_messae), 
				Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				manager.removeAllUpdates();
				Toast.makeText(
						TweetActivity.this,
						getText(R.string.location_failed_message), 
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
