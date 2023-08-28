package com.zj.analyticSdk.persistence.loader

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.persistence.DataDBHelper
import com.zj.analyticSdk.persistence.DbParams

internal class PersistentLoader(context: Context, private val tabName: String) : BasePersistent(context) {

    private var dbHelper: DataDBHelper? = null
    private var isDbWritable = true

    init {
        if (dbHelper == null) dbHelper = DataDBHelper(context)
    }

    override fun query(uri: Uri, table: DbParams.DbUriMap, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor = dbHelper?.writableDatabase?.query(tabName, projection, selection, selectionArgs, null, null, sortOrder)
        } catch (e: SQLiteException) {
            isDbWritable = false
            CALogs.printStackTrace(e)
        }
        return cursor
    }

    override fun bulkInsert(uri: Uri?, table: DbParams.DbUriMap, values: Array<ContentValues>): Int {
        if (!isDbWritable) {
            return 0
        }
        var numValues = 0
        var database: SQLiteDatabase? = null
        try {
            try {
                database = dbHelper?.writableDatabase
            } catch (e: SQLiteException) {
                isDbWritable = false
                CALogs.printStackTrace(e)
                return 0
            }
            database?.beginTransaction()
            values.forEach {
                insert(uri, table, it)
                numValues++
            }
            database?.setTransactionSuccessful()
        } finally {
            database?.endTransaction()
        }
        return numValues
    }

    override fun insert(uri: Uri?, table: DbParams.DbUriMap, values: ContentValues): Uri? {
        if (!isDbWritable) return uri
        val database: SQLiteDatabase?
        try {
            database = dbHelper?.writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            CALogs.printStackTrace(e)
            return uri
        }
        if (!values.containsKey(DbParams.KEY_DATA) || !values.containsKey(DbParams.KEY_CREATED_AT)) {
            return uri
        }
        val d = database?.insert(tabName, "_id", values)
        if (uri == null || d == null) return uri
        return ContentUris.withAppendedId(uri, d)
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int {
        if (!isDbWritable || uri == null) {
            return 0
        }
        var deletedCounts = 0
        try {
            if (DbParams.matchWith(uri) === DbParams.DbUriMap.TAB_EVENT) {
                try {
                    val database = dbHelper!!.writableDatabase
                    deletedCounts = database.delete(tabName, selection, selectionArgs)
                } catch (e: SQLiteException) {
                    isDbWritable = false
                    CALogs.printStackTrace(e)
                }
            }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
        return deletedCounts
    }
}