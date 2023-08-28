package com.zj.analyticSdk.recorder

import android.app.Application
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.utils.AppInfoUtils
import org.json.JSONObject
import java.util.*

internal object BasePropertyCollector {

    fun getBaseProperties(eventName: String): JSONObject {
        val context: Application = CCAnalytic.getApplication()
        val nameBuilder = CCAnalytic.getConfig().getEventNameBuilder()
        val params = JSONObject()
        params.put(nameBuilder.eventName(), eventName)
        params.put(nameBuilder.osType(), "Android")
        params.put(nameBuilder.eventId(), UUID.randomUUID().toString())
        params.put(nameBuilder.eventTime(), "${System.currentTimeMillis()}")
        params.put(nameBuilder.appVersionName(), AppInfoUtils.getAppVersionName(context))
        return params
    }

}