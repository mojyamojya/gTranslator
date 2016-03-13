package asia.live_cast.translator.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import asia.live_cast.translator.android.schemas.ITranslate;

public class TranslateProvider extends ContentProvider {
	private TranslateHelper helper;
	private static final String AUTHORITY = "asia.live_cast.translator.android.provider.TranslateProvider";
	private static final int HISTORIES = 1;
	private static final int HISTORY_ID = 2;
	private static final UriMatcher uriMatcher;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, ITranslate.TABLE_NAME, HISTORIES);
		uriMatcher.addURI(AUTHORITY, ITranslate.TABLE_NAME + "/#", HISTORY_ID);
	}

	private static class TranslateHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "histories.db";
		private static final int DATABASE_VERSION = 1;

		public TranslateHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS "+ ITranslate.TABLE_NAME + "(" +
				BaseColumns._ID + " INTEGER PRIMARY KEY, " +
				"original TEXT, " +
				"translated TEXT, " +
				"languagefrom TEXT, " +
				"languageto TEXT, " +
				"lat TEXT, " +
				"lng TEXT, " +
				"dt TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE " + ITranslate.TABLE_NAME + " if exists " + ITranslate.TABLE_NAME + ";");
			onCreate(db);
		}
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		
		SQLiteDatabase db = helper.getReadableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case HISTORIES:
			count = db.delete(ITranslate.TABLE_NAME, selection, selectionArgs);
			break;
		case HISTORY_ID:
			final String id = uri.getPathSegments().get(1);
			count = db.delete(
				ITranslate.TABLE_NAME,
				BaseColumns._ID + "=" + id + ((selection == null) ? "" : " AND (" + selection + ")"),
				selectionArgs); 
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri); 
		}
		
		return count;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.insert(ITranslate.TABLE_NAME, null, values);
		return null;
	}

	@Override
	public boolean onCreate() {
		helper = new TranslateHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteDatabase db = helper.getReadableDatabase();
		SQLiteQueryBuilder sql = new SQLiteQueryBuilder();
		sql.setTables(ITranslate.TABLE_NAME);
		Cursor cursor = sql.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

}
