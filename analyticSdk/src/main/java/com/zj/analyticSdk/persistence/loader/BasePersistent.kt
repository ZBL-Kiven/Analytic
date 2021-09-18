package com.zj.analyticSdk.persistence.loader

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.zj.analyticSdk.CALogs.printStackTrace
import com.zj.analyticSdk.persistence.DbParams

abstract class BasePersistent(protected val context: Context) {

    protected abstract fun query(uri: Uri, table: DbParams.DbUriMap, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor?

    protected abstract fun bulkInsert(uri: Uri?, table: DbParams.DbUriMap, values: Array<ContentValues>): Int

    protected abstract fun insert(uri: Uri?, table: DbParams.DbUriMap, values: ContentValues): Uri?

    abstract fun delete(uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int

    internal fun queryByUri(uri: Uri, table: DbParams.DbUriMap, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        var cursor: Cursor? = null
        try {
            cursor = query(uri, table, projection, selection, selectionArgs, sortOrder)
        } catch (e: java.lang.Exception) {
            printStackTrace(e)
        }
        return if (cursor == null) {
            val column = table.path
            val matrixCursor = MatrixCursor(arrayOf(column))
            matrixCursor.addRow(arrayOf("null"))
            matrixCursor
        } else {
            cursor
        }
    }

    internal fun bulkInsertToUri(uri: Uri?, table: DbParams.DbUriMap, values: Array<ContentValues>?): Int {
        if (values.isNullOrEmpty()) {
            return 0
        }
        return bulkInsert(uri, table, values)
    }

    internal fun insertToUri(uri: Uri?, table: DbParams.DbUriMap, values: ContentValues?): Uri? {
        if (values == null || values.size() == 0) {
            return uri
        }
        try {
            return insert(uri, table, values)
        } catch (e: Exception) {
            printStackTrace(e)
        }
        return uri
    }

}