package asia.live_cast.translator.android.views;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import asia.live_cast.translator.android.R;

public class EditTextPreference4Twitter extends EditTextPreference {
	private boolean first;
	
	public EditTextPreference4Twitter(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		getEditText().setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (!first) {
					if (hasFocus) {
						first = hasFocus;
					}
				}
				
				if (first) {
					if (!hasFocus) {
						isValid();
					}
				}
			}
			
		});
	}

	private void isValid() {
		EditText text = getEditText();
		
		if (text.getText().toString().equals("")) {
			text.setError(text.getResources().getString((getKey().equals("twitter_account")) ?
					R.string.conf_twitter_account_empty_message : R.string.conf_twitter_password_empty_message));
		}
		else {
			text.setError(null);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    super.onDialogClosed(positiveResult);

		getEditText().setError(null);
		first = false;
	}
}