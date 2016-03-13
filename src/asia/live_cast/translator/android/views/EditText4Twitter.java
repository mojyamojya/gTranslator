package asia.live_cast.translator.android.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;
import asia.live_cast.translator.android.R;

public class EditText4Twitter extends EditText {
	
	public EditText4Twitter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void isValid() {
		String text = getEditableText().toString();
		
		if (text.equals("")) {
			setError(getResources().getString((getId() == R.id.account) ?
					R.string.conf_twitter_account_empty_message : R.string.conf_twitter_password_empty_message));
		}
		else {
			setError(null);
		}
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		if (!focused) {
			isValid();
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}
}