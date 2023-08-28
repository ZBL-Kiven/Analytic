package com.zj.analyticSdk.core.data

import android.net.Uri
import android.text.TextUtils
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.exceptions.ConnectErrorException
import com.zj.analyticSdk.core.exceptions.ResponseErrorException
import com.zj.analyticSdk.utils.JSONUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.net.ssl.HttpsURLConnection

object UpdateHttpTask {

    private const val TAG = "CCA.UpdateHttpTask"

    fun sendHttpRequest(path: String?, data: String?, gzip: String?, rawMessage: String?, isRedirects: Boolean) {
        var connection: HttpURLConnection? = null
        var mIs: InputStream? = null
        var out: OutputStream? = null
        var bout: BufferedOutputStream? = null
        try {
            val url = URL(path)
            connection = url.openConnection() as? HttpURLConnection
            if (connection == null) {
                CALogs.e(CAConfigs.LOG_SYSTEM, TAG, String.format("can not connect %s, it shouldn't happen", url.toString()), null)
                return
            }
            val sslFactory = CCAnalytic.getConfig().getSSLSocketFactory()
            if (sslFactory != null && connection is HttpsURLConnection) {
                connection.sslSocketFactory = sslFactory
            }
            connection.instanceFollowRedirects = false
            val builder = Uri.Builder()
            val s = System.currentTimeMillis().toString()
            if (!TextUtils.isEmpty(data)) {
                builder.appendQueryParameter("crc", String.format("%scc%s", data, s).hashCode().toString())
            }
            builder.appendQueryParameter("time", s)
            builder.appendQueryParameter("gzip", gzip)
            builder.appendQueryParameter("data_list", data)
            val query = builder.build().encodedQuery
            if (TextUtils.isEmpty(query)) {
                return
            }
            connection.setFixedLengthStreamingMode(query?.toByteArray(StandardCharsets.UTF_8)?.size ?: 0)
            connection.doOutput = true
            connection.requestMethod = "POST"
            out = connection.outputStream
            bout = BufferedOutputStream(out)
            bout.write(query?.toByteArray(StandardCharsets.UTF_8))
            bout.flush()
            val responseCode = connection.responseCode
            CALogs.i(CAConfigs.LOG_UPLOAD, TAG, "responseCode: $responseCode")
            if (!isRedirects && needRedirects(responseCode)) {
                val location = getLocation(connection, path)
                if (!location.isNullOrEmpty()) {
                    closeStream(bout, out, null, connection)
                    sendHttpRequest(location, data, gzip, rawMessage, true)
                    return
                }
            }
            mIs = try {
                connection.inputStream
            } catch (e: FileNotFoundException) {
                connection.errorStream
            }
            val responseBody = slurp(mIs)
            mIs?.close()
            mIs = null
            if (responseBody == null) throw ConnectErrorException("flush failure ,the response body is null!!")
            val response = String(responseBody, StandardCharsets.UTF_8)
            if (CCAnalytic.getConfig().isDebugEnabled()) {
                val jsonMessage: String = JSONUtils.formatJson(rawMessage) // Status code 200-300 are considered correct
                if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                    CALogs.i(CAConfigs.LOG_SYSTEM, TAG, "valid message: \n$jsonMessage")
                } else {
                    CALogs.i(CAConfigs.LOG_SYSTEM, TAG, "invalid message: \n$jsonMessage")
                    CALogs.i(CAConfigs.LOG_SYSTEM, TAG, String.format(Locale.ENGLISH, "ret_code: %d", responseCode))
                    CALogs.i(CAConfigs.LOG_SYSTEM, TAG, String.format(Locale.ENGLISH, "ret_content: %s", response))
                }
            }
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) { // 校验错误
                throw ResponseErrorException(String.format("flush failure with response '%s', the response code is '%d'", response, responseCode), responseCode)
            }
        } catch (e: IOException) {
            throw ConnectErrorException(e)
        } finally {
            closeStream(bout, out, mIs, connection)
        }
    }

    private fun needRedirects(responseCode: Int): Boolean {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307
    }

    @Throws(MalformedURLException::class)
    private fun getLocation(connection: HttpURLConnection?, path: String?): String? {
        if (connection == null || TextUtils.isEmpty(path)) {
            return null
        }
        var location = connection.getHeaderField("Location")
        if (TextUtils.isEmpty(location)) {
            location = connection.getHeaderField("location")
        }
        if (TextUtils.isEmpty(location)) {
            return null
        }
        if (!(location.startsWith("http://") || location.startsWith("https://"))) {

            //Sometimes the host will be omitted and only the path behind will be returned, so the url needs to be completed
            val originUrl = URL(path)
            location = (originUrl.protocol + "://" + originUrl.host + location)
        }
        return location
    }

    private fun closeStream(bout: BufferedOutputStream?, out: OutputStream?, `in`: InputStream?, connection: HttpURLConnection?) {
        if (null != bout) {
            try {
                bout.close()
            } catch (e: Exception) {
                CALogs.e(CAConfigs.LOG_SYSTEM, TAG, e.message)
            }
        }
        if (null != out) {
            try {
                out.close()
            } catch (e: Exception) {
                CALogs.e(CAConfigs.LOG_SYSTEM, TAG, e.message)
            }
        }
        if (null != `in`) {
            try {
                `in`.close()
            } catch (e: Exception) {
                CALogs.e(CAConfigs.LOG_SYSTEM, TAG, e.message)
            }
        }
        if (null != connection) {
            try {
                connection.disconnect()
            } catch (e: Exception) {
                CALogs.e(CAConfigs.LOG_SYSTEM, TAG, e.message)
            }
        }
    }

    @Throws(IOException::class)
    private fun slurp(inputStream: InputStream?): ByteArray? {
        if (inputStream == null) return null
        val buffer = ByteArrayOutputStream()
        var nRead: Int
        val data = ByteArray(8192)
        while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        buffer.flush()
        return buffer.toByteArray()
    }

}