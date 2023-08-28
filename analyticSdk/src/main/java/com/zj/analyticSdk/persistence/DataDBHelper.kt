package com.zj.analyticSdk.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs.i

internal class DataDBHelper(context: Context?) : SQLiteOpenHelper(context, DbParams.DATABASE_NAME, null, DbParams.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        i(CAConfigs.LOG_ALL, TAG, "Creating a new Analytics DB", null)
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        i(CAConfigs.LOG_ALL, TAG, "Upgrading app, replacing Analytics DB", null)
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", DbParams.TABLE_EVENTS))
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }

    companion object {
        private const val TAG = "CCA.SQLiteOpenHelper"
        private val CREATE_EVENTS_TABLE = String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL);", DbParams.TABLE_EVENTS, DbParams.KEY_DATA, DbParams.KEY_CREATED_AT)
        private val EVENTS_TIME_INDEX = String.format("CREATE INDEX IF NOT EXISTS time_idx ON %s (%s);", DbParams.TABLE_EVENTS, DbParams.KEY_CREATED_AT)
    }
}