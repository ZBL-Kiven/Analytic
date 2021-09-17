package com.zj.analyticSdk.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zj.analyticSdk.CALogs;

class DataDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "CCA.SQLiteOpenHelper";
    private static final String CREATE_EVENTS_TABLE = String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL);", DbParams.TABLE_EVENTS, DbParams.KEY_DATA, DbParams.KEY_CREATED_AT);
    private static final String EVENTS_TIME_INDEX = String.format("CREATE INDEX IF NOT EXISTS time_idx ON %s (%s);", DbParams.TABLE_EVENTS, DbParams.KEY_CREATED_AT);

    DataDBHelper(Context context) {
        super(context, DbParams.DATABASE_NAME, null, DbParams.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CALogs.i(TAG, "Creating a new Analytics DB", null);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(EVENTS_TIME_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CALogs.i(TAG, "Upgrading app, replacing Analytics DB", null);
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", DbParams.TABLE_EVENTS));
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(EVENTS_TIME_INDEX);
    }
}
