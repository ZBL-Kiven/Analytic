package com.zj.analyticSdk.persistence.operator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.persistence.DbParams
import org.json.JSONObject

internal class PersistentDataOperation(context: Context?) : DataOperation(context!!) {

    override fun queryData(uri: Uri, limit: Int): Array<String?>? {
        return handleQueryUri(uri)
    }

    override fun insertData(uri: Uri, jsonObject: JSONObject): Int {
        return handleInsertUri(uri, jsonObject)
    }

    override fun insertData(uri: Uri, contentValues: ContentValues): Int {
        return 0
    }

    private fun handleInsertUri(uri: Uri?, jsonObject: JSONObject): Int {
        if (uri == null) return -1
        val contentValues = ContentValues()
        contentValues.put(DbParams.matchWith(uri).path, jsonObject.toString())
        withCR { it.insert(uri, contentValues) }
        return 0
    }

    private fun handleQueryUri(uri: Uri?): Array<String?>? {
        if (uri == null || uri.path.isNullOrEmpty()) return null
        return withCR {
            var cursor: Cursor? = null
            try {
                cursor = it.query(uri, null, null, null, null)
                if (cursor != null && cursor.count > 0) {
                    cursor.moveToNext()
                    arrayOf(cursor.getString(0))
                } else null
            } catch (ex: Exception) {
                CALogs.printStackTrace(ex);null
            } finally {
                cursor?.close()
            }
        }
    }
}