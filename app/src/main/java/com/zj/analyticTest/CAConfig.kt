package com.zj.analyticTest

import android.util.Log
import com.zj.analyticSdk.CAConfigs
import org.json.JSONObject


object CAConfig : CAConfigs {

    override val maxCacheSize: Long = 10 * 1024 * 1024
    override val isLogEnabled: () -> Boolean = { true }
    override val isDebugEnabled: () -> Boolean = { true }
    override val uploadInterval: Long = 10000L
    override val uploadMaxSize: Int = 5
    override fun autoUploadAble(): Boolean = true

    //服务器地址
    override fun getServerUrl(): String {
        return "https://data.ccdev.lerjin.com/track_new?access_token=6228feb1-2a09-432a-a923-bc6aaf89f91e"
    }

    //针对特殊数据的处理或者需要添加一些新的参数时。
    override fun addDefaultParam(eventName: String, withData: Any?, baseProperties: JSONObject): JSONObject {
        return super.addDefaultParam(eventName, withData, baseProperties)
    }

    //当数据完成所有构建即将入库。
    override fun beforeEvent(eventName: String, properties: JSONObject): JSONObject? {
        Log.e("on-cc analytic --- ", properties.toString())
        return super.beforeEvent(eventName, properties)
    }

}