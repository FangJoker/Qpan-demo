package com.chavez.qpan.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chavez.qpan.service.UploadService;

import java.util.Map;

public class UploadProvider extends ContentProvider {
    private static final String TAG = "UploadProvider";
    /**
     * Database filename
     */
    private static final String DB_NAME = "qpan_upload.db";
    /**
     * Current database version
     */
    private static final int DB_VERSION = 100;
    /**
     * Name of table in the database
     */
    private static final String DB_TABLE = "qpan_upload";

    /**
     * URI matcher used to recognize URIs sent by applications
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int UPLOAD_INFO = 1;

    private static final int UPLOAD_INFO_BY_ID = 2;

    private static final String AUTHORITY = "com.chavez.qpan.providers.UploadProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/upload");

    private static final Map<String, String> mUploadInfoMap = new ArrayMap<>();

    static {
        sURIMatcher.addURI(AUTHORITY, "upload", UPLOAD_INFO);
        sURIMatcher.addURI(AUTHORITY, "upload/#", UPLOAD_INFO_BY_ID);

        mUploadInfoMap.put(UploadColumns._ID, UploadColumns._ID);
        mUploadInfoMap.put(UploadColumns.COLUMN_CHUNK, UploadColumns.COLUMN_CHUNK);
        mUploadInfoMap.put(UploadColumns.COLUMN_CHUNKS, UploadColumns.COLUMN_CHUNKS);
        mUploadInfoMap.put(UploadColumns.COLUMN_CURRENT_BYTE, UploadColumns.COLUMN_CURRENT_BYTE);
        mUploadInfoMap.put(UploadColumns.COLUMN_TOTAL_BYTES, UploadColumns.COLUMN_TOTAL_BYTES);
        mUploadInfoMap.put(UploadColumns.COLUMN_DATA, UploadColumns.COLUMN_DATA);
        mUploadInfoMap.put(UploadColumns.COLUMN_STATUS, UploadColumns.COLUMN_STATUS);
        mUploadInfoMap.put(UploadColumns.COLUMN_TITEL, UploadColumns.COLUMN_TITEL);
        mUploadInfoMap.put(UploadColumns.COLUMN_TYPE, UploadColumns.COLUMN_TYPE);
        mUploadInfoMap.put(UploadColumns.COLUMN_URL, UploadColumns.COLUMN_URL);
        mUploadInfoMap.put(UploadColumns.COLUMN_UPLOAD_PATH,UploadColumns.COLUMN_UPLOAD_PATH);
        mUploadInfoMap.put(UploadColumns.COLUMN_CREATE_TIME, UploadColumns.COLUMN_CREATE_TIME);
    }


    private Context mContext;
    private UploadProvider.DatabaseHelper mOpenHelper;

    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onUpgrade(db, 0, DB_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 100) {
                oldVersion = 99;
            } else if (oldVersion > newVersion) {
                oldVersion = 99;
            }
            for (int version = oldVersion + 1; version <= newVersion; version++) {
                upgradeTo(db, version);
            }
        }

        private void upgradeTo(SQLiteDatabase db, int version) {
            switch (version) {
                case 100:
                    createDownloadsTable(db);
                    break;
            }
        }

        private void createDownloadsTable(SQLiteDatabase db) {
            try {
                final String CREATE_TABLE =
                        "CREATE TABLE " + DB_TABLE + "(" +
                                UploadColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                UploadColumns.COLUMN_CHUNK + " INT, " +
                                UploadColumns.COLUMN_CHUNKS + " INT," +
                                UploadColumns.COLUMN_CURRENT_BYTE + " LONG," +
                                UploadColumns.COLUMN_TOTAL_BYTES + " LONG," +
                                UploadColumns.COLUMN_DATA + " TEXT," +
                                UploadColumns.COLUMN_TITEL + " TEXT," +
                                UploadColumns.COLUMN_URL + " TEXT," +
                                UploadColumns.COLUMN_STATUS + " INT," +
                                UploadColumns.COLUMN_CREATE_TIME + " TEXT, " +
                                UploadColumns.COLUMN_UPLOAD_PATH +" TEXT, "+
                                UploadColumns.COLUMN_TYPE + " INT);";
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
                db.execSQL(CREATE_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public boolean onCreate() {
        mContext = getContext();
        mOpenHelper = new UploadProvider.DatabaseHelper(mContext);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);
        Log.v(TAG, "querying user info: " + uri);
        final SQLiteQueryBuilder qb = getQueryBuilder(uri, match);
        return qb.query(db, projection, selection, selectionArgs, null, null, null);
    }


    private SQLiteQueryBuilder getQueryBuilder(Uri uri, int match) {
        if (match == -1) {
            Log.v(TAG, "querying unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        StringBuilder where = new StringBuilder();
        qb.setTables(DB_TABLE);
        if (match == UPLOAD_INFO_BY_ID) {
            long id = ContentUris.parseId(uri);
            where.append(UploadColumns._ID + " = " + id);
        }
        qb.setProjectionMap(mUploadInfoMap);
        qb.setStrict(true);
        qb.appendWhere(where);
        return qb;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    String getToken() {
        Cursor cursor = mContext.getContentResolver().query(UserInfoProvider.CONTENT_URI, new String[]{
                UserColumns.COLUMN_ACCESS_TOKEN}, null, null, null);
        if (cursor.moveToLast()) {
            String accessToken = cursor.getString(cursor.getColumnIndex(UserColumns.COLUMN_ACCESS_TOKEN));
            if (!TextUtils.isEmpty(accessToken)) return accessToken;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        isValidUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        getContentValues(values, contentValues);
        long id = db.insert(DB_TABLE, null, contentValues);
        if (id > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
            System.out.println("==UploadProvider insert: " + uri.toString());
            String uploadPathUuid = null;
            if (contentValues.get(UploadColumns.COLUMN_UPLOAD_PATH) != null) {
                uploadPathUuid = contentValues.get(UploadColumns.COLUMN_UPLOAD_PATH).toString();
            }
            UploadService.startActionUpload(mContext, id, getToken(), uploadPathUuid);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        } else {
            Log.d(TAG, "couldn't insert into user database");
            return null;
        }
    }

    private int isValidUri(Uri uri) {
        int match = sURIMatcher.match(uri);
        if (match != UPLOAD_INFO && match != UPLOAD_INFO_BY_ID) {
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        } else {
            return match;
        }
    }

    private ContentValues getContentValues(ContentValues fromValues, ContentValues toValues) {
        copyString(UploadColumns.COLUMN_URL, fromValues, toValues);
        copyString(UploadColumns.COLUMN_TYPE, fromValues, toValues);
        copyString(UploadColumns.COLUMN_TITEL, fromValues, toValues);
        copyString(UploadColumns.COLUMN_STATUS, fromValues, toValues);
        copyString(UploadColumns.COLUMN_DATA, fromValues, toValues);
        copyString(UploadColumns.COLUMN_TOTAL_BYTES, fromValues, toValues);
        copyString(UploadColumns.COLUMN_CURRENT_BYTE, fromValues, toValues);
        copyString(UploadColumns.COLUMN_CHUNKS, fromValues, toValues);
        copyString(UploadColumns.COLUMN_CHUNK, fromValues, toValues);
        copyString(UploadColumns.COLUMN_CREATE_TIME, fromValues, toValues);
        copyString(UploadColumns.COLUMN_UPLOAD_PATH, fromValues, toValues);
        return toValues;
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int result = db.delete(DB_TABLE, selection, selectionArgs);
        if (result > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        isValidUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int match = sURIMatcher.match(uri);
        int result = -1;
        switch (match) {
            case UPLOAD_INFO_BY_ID:
                getContentValues(values, contentValues);
                StringBuilder logStringBuilder = new StringBuilder();
                for (String key : contentValues.keySet()) {
                    logStringBuilder.append(key).append(":").append(contentValues.get(key));
                }
                Log.v(TAG, "update uri: " + uri + " " + logStringBuilder);
                long id = ContentUris.parseId(uri);
                result = db.update(DB_TABLE, contentValues, " _id = ? ", new String[]{String.valueOf(id)});
                break;
            case UPLOAD_INFO:
                result = db.update(DB_TABLE, contentValues, selection, selectionArgs);
                break;
            default:
                break;
        }
        if (result < 0) {
            Log.v(TAG, "couldn't update user database");
            return -1;
        } else {
            mContext.getContentResolver().notifyChange(uri, null);
            return result;
        }
    }
}
