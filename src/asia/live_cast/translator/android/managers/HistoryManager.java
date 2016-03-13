package asia.live_cast.translator.android.managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import asia.live_cast.translator.android.adapter.TranslateListAdapter;
import asia.live_cast.translator.android.dto.TranslateDto;

public class HistoryManager {
	private ContentResolver resolver;
	private int page;
	public final static int HISTORY_LIST_VIEW_COUNT = 20;

	public HistoryManager(Context context) {
 		this.resolver = context.getApplicationContext().getContentResolver();
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}
	
	public int getHistoriesCount() {
 		int count = 0;
 		
 		Cursor cursor = resolver.query(Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider"),
 				new String[] {"count(*)"}, null, null, null);
 		
 		if (cursor != null) {
 			if (cursor.getCount() == 1) {
 				cursor.moveToFirst();
 				count = cursor.getInt(0);
 			}
 		}
 		
		cursor.close();
		
 		return count;
	}
	
    public int setTranslateAdapter(TranslateListAdapter adapter, boolean reset) {
    	if (reset) {
    		adapter.clear();
    	}
    	
    	Cursor cursor = getHistories(reset);
    	
    	addTranslateModel(adapter, cursor);
		
		cursor.close();
		
		return adapter.getCount();
    }
    
	private Cursor getHistories(boolean reset) {
    	String order = BaseColumns._ID + " DESC limit ";
    	
    	if (reset) {
    		order = order + String.valueOf(HISTORY_LIST_VIEW_COUNT * (page + 1));
    	}
    	else {
    		order = order + String.valueOf(HISTORY_LIST_VIEW_COUNT) + " offset " +
    			String.valueOf(HISTORY_LIST_VIEW_COUNT * page); 
    	}
    	
       return resolver.query(Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider"),
            	null, null, null, order);
	}
	
	private int addTranslateModel(TranslateListAdapter adapter, Cursor cursor) {
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					TranslateDto dto = new TranslateDto();
					dto.setId(cursor.getInt(0));
					dto.setOriginal(cursor.getString(1));
					dto.setTranslated(cursor.getString(2));
					dto.setLanguageFrom(cursor.getString(3));
					dto.setLanguageTo(cursor.getString(4));
					dto.setDt(cursor.getString(7));
					adapter.add(dto);
				}
			}
		}
		
		return adapter.getCount();
	}
	
    public void delete(int id) {
		resolver.delete(
				Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider/histories/" + String.valueOf(id)), null, null);
    }
    
    public void deleteAll() {
    	resolver.delete(Uri.parse("content://asia.live_cast.translator.android.provider.TranslateProvider/histories/"), null, null);
    }
}
