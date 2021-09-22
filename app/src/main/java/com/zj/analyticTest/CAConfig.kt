package com.zj.analyticTest

import com.zj.analyticSdk.CAConfigs
import org.json.JSONObject


object CAConfig : CAConfigs {

    override val maxCacheSize: Long = 10 * 1024 * 1024
    override val isLogEnabled: () -> Boolean = { true }
    override val isDebugEnabled: () -> Boolean = { true }
    override val uploadInterval: Long = 10000L
    override val uploadMaxSize: Int = 5

    override fun getServerUrl(): String {
        return "http://192.168.50.212:8081/track_new?access_token=6228feb1-2a09-432a-a923-bc6aaf89f91e"
    }


    override fun getEventParams(eventName: String, withData: Any?, baseProperties: JSONObject): JSONObject {
        return super.getEventParams(eventName, withData, baseProperties)
    }

    override fun beforeEvent(properties: JSONObject): JSONObject? {
        return super.beforeEvent(properties)
    }

}