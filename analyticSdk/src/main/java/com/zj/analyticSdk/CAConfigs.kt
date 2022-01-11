package com.zj.analyticSdk

import com.zj.analyticSdk.utils.JOUtils
import com.zj.analyticSdk.utils.NetworkUtils
import org.json.JSONObject
import javax.net.ssl.SSLSocketFactory

interface CAConfigs {

    companion object {

        const val LOG_SYSTEM = 0

        const val LOG_RECORD = 1

        const val LOG_INTERMITTENT_RECORD = 2

        const val LOG_INTERMITTENT_FLUSH = 4

        const val LOG_BEFORE_EVENT = 8

        const val LOG_UPLOAD = 16

        const val LOG_ALL = 32
    }

    val maxCacheSize: Long

    val isLogEnabled: () -> Boolean

    val isDebugEnabled: () -> Boolean

    val uploadInterval: Long

    val uploadMaxSize: Int

    fun getServerUrl(): String?

    fun logFrequency(): Int = LOG_ALL

    fun autoUploadAble(): Boolean = true

    fun isNetworkRequestEnable(): Boolean = true

    fun getSSLSocketFactory(): SSLSocketFactory? = null

    /**
     * how to build the default properties ,with your [withData]
     * @see CCAnalytic.trackEvent
     * */
    fun addDefaultParam(eventName: String, withData: Any?, baseProperties: JSONObject): JSONObject {
        return baseProperties
    }

    /**
     * how to merge your properties [source] to [dest]
     * */
    fun onMergeProperties(eventName: String, source: JSONObject, dest: JSONObject): JSONObject? {
        return JOUtils.mergeJSONObject(source, dest)
    }

    /**
     * the data will be record to database after callback, if it returned not empty.
     * */
    fun beforeEvent(eventName: String, properties: JSONObject): JSONObject? {
        return properties
    }

    /**
     * if [autoUploadAble] ,this is configuration the policy to allowed auto upload with which network type.
     * */
    fun getNetworkFlushPolicy(): Int {
        return NetworkUtils.NetworkType.TYPE_3G or NetworkUtils.NetworkType.TYPE_4G or NetworkUtils.NetworkType.TYPE_WIFI or NetworkUtils.NetworkType.TYPE_5G
    }

    /**
     * for overridden to custom the default or auto-track events name
     * @see EventNameBuilder
     */
    fun getEventNameBuilder(): EventNameBuilder {
        return DefaultEventNameBuilder
    }
}