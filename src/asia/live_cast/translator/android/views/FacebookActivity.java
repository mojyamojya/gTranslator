package asia.live_cast.translator.android.views;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.models.FacebookModel;
import asia.live_cast.translator.android.threads.FacebookThread;
import asia.live_cast.translator.android.views.HistoryActivity;
import asia.live_cast.translator.android.views.ConfigActivity;

public class FacebookActivity extends Activity implements Runnable {
	private Facebook facebook = new Facebook("Your own developer's id");
	private FacebookModel fmodel;
    private ProgressDialog dialog;
    private AdView adView;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.facebook);
        
        // タイトルバーのカスタマイズ（ActionBar風）
        setTitle();
        
        // 翻訳画面から受け渡されたIntentを取得
        Intent intent = getIntent();
        
        // 画面コントロールの初期化
        initModel();
        initText(intent);		// EditTextに翻訳結果をデフォルト表示する
        initButton();			// Clickイベントリスナーの設定
        
        // 広告（Admob）の初期化
        adView = new AdView(this, AdSize.BANNER, "Your own admob publisher id.");
        LinearLayout layout = (LinearLayout)findViewById(R.id.facebookLayout);
        layout.addView(adView);
        adView.loadAd(new AdRequest());
	}
	
	private void setTitle() {
		// タイトルバーのカスタマイズ（ActionBar風）
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.facebook_title);
		TextView title = (TextView)findViewById(R.id.fb_title);
		title.setText(getText(R.string.facebook_title_text));
		
		// アイコン
//		ImageView icon = (ImageView)findViewById(R.id.fb_icon);
//		icon.setImageResource(R.drawable.f_logo);
		
		// 履歴ボタン
    	ImageView history = (ImageView)findViewById(R.id.fb_history);
    	history.setImageResource(android.R.drawable.ic_menu_recent_history);
    	history.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 履歴画面（Activity）を開く
	        	Intent history = new Intent(FacebookActivity.this, HistoryActivity.class);
	        	history.setAction(Intent.ACTION_VIEW);
	        	startActivity(history);
			}
    		
    	});
    	
    	// 設定ボタン
    	ImageView config = (ImageView)findViewById(R.id.fb_config);
    	config.setImageResource(android.R.drawable.ic_menu_preferences);
    	config.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 設定画面（Activity）を開く
	        	Intent config = new Intent(FacebookActivity.this, ConfigActivity.class);
	        	config.setAction(Intent.ACTION_VIEW);
	        	startActivity(config);
			}
    		
    	});
	}
	
	private void initModel() {
		if (fmodel == null) {
			fmodel = new FacebookModel(PreferenceManager.getDefaultSharedPreferences(FacebookActivity.this));
		}
	}
	
	private void initText(Intent intent) {
    	String translated = "";
    	
    	// 翻訳結果を取得
    	if (intent != null) {
    		translated = (intent.getStringExtra("Translated"));
    	}
    	
    	EditText txtMessage = (EditText)findViewById(R.id.message);

    	// 画面にデフォルト表示する
    	if (!translated.equals("")) {
    		txtMessage.setText(translated);
    	}
	}
	
	private void initButton() {
		// ボタン初期化処理
		Button btnPost = (Button)findViewById(R.id.fb_post);
		Button btnCancel = (Button)findViewById(R.id.fb_cancel);
		
		// 投稿ボタンのClickイベントハンドラセット
		btnPost.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
		    	EditText txtMessage = (EditText)findViewById(R.id.message);
		    	String message = txtMessage.getText().toString(); 
		    	
		    	// 入力されているか？
		    	if (!message.equals("")) {
		    		// 空じゃなければウォールに投稿
		    		fmodel.setMessage(message);
		    		
		    		// アクセストークンと有効期限を取得
		    		String token = fmodel.getToken();
		    		long expires = fmodel.getExpires();
		    		
		    		// アクセストークンと有効期限が保存されているか？
		    		if (token != null && expires != -1) {
		    			// あればその値をセット
			    		facebook.setAccessToken(token);
			    		facebook.setAccessExpires(expires);
		    		}
		    		
		    		// セッションが有効かチェック
		    		if (!facebook.isSessionValid()) {
		    			// 無効の場合には認証
			    		facebook.authorize(FacebookActivity.this, new String[] {"publish_stream"}, new DialogListener() {
	
			    			public void onComplete(Bundle values) {
			    				// アクセストークンと有効期限を保存
			    				fmodel.setToken(facebook.getAccessToken());
			    				fmodel.setExpires(facebook.getAccessExpires());
			    				
			    				// ウォールに投稿
					    		postToFacebook();
			    			}
	
			    			public void onFacebookError(FacebookError e) {
			    	 			// 認証失敗
			    				showAuthorizeFailedMessage();
			    			}
	
			    			public void onError(DialogError e) {
			    	 			// 認証失敗
			    				showAuthorizeFailedMessage();
			    			}
	
			    			public void onCancel() {
			    				
			    			}
			    		});
		    		}
		    		else {
	    				// セッションが有効な場合はすぐにウォールに投稿
			    		postToFacebook();
		    		}
		    	}
			}
		});
		
		// キャンセルボタンのClickイベントハンドラセット
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void postToFacebook() {
		// 処理中ダイアログの表示
		showProgressDialog(R.string.facebook_progress_message);
		
		// Thread実行
    	new FacebookThread(new Handler(), FacebookActivity.this, facebook, fmodel).start();
	}
    
	private void showAuthorizeFailedMessage() {
		// 認証失敗エラーメッセージ表示
		Toast.makeText(
			FacebookActivity.this,
			getText(R.string.facebook_authorize_failed_message), 
			Toast.LENGTH_LONG).show();
	}
	
	private void showProgressDialog(int id) {
		// 処理中ダイアログの表示
    	if (dialog == null) {
    		dialog = new ProgressDialog(FacebookActivity.this);
    		dialog.setIndeterminate(true);
    	}
		dialog.setMessage(getText(id));
    	dialog.show();
    }

	public void run() {
		// Facebookに投稿処理のコールバック
    	dialog.dismiss();
		
 		if (fmodel.isSuccess()) {
 			// 正常終了
			Toast.makeText(
				this,
				getText(R.string.facebook_posted_message), 
				Toast.LENGTH_LONG).show();
			
			finish();
		}
 		else {
 			// 投稿失敗
			Toast.makeText(
				this,
				getText(R.string.facebook_error_message), 
				Toast.LENGTH_LONG).show();
 		}
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
}
