package asia.live_cast.translator.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import asia.live_cast.translator.android.R;

public class TranslateListView extends ListView {
	private boolean visible;
	private View _footer;

	public TranslateListView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public void setFooterVisible(boolean visible) {
		if (_footer != null) {
			if (!visible) {
				removeFooterView(_footer);
			}
		}
		
		this.visible = visible;
	}
	
	public boolean isFooterVisible() {
		return this.visible;
	}
	
	@Override
	public void addFooterView(View footer) {
		super.addFooterView(footer);
		
		if (_footer != null) {
			removeFooterView(_footer);
		}
		
		_footer = footer;
        ImageView img = (ImageView)_footer.findViewById(R.id.more);
        img.setImageResource(android.R.drawable.ic_menu_more);
	}

}