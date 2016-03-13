package asia.live_cast.translator.android.views;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.gTranslator;
import asia.live_cast.translator.android.adapter.TranslateListAdapter;
import asia.live_cast.translator.android.dto.TranslateDto;
import asia.live_cast.translator.android.managers.HistoryManager;

public class HistoryActivity extends Activity {
	private TranslateListAdapter adapter; 
	private HistoryManager manager;
	private int max;

    private static final int MENU_ID_MENU1 = Menu.FIRST + 1;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.history);
        
        // タイトルバーのカスタマイズ（ActionBar風）
        setTitle();

        ArrayList<TranslateDto> arr = new ArrayList<TranslateDto>();
        manager = new HistoryManager(this);
        adapter = new TranslateListAdapter(this, arr, manager);
        manager.setPage(0);
        max = manager.getHistoriesCount();
        int len = manager.setTranslateAdapter(adapter, false);
        
        TranslateListView list = (TranslateListView)findViewById(R.id.histories);
        list.addFooterView(getLayoutInflater().inflate(R.layout.footer, null));
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.message));
        list.setFooterVisible(!(len == max));
        list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TranslateListView list = ((TranslateListView)findViewById(R.id.histories));
				if (list.isFooterVisible()) {
					if (position == (parent.getCount() - 1)) {
						manager.setPage(manager.getPage() + 1);
				        max = manager.getHistoriesCount();
						int len = manager.setTranslateAdapter(adapter, false);
						int pos = HistoryManager.HISTORY_LIST_VIEW_COUNT * (manager.getPage());
						list.setAdapter(adapter);
						list.setFooterVisible(!(len == max));
						list.setSelection((max > pos) ? pos : max);
						return;
					}
				}
				
				Intent intent = new Intent(HistoryActivity.this, gTranslator.class);
				intent.putExtra("TranslateDto", (TranslateDto)parent.getItemAtPosition(position));
				intent.setAction(Intent.ACTION_VIEW);
				startActivity(intent);
			}
        	
        });
	}
	
	private void setTitle() {
		// タイトルバーのカスタマイズ（ActionBar風）
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.history_title);
		TextView title = (TextView)findViewById(R.id.his_title);
		title.setText(getText(R.string.app_name));
		
		// 戻るボタン
		ImageView revert = (ImageView)findViewById(R.id.his_revert);
		revert.setImageResource(android.R.drawable.ic_menu_revert);
		revert.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
		
    	// 履歴ボタン
    	ImageView config = (ImageView)findViewById(R.id.his_config);
    	config.setImageResource(android.R.drawable.ic_menu_preferences);
    	config.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 履歴画面（Activity）を開く
	        	Intent config = new Intent(HistoryActivity.this, ConfigActivity.class);
	        	config.setAction(Intent.ACTION_VIEW);
	        	startActivity(config);
			}
    		
    	});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ID_MENU1, Menu.NONE, "Delete all").setIcon(android.R.drawable.ic_menu_delete);
    	
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
   		manager = new HistoryManager(this);
    	
    	menu.getItem(0).setEnabled(manager.getHistoriesCount() > 0);
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	boolean result = true;
    	
    	switch (item.getItemId()) {
    	case MENU_ID_MENU1:
	    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
		        	manager.deleteAll();
		    		manager.setTranslateAdapter(adapter, true);
		        	
		            TranslateListView list = (TranslateListView)findViewById(R.id.histories);
					list.setAdapter(adapter);
				}
	    		
	    	});
	    	
	    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
				
			});
	    	
	    	builder.setTitle(getText(R.string.adp_history_delete_dialog_title));
	    	builder.setMessage(getText(R.string.adp_history_delete_all_dialog_message));
			builder.setCancelable(true);
	    	builder.show();
    		break;
    	default:
    		result = false;
    		break;
    	}
    	
    	return result;
    }
}


