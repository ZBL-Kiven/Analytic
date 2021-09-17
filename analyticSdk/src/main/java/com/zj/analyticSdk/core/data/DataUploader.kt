package com.zj.analyticSdk.core.data

import android.content.Context
import android.text.TextUtils
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.data.UpdateHttpTask.sendHttpRequest
import com.zj.analyticSdk.core.exceptions.ConnectErrorException
import com.zj.analyticSdk.core.exceptions.InvalidDataException
import com.zj.analyticSdk.core.exceptions.ResponseErrorException
import com.zj.analyticSdk.utils.NetworkUtils
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.persistence.DbParams
import com.zj.analyticSdk.persistence.encrypt.Base64Coder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import java.util.zip.GZIPOutputStream

class DataUploader(private val mContext: Context) {

    companion object {
        private const val TAG = "CCA.DataUploader"
    }

    private val mDbAdapter = DBHelper.getInstance()

    fun sendData(onUploaded: (isSuccess: Boolean) -> Unit) {
        var isSuccess = canSend()
        try {
            if (!isSuccess) return
            var count = 100
            while (count > 0) {
                var deleteEvents = true
                val eventsData = synchronized(mDbAdapter) {
                    mDbAdapter.generateDataString(50)
                }
                if (eventsData.isNullOrEmpty()) {
                    isSuccess = false
                    return
                }
                val lastId = eventsData[0]
                val rawMessage = eventsData[1]
                val gzip = eventsData[2]
                var errorMessage: String? = null
                try {
                    var data = rawMessage
                    if (DbParams.GZIP_DATA_EVENT == gzip) {
                        data = encodeData(rawMessage)
                    }
                    if (!TextUtils.isEmpty(data)) {
                        sendHttpRequest(CCAnalytic.getConfig().getServerUrl(), data, gzip, rawMessage, false)
                    }
                } catch (e: ConnectErrorException) {
                    deleteEvents = false
                    isSuccess = false
                    errorMessage = "Connection error: " + e.message
                } catch (e: InvalidDataException) {
                    isSuccess = false
                    errorMessage = "Invalid data: " + e.message
                } catch (e: ResponseErrorException) {
                    isSuccess = false
                    deleteEvents = isDeleteEventsByCode(e.httpCode)
                    errorMessage = "ResponseErrorException: " + e.message
                } catch (e: Exception) {
                    deleteEvents = false
                    isSuccess = false
                    errorMessage = "Exception: " + e.message
                } finally {
                    val isDebugMode: Boolean = CCAnalytic.getConfig().isDebugEnabled()
                    if (!TextUtils.isEmpty(errorMessage)) {
                        if (isDebugMode || CCAnalytic.getConfig().isLogEnabled()) {
                            CALogs.e(TAG, errorMessage)
                        }
                    }
                    if (deleteEvents) {
                        lastId?.let { count = mDbAdapter.cleanupEvents(it) }
                        CALogs.i(TAG, String.format(Locale.CHINA, "Events flushed. [left = %d]", count))
                    } else {
                        count = 0
                    }
                }
            }
        } catch (e: Exception) {
            isSuccess = false
            CALogs.printStackTrace(e)
        } finally {
            onUploaded(isSuccess)
        }
    }

    private fun encodeData(rawMessage: String?): String? {
        var gos: GZIPOutputStream? = null
        return try {
            val os = ByteArrayOutputStream(rawMessage?.toByteArray(Charsets.UTF_8)?.size ?: 0)
            gos = GZIPOutputStream(os)
            gos.write(rawMessage?.toByteArray(Charsets.UTF_8))
            gos.close()
            val compressed = os.toByteArray()
            os.close()
            String(Base64Coder.encode(compressed))
        } catch (exception: IOException) {
            throw InvalidDataException(exception)
        } finally {
            if (gos != null) {
                try {
                    gos.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    private fun canSend(): Boolean {
        if (!CCAnalytic.getConfig().isNetworkRequestEnable()) {
            CALogs.i(TAG, "NetworkRequest is closed！")
            return false
        }
        if (CCAnalytic.getConfig().getServerUrl().isNullOrEmpty()) {
            CALogs.i(TAG, "Server url is null or empty.")
            return false
        }
        if (!CCAnalytic.isMainProgress()) {
            CALogs.i(TAG, "cannot upload in other progress.")
            return false
        }
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            CALogs.i(TAG, "net work is available to send.")
            return false
        }
        val networkType: String = NetworkUtils.networkType(mContext)
        if (!NetworkUtils.isShouldFlush(networkType, CCAnalytic.getConfig().getNetworkFlushPolicy())) {
            CALogs.i(TAG, String.format("cannot send data with current network , type is %s！", networkType))
            return false
        }
        return true
    }

    private fun isDeleteEventsByCode(httpCode: Int): Boolean {
        var shouldDelete = true
        if (httpCode == HttpURLConnection.HTTP_NOT_FOUND || httpCode == HttpURLConnection.HTTP_FORBIDDEN || httpCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && httpCode < 600) {
            shouldDelete = false
        }
        return shouldDelete
    }

}