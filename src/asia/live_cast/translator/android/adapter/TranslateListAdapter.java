package asia.live_cast.translator.android.adapter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import asia.live_cast.translator.android.R;
import asia.live_cast.translator.android.dto.TranslateDto;
import asia.live_cast.translator.android.managers.HistoryManager;
import asia.live_cast.translator.android.views.TranslateListView;

public class TranslateListAdapter extends BaseAdapter implements DialogInterface.OnClickListener {
	private LayoutInflater inflater;
	private ArrayList<TranslateDto> list;
	private HistoryManager manager;
	private TranslateListView view;
	private int id;

	public TranslateListAdapter(Context context, ArrayList<TranslateDto> list, HistoryManager manager) {
		this.list = list;
		this.manager = manager;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return list.get(position).getId();
	}

	public View getView(int position, View view, ViewGroup parent) {
		view = inflater.inflate(R.layout.list_item, null);
		
		TextView original = (TextView)view.findViewById(R.id.original);
		TextView translated = (TextView)view.findViewById(R.id.translated);
		TextView dt = (TextView)view.findViewById(R.id.dt);
		ImageView image = (ImageView)view.findViewById(R.id.delete);
		
		original.setText(list.get(position).getOriginal());
		translated.setText(list.get(position).getTranslated());
		dt.setText(list.get(position).getDt());
		image.setImageResource(android.R.drawable.ic_menu_delete);
		image.setId(list.get(position).getId());
		
		image.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				TranslateListAdapter.this.id = v.getId();
				TranslateListAdapter.this.view = ((TranslateListView)v.getParent().getParent());
				
		    	final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

		    	builder.setPositiveButton("OK", TranslateListAdapter.this);
		    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
					
				});
		    	
		    	builder.setTitle(v.getResources().getText(R.string.adp_history_delete_dialog_title));
		    	builder.setMessage(v.getResources().getText(R.string.adp_history_delete_dialog_message));
				builder.setCancelable(true);
		    	builder.show();
			}
			
		});
		
		return view;
	}
	
	public void add(TranslateDto dto) {
		list.add(dto);
	}

	public void clear() {
		list.clear();
	}

	public void onClick(DialogInterface dialog, int which) {
    	manager.delete(id);
		int max = manager.getHistoriesCount();
		int len = manager.setTranslateAdapter(TranslateListAdapter.this, true);
		int pos = HistoryManager.HISTORY_LIST_VIEW_COUNT * (manager.getPage());
		view.setAdapter(TranslateListAdapter.this);
		view.setFooterVisible(!(len == max));
		view.setSelection((max > pos) ? pos : max);
	}
}
