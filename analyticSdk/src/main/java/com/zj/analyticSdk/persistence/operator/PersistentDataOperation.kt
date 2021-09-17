package com.zj.analyticSdk.persistence.operator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
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
        var path = uri.path
        if (!path.isNullOrEmpty()) {
            path = path.substring(1)
            when (path) {
                DbParams.TABLE_APP_END_TIME -> contentValues.put(DbParams.TABLE_APP_END_TIME, jsonObject.optLong(DbParams.VALUE))
                DbParams.TABLE_DEVICE_ID -> contentValues.put(DbParams.TABLE_DEVICE_ID, jsonObject.optString(DbParams.VALUE))
                DbParams.TABLE_APP_START_TIME -> contentValues.put(DbParams.TABLE_APP_START_TIME, jsonObject.optLong(DbParams.VALUE))
                DbParams.TABLE_LOGIN_ID -> contentValues.put(DbParams.TABLE_LOGIN_ID, jsonObject.optString(DbParams.VALUE))
                else -> return -1
            }
            contentResolver.insert(uri, contentValues)
        }
        return 0
    }

    private fun handleQueryUri(uri: Uri?): Array<String?>? {
        if (uri == null) return null
        var path = uri.path
        if (TextUtils.isEmpty(path)) return null
        var cursor: Cursor? = null
        try {
            path = path?.substring(1)
            cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToNext()
                return when (path) {
                    DbParams.TABLE_LOGIN_ID, DbParams.TABLE_DEVICE_ID -> arrayOf(cursor.getString(0))
                    DbParams.TABLE_APP_END_TIME, DbParams.TABLE_APP_START_TIME -> arrayOf(cursor.getLong(0).toString())
                    else -> null
                }
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        } finally {
            cursor?.close()
        }
        return null
    }
}