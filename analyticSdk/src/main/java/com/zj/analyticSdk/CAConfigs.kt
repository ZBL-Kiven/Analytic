package com.zj.analyticSdk

import com.zj.analyticSdk.utils.JOUtils
import com.zj.analyticSdk.utils.NetworkUtils
import org.json.JSONObject
import javax.net.ssl.SSLSocketFactory

interface CAConfigs {

    val maxCacheSize: Long

    val isLogEnabled: () -> Boolean

    val isDebugEnabled: () -> Boolean

    val uploadInterval: Long

    val uploadMaxSize: Int

    fun getServerUrl(): String?

    fun autoUploadAble(): Boolean = true

    fun isNetworkRequestEnable(): Boolean = true

    fun getSSLSocketFactory(): SSLSocketFactory? = null

    fun addDefaultParam(eventName: String, withData: Any?, baseProperties: JSONObject): JSONObject {
        return baseProperties
    }

    fun onMergeProperties(eventName: String, source: JSONObject, dest: JSONObject): JSONObject? {
        return JOUtils.mergeJSONObject(source, dest)
    }

    fun beforeEvent(eventName: String, properties: JSONObject): JSONObject? {
        return properties
    }

    fun getNetworkFlushPolicy(): Int {
        return NetworkUtils.NetworkType.TYPE_3G or NetworkUtils.NetworkType.TYPE_4G or NetworkUtils.NetworkType.TYPE_WIFI or NetworkUtils.NetworkType.TYPE_5G
    }

    fun getEventNameBuilder(): EventNameBuilder {
        return DefaultEventNameBuilder
    }
}