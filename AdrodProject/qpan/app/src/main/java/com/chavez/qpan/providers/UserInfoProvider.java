package com.chavez.qpan.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class UserInfoProvider extends ContentProvider {
    private static final String TAG = "UserInfoProvider";
    /**
     * Database filename
     */
    private static final String DB_NAME = "user.db";
    /**
     * Current database version
     */
    private static final int DB_VERSION = 100;
    /**
     * Name of table in the database
     */
    private static final String DB_TABLE = "user";

    /**
     * URI matcher used to recognize URIs sent by applications
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int USER_INFO = 1;

    private static final int USER_INFO_BY_ID = 2;


    public static final Uri CONTENT_URI = Uri.parse("content://com.chavez.qpan.providers.UserInfoProvider/user");

    private static final Map<String, String> mUserInfoMap = new ArrayMap<>();

    static {
        sURIMatcher.addURI("com.chavez.qpan.providers.UserInfoProvider", "user", USER_INFO);
        sURIMatcher.addURI("com.chavez.qpan.providers.UserInfoProvider", "user/#", USER_INFO_BY_ID);

        mUserInfoMap.put(UserColumns.COLUMN_USER_ACCOUNT,UserColumns.COLUMN_USER_ACCOUNT);
        mUserInfoMap.put(UserColumns.COLUMN_FREE_BYTES, UserColumns.COLUMN_FREE_BYTES);
        mUserInfoMap.put(UserColumns.COLUMN_TOTAL_BYTES,UserColumns.COLUMN_TOTAL_BYTES);
        mUserInfoMap.put(UserColumns.COLUMN_USER_NAME, UserColumns.COLUMN_USER_NAME);
        mUserInfoMap.put(UserColumns.COLUMN_ACCESS_TOKEN, UserColumns.COLUMN_ACCESS_TOKEN);
        mUserInfoMap.put(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME, UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME);
    }


    private Context mContext;
    private DatabaseHelper mOpenHelper;

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
                                UserColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                UserColumns.COLUMN_USER_ACCOUNT +" TEXT, "+
                                UserColumns.COLUMN_FREE_BYTES + " LONG, " +
                                UserColumns.COLUMN_TOTAL_BYTES+ " LONG, " +
                                UserColumns.COLUMN_USER_NAME + " TEXT, " +
                                UserColumns.COLUMN_ACCESS_TOKEN + " TEXT, " +
                                UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME + " LONG);";
                System.out.println("create table:" + CREATE_TABLE);
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
        mOpenHelper = new DatabaseHelper(mContext);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);
        Log.v(TAG, "querying user info: " + uri);
        final SQLiteQueryBuilder qb = getQueryBuilder(uri, match);
        return qb.query(db, projection, null, null, null, null, null);
    }

    private SQLiteQueryBuilder getQueryBuilder(Uri uri, int match) {
        if (match == -1) {
            Log.v(TAG, "querying unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        StringBuilder where = new StringBuilder();
        qb.setTables(DB_TABLE);
        if (match == USER_INFO_BY_ID) {
            long id = ContentUris.parseId(uri);
            where.append(" where " + UserColumns._ID + " = " + id);
        }
        qb.setProjectionMap(mUserInfoMap);
        qb.setStrict(true);
        qb.appendWhere(where);
        return qb;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
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
            return ContentUris.withAppendedId(CONTENT_URI, id);
        } else {
            Log.d(TAG, "couldn't insert into user database");
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        isValidUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.execSQL("DELETE FROM USER");
        return  1;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        isValidUri(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        getContentValues(values, contentValues);
        int result = db.update(DB_TABLE, contentValues, selection, selectionArgs);
        if (result < 0) {
            Log.d(TAG, "couldn't update user database");
            return -1;
        } else {
            return result;
        }
    }

    private int isValidUri(Uri uri) {
        int match = sURIMatcher.match(uri);
        if (match != USER_INFO) {
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        } else {
            return match;
        }
    }

    private ContentValues getContentValues(ContentValues fromValues, ContentValues toValues) {
        copyString(UserColumns.COLUMN_FREE_BYTES, fromValues, toValues);
        copyString(UserColumns.COLUMN_TOTAL_BYTES,fromValues,toValues);
        copyString(UserColumns.COLUMN_USER_ACCOUNT,fromValues,toValues);
        copyString(UserColumns.COLUMN_USER_NAME, fromValues, toValues);
        copyString(UserColumns.COLUMN_ACCESS_TOKEN, fromValues, toValues);
        copyString(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME, fromValues, toValues);
        return toValues;
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }
}
