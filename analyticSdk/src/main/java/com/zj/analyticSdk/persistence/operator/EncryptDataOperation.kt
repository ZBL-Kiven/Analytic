package com.zj.analyticSdk.persistence.operator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.zj.analyticSdk.CALogs.printStackTrace
import com.zj.analyticSdk.persistence.DbParams
import com.zj.analyticSdk.persistence.encrypt.CCAnalyticsEncrypt
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

internal class EncryptDataOperation(context: Context?, private val dataEncrypt: CCAnalyticsEncrypt) : DataOperation(context!!) {

    override fun insertData(uri: Uri, jsonObject: JSONObject): Int {
        try {
            if (deleteDataLowMemory(uri) != 0) {
                return DbParams.DB_OUT_OF_MEMORY_ERROR
            }
            val obj = dataEncrypt.encryptTrackData(jsonObject)
            val cv = ContentValues()
            cv.put(DbParams.KEY_DATA, obj.toString() + "\t" + obj.toString().hashCode())
            cv.put(DbParams.KEY_CREATED_AT, System.currentTimeMillis())
            withCR { it.insert(uri, cv) }
        } catch (e: Exception) {
            printStackTrace(e)
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
            printStackTrace(e)
        }
        return 0
    }

    override fun queryData(uri: Uri, limit: Int): Array<String?>? {
        var data: String? = null
        var lastId: String? = null
        var gzipType = DbParams.GZIP_DATA_ENCRYPT
        withCR {
            var cursor: Cursor? = null
            try {
                val dataEncryptMap: MutableMap<String, JSONArray> = HashMap()
                val dataJsonArray = JSONArray()
                cursor = it.query(uri, null, null, null, DbParams.KEY_CREATED_AT + " ASC LIMIT " + limit)
                if (cursor != null) {
                    var keyData: String?
                    var jsonObject: JSONObject
                    val eKey = "eKey"
                    val keyVer = "pkv"
                    val payloads = "payloads"
                    while (cursor.moveToNext()) {
                        if (cursor.isLast) {
                            lastId = cursor.getString(cursor.getColumnIndex("_id"))
                        }
                        try {
                            keyData = cursor.getString(cursor.getColumnIndex(DbParams.KEY_DATA))
                            keyData = parseData(keyData)
                            if (TextUtils.isEmpty(keyData)) {
                                continue
                            }
                            jsonObject = JSONObject(keyData)
                            val isHasEKey = jsonObject.has(eKey)
                            if (!isHasEKey) { // 如果没有包含 eKey 字段，则重新进行加密
                                jsonObject = dataEncrypt.encryptTrackData(jsonObject)
                            }
                            if (jsonObject.has(eKey)) {
                                val key = jsonObject.getString(eKey) + "$" + jsonObject.getInt(keyVer)
                                if (dataEncryptMap.containsKey(key)) {
                                    dataEncryptMap[key]!!.put(jsonObject.getString(payloads))
                                } else {
                                    val jsonArray = JSONArray()
                                    jsonArray.put(jsonObject.getString(payloads))
                                    dataEncryptMap[key] = jsonArray
                                }
                            } else {
                                dataJsonArray.put(jsonObject)
                            }
                        } catch (e: Exception) {
                            printStackTrace(e)
                        }
                    }
                    val dataEncryptJsonArray = JSONArray()
                    for (key in dataEncryptMap.keys) {
                        jsonObject = JSONObject()
                        jsonObject.put(eKey, key.substring(0, key.indexOf("$")))
                        jsonObject.put(keyVer, Integer.valueOf(key.substring(key.indexOf("$") + 1)))
                        jsonObject.put(payloads, dataEncryptMap[key])
                        jsonObject.put("flush_time", System.currentTimeMillis())
                        dataEncryptJsonArray.put(jsonObject)
                    }
                    if (dataEncryptJsonArray.length() > 0) {
                        data = dataEncryptJsonArray.toString()
                    } else {
                        data = dataJsonArray.toString()
                        gzipType = DbParams.GZIP_DATA_EVENT
                    }
                }
            } catch (ex: Exception) {
                printStackTrace(ex)
            } finally {
                cursor?.close()
            }
        }
        return lastId?.let { arrayOf(it, data ?: "", gzipType) }
    }
}