package asia.live_cast.translator.android.views;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import asia.live_cast.translator.android.R;

public class ConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config);
        
        // タイトルバーのカスタマイズ（ActionBar風）
        setTitle();
        
        ListPreference srcLangPreference = (ListPreference)findPreference("list_src_language");
        ListPreference dstLangPreference = (ListPreference)findPreference("list_dst_language");
        ListPreference transEnginePreference = (ListPreference)findPreference("list_translation_engines");
        
        if (srcLangPreference.getValue().equals("zh")) {
        	srcLangPreference.setValue("zh-CN");
        }
        if (dstLangPreference.getValue().equals("zh")) {
        	dstLangPreference.setValue("zh-CN");
        }
        
        srcLangPreference.setTitle(String.format(getString(R.string.conf_translate_src_lang_title), srcLangPreference.getEntry()));
        dstLangPreference.setTitle(String.format(getString(R.string.conf_translate_dst_lang_title), dstLangPreference.getEntry()));
        transEnginePreference.setTitle("Microsoft Bing Translate");
        
        srcLangPreference.setOnPreferenceChangeListener(this);
        dstLangPreference.setOnPreferenceChangeListener(this);
        transEnginePreference.setOnPreferenceChangeListener(this);
	}
	
	private void setTitle() {
		// タイトルバーのカスタマイズ（ActionBar風）
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.config_title);
		TextView title = (TextView)findViewById(R.id.conf_title);
		title.setText(getText(R.string.app_name));
		
		// 戻るボタン
		ImageView revert = (ImageView)findViewById(R.id.conf_revert);
		revert.setImageResource(android.R.drawable.ic_menu_revert);
		revert.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
		
    	// 履歴ボタン
    	ImageView history = (ImageView)findViewById(R.id.conf_history);
    	history.setImageResource(android.R.drawable.ic_menu_recent_history);
    	history.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 履歴画面（Activity）を開く
	        	Intent history = new Intent(ConfigActivity.this, HistoryActivity.class);
	        	history.setAction(Intent.ACTION_VIEW);
	        	startActivity(history);
			}
    		
    	});
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean result = false;
		
		CharSequence[] values = ((ListPreference)preference).getEntryValues();
		CharSequence[] entries = ((ListPreference)preference).getEntries();
		
		int i;
		int len = entries.length;
		for (i = 0; i < len; i++) {
			if (values[i].equals(newValue)) {
				break;
			}
		}
		
		String key = preference.getKey();
		
		if (key.equals("list_src_language")) {
			// 言語設定（from）
			preference.setTitle(String.format(getString(R.string.conf_translate_src_lang_title), entries[i]));
			result = true;
		}
		else if (key.equals("list_dst_language")) {
			// 言語設定（to）
			preference.setTitle(String.format(getString(R.string.conf_translate_dst_lang_title), entries[i]));
			result = true;
		}
		else if (key.equals("list_translation_engines")) {
			// 翻訳エンジン
			preference.setTitle(String.format(getString(R.string.conf_translate_engines_title), entries[i]));
			result = true;
		}
		
		return result;
	}
}
