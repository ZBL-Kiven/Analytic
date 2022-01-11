package com.zj.analyticSdk.persistence.operator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.persistence.DbParams
import org.json.JSONObject
import java.io.File

internal abstract class DataOperation(mContext: Context) {

    private var tag = "EventDataOperation"

    private var contentResolver: ContentResolver = mContext.contentResolver

    private val mDatabaseFile: File = mContext.getDatabasePath(DbParams.DATABASE_NAME)

    abstract fun insertData(uri: Uri, jsonObject: JSONObject): Int

    abstract fun insertData(uri: Uri, contentValues: ContentValues): Int

    abstract fun queryData(uri: Uri, limit: Int): Array<String?>?

    @JvmOverloads
    fun queryDataCount(uri: Uri, selection: String? = null, selectionArgs: Array<String?>? = null): Int {
        try {
            contentResolver.query(uri, null, selection, selectionArgs, null).use { cursor ->
                if (cursor != null) {
                    return cursor.count
                }
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return 0
    }

    open fun deleteData(uri: Uri, id: String) {
        try {
            if (DbParams.DB_DELETE_ALL == id) {
                contentResolver.delete(uri, null, null)
            } else {
                contentResolver.delete(uri, "_id <= ?", arrayOf(id))
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
    }

    fun parseData(kd: String): String {
        var keyData = kd
        try {
            if (TextUtils.isEmpty(keyData)) return ""
            val index = keyData.lastIndexOf("\t")
            if (index > -1) {
                val crc = keyData.substring(index).replaceFirst("\t".toRegex(), "")
                keyData = keyData.substring(0, index)
                if (TextUtils.isEmpty(keyData) || TextUtils.isEmpty(crc) || crc != keyData.hashCode().toString()) {
                    return ""
                }
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return keyData
    }

    /**
     * Delete data when the database is full
     * @param uri URI
     * @return Normally returns 0
     */
    fun deleteDataLowMemory(uri: Uri): Int {
        if (belowMemThreshold()) {
            CALogs.i(CAConfigs.LOG_ALL, tag, "There is not enough space left on the device to store events, so will delete 100 oldest events")
            val eventsData = queryData(uri, 100) ?: return DbParams.DB_OUT_OF_MEMORY_ERROR
            eventsData[0]?.let {
                deleteData(uri, it)
            }
            if (queryDataCount(uri) <= 0) {
                return DbParams.DB_OUT_OF_MEMORY_ERROR
            }
        }
        return 0
    }

    protected fun <R> withCR(run: (ContentResolver) -> R?): R? {
        return synchronized(contentResolver) {
            run(contentResolver)
        }
    }

    private fun getMaxCacheSize(): Long {
        return try {
            CCAnalytic.getConfig().maxCacheSize
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
            32 * 1024 * 1024
        }
    }

    private fun belowMemThreshold(): Boolean {
        return if (mDatabaseFile.exists()) {
            mDatabaseFile.length() >= getMaxCacheSize()
        } else false
    }
}