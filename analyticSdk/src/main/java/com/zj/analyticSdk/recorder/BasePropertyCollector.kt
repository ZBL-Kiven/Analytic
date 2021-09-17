package com.zj.analyticSdk.recorder

import android.app.Application
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.utils.AppInfoUtils
import org.json.JSONObject
import java.util.*

internal object BasePropertyCollector {

    fun getBaseProperties(eventName: String): JSONObject {
        val context: Application = CCAnalytic.getApplication()
        val nameBuilder = CCAnalytic.getConfig().getEventNameBuilder()
        val params = JSONObject()
        try {
            AppUtils.parseCurActParamInfo(params)
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
        params.put(nameBuilder.osType(), "Android")
        params.put(nameBuilder.eventName(), eventName)
        params.put(nameBuilder.appVersionName(), AppInfoUtils.getAppVersionName(context))
        params.put(nameBuilder.eventTime(), "${System.currentTimeMillis()}")
        params.put(nameBuilder.eventId(), UUID.randomUUID().toString())
        return params
    }

}