package com.zj.analyticSdk.persistence.operator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.TextUtils
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.persistence.DbParams
import org.json.JSONObject

internal class EventDataOperation(mContext: Context) : DataOperation(mContext) {

    private var tag = this.javaClass.simpleName

    override fun insertData(uri: Uri, jsonObject: JSONObject): Int {
        try {
            if (deleteDataLowMemory(uri) != 0) {
                return DbParams.DB_OUT_OF_MEMORY_ERROR
            }
            val cv = ContentValues()
            cv.put(DbParams.KEY_DATA, jsonObject.toString() + "\t" + jsonObject.toString().hashCode())
            cv.put(DbParams.KEY_CREATED_AT, System.currentTimeMillis())
            withCR { it.insert(uri, cv) }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
        return 0
    }

    override fun insertData(uri: Uri, contentValues: ContentValues): Int {
        try {
            if (deleteDataLowMemory(uri) != 0) {
                return DbParams.DB_OUT_OF_MEMORY_ERROR
            }
            withCR { it.insert(uri, contentValues) }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
        return 0
    }

    override fun queryData(uri: Uri, limit: Int): Array<String?>? {
        var data: String? = null
        var lastId: String? = null
        withCR {
            var cursor: Cursor? = null
            try {
                cursor = it.query(uri, null, null, null, DbParams.KEY_CREATED_AT + " ASC LIMIT " + limit)
                if (cursor != null) {
                    val dataBuilder = StringBuilder()
                    val flushTime = ",\"_flush_time\":"
                    var suffix = ","
                    dataBuilder.append("[")
                    var keyData: String
                    while (cursor.moveToNext()) {
                        if (cursor.isLast) {
                            suffix = "]"
                            val id = cursor.getColumnIndex("_id")
                            lastId = cursor.getString(id)
                        }
                        try {
                            val index = cursor.getColumnIndex(DbParams.KEY_DATA)
                            keyData = cursor.getString(index)
                            keyData = parseData(keyData)
                            if (!TextUtils.isEmpty(keyData)) {
                                dataBuilder.append(keyData, 0, keyData.length - 1).append(flushTime).append(System.currentTimeMillis()).append("}").append(suffix)
                            }
                        } catch (e: Exception) {
                            CALogs.printStackTrace(e)
                            if (cursor.isLast) {
                                if (dataBuilder.length == 1) {
                                    dataBuilder.append("]")
                                } else {
                                    val lastOf = dataBuilder[dataBuilder.length - 1]
                                    if (lastOf == ',') {
                                        dataBuilder.replace(dataBuilder.length - 1, dataBuilder.length, "]")
                                    }
                                }
                            }
                        }
                    }
                    data = dataBuilder.toString()
                }

            } catch (e: SQLiteException) {
                CALogs.e(CAConfigs.LOG_SYSTEM, tag, "Could not pull records for data out of database events. Waiting to send.", e)
                lastId = null
                data = null
            } finally {
                cursor?.close()
            }
        }
        return if (lastId != null) {
            arrayOf(lastId, data ?: "", DbParams.GZIP_DATA_EVENT)
        } else null
    }
}