package com.zj.analyticSdk.persistence;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.zj.analyticSdk.CALogs;
import com.zj.analyticSdk.persistence.loader.PersistentLoader;
import com.zj.analyticSdk.persistence.data.*;


public class CCAnalyticsContentProvider extends ContentProvider {
    private final static int EVENTS = 1;
    private final static int APP_START_TIME = 2;
    private final static int APP_END_TIME = 3;
    private final static int LOGIN_ID = 4;
    private final static int DEVICE_ID = 5;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DataDBHelper dbHelper;
    private PersistentAppStartTime persistentAppStartTime;
    private PersistentAppEnd persistentAppEnd;
    private PersistentLoginId persistentLoginId;
    private PersistentDeviceId persistentDeviceId;

    private boolean isDbWritable = true;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            String packageName;
            try {
                packageName = context.getApplicationContext().getPackageName();
            } catch (UnsupportedOperationException e) {
                packageName = "com.zj.analyticSdk";
            }
            String authority = packageName + ".CCAnalyticsContentProvider";
            uriMatcher.addURI(authority, DbParams.TABLE_EVENTS, EVENTS);
            uriMatcher.addURI(authority, DbParams.TABLE_APP_START_TIME, APP_START_TIME);
            uriMatcher.addURI(authority, DbParams.TABLE_APP_END_TIME, APP_END_TIME);
            uriMatcher.addURI(authority, DbParams.TABLE_LOGIN_ID, LOGIN_ID);
            uriMatcher.addURI(authority, DbParams.TABLE_DEVICE_ID, DEVICE_ID);
            dbHelper = new DataDBHelper(context);
            PersistentLoader.initLoader(context);
            persistentAppStartTime = (PersistentAppStartTime) PersistentLoader.loadPersistent(DbParams.TABLE_APP_START_TIME);
            persistentAppEnd = (PersistentAppEnd) PersistentLoader.loadPersistent(DbParams.TABLE_APP_END_TIME);
            persistentLoginId = (PersistentLoginId) PersistentLoader.loadPersistent(DbParams.TABLE_LOGIN_ID);
            persistentDeviceId = (PersistentDeviceId) PersistentLoader.loadPersistent(DbParams.TABLE_DEVICE_ID);
        }
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!isDbWritable) {
            return 0;
        }
        int deletedCounts = 0;
        try {
            int code = uriMatcher.match(uri);
            if (EVENTS == code) {
                try {
                    SQLiteDatabase database = dbHelper.getWritableDatabase();
                    deletedCounts = database.delete(DbParams.TABLE_EVENTS, selection, selectionArgs);
                } catch (SQLiteException e) {
                    isDbWritable = false;
                    CALogs.printStackTrace(e);
                }
            }
        } catch (Exception e) {
            CALogs.printStackTrace(e);
        }
        return deletedCounts;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (!isDbWritable || values == null || values.size() == 0) {
            return uri;
        }
        try {
            int code = uriMatcher.match(uri);

            if (code == EVENTS) {
                return insertEvent(uri, values);
            } else {
                insert(code, values);
            }
            return uri;
        } catch (Exception e) {
            CALogs.printStackTrace(e);
        }
        return uri;
    }

    private Uri insertEvent(Uri uri, ContentValues values) {
        SQLiteDatabase database;
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            isDbWritable = false;
            CALogs.printStackTrace(e);
            return uri;
        }
        if (!values.containsKey(DbParams.KEY_DATA) || !values.containsKey(DbParams.KEY_CREATED_AT)) {
            return uri;
        }
        long d = database.insert(DbParams.TABLE_EVENTS, "_id", values);
        return ContentUris.withAppendedId(uri, d);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (!isDbWritable) {
            return 0;
        }
        int numValues;
        SQLiteDatabase database = null;
        try {
            try {
                database = dbHelper.getWritableDatabase();
            } catch (SQLiteException e) {
                isDbWritable = false;
                CALogs.printStackTrace(e);
                return 0;
            }
            database.beginTransaction();
            numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                insert(uri, values[i]);
            }
            database.setTransactionSuccessful();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return numValues;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!isDbWritable) {
            return null;
        }
        Cursor cursor = null;
        try {
            int code = uriMatcher.match(uri);
            if (code == EVENTS) {
                cursor = queryByTable(projection, selection, selectionArgs, sortOrder);
            } else {
                cursor = query(code);
            }
        } catch (Exception e) {
            CALogs.printStackTrace(e);
        }
        return cursor;
    }

    private Cursor queryByTable(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getWritableDatabase().query(DbParams.TABLE_EVENTS, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (SQLiteException e) {
            isDbWritable = false;
            CALogs.printStackTrace(e);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * insert 处理
     *
     * @param code   Uri code
     * @param values ContentValues
     */
    private void insert(int code, ContentValues values) {
        switch (code) {
            case APP_START_TIME:
                persistentAppStartTime.commit(values.getAsLong(DbParams.TABLE_APP_START_TIME));
                break;
            case APP_END_TIME:
                persistentAppEnd.commit(values.getAsLong(DbParams.TABLE_APP_END_TIME));
                break;
            case LOGIN_ID:
                persistentLoginId.commit(values.getAsString(DbParams.TABLE_LOGIN_ID));
                break;
            case DEVICE_ID:
                persistentDeviceId.commit(values.getAsString(DbParams.TABLE_DEVICE_ID));
                break;
            default:
                break;
        }
    }

    /**
     * query 处理
     *
     * @param code Uri code
     * @return Cursor
     */
    private Cursor query(int code) {
        String column = null;
        Object data = null;
        switch (code) {
            case APP_START_TIME:
                data = persistentAppStartTime.get();
                column = DbParams.TABLE_APP_START_TIME;
                break;
            case APP_END_TIME:
                data = persistentAppEnd.get();
                column = DbParams.TABLE_APP_END_TIME;
                break;
            case LOGIN_ID:
                data = persistentLoginId.get();
                column = DbParams.TABLE_LOGIN_ID;
                break;
            case DEVICE_ID:
                data = persistentDeviceId.get();
                column = DbParams.TABLE_DEVICE_ID;
                break;
            default:
                break;
        }
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{column});
        matrixCursor.addRow(new Object[]{data});
        return matrixCursor;
    }
}
