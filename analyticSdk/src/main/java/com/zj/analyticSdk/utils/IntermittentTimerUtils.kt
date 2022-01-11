package com.zj.analyticSdk.utils

import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.persistence.sp.SpUtils
import com.zj.analyticSdk.persistence.sp.SpUtils.pollIntermittent
import com.zj.analyticSdk.persistence.sp.SpUtils.saveObject
import org.json.JSONObject


internal object IntermittentTimerUtils {

    const val PROPER_NAME = "_properties_event_name"

    /**
     * enqueue recorder task for all of cached intermittent data event
     * call in [com.zj.analyticSdk.CCAnalytic.flushIntermittentInfo]
     * or [com.zj.analyticSdk.CCAnalytic.flushAllIntermittentInfo]
     * */
    fun flushIfExists(flushInfo: EventInfo.FlushInfo) {
        if (flushInfo.eventName.isNullOrEmpty()) {
            val data = SpUtils.pollAllIntermittent()
            data.forEach {
                val eventName = kotlin.runCatching {
                    it.optString(CCAnalytic.getConfig().getEventNameBuilder().eventName())
                }.getOrNull()
                if (!eventName.isNullOrEmpty()) {
                    CCAnalytic.getConfig().beforeEvent(eventName, it)?.let { param ->
                        DBHelper.getInstance().addJSON(param)
                        CALogs.i(CAConfigs.LOG_INTERMITTENT_FLUSH, "CCA.Intermittent.flushIfExists", param.toString(), null)
                    }
                }
            }
        } else {
            val data = flushInfo.eventName.pollIntermittent() ?: return
            CCAnalytic.getConfig().beforeEvent(flushInfo.eventName, data)?.let { param ->
                DBHelper.getInstance().addJSON(param)
                CALogs.i(CAConfigs.LOG_INTERMITTENT_FLUSH, "CCA.Intermittent.flushIfExists", param.toString(), null)
            }
        }
    }

    /**
     * @see [Boolean]
     * */
    fun putOrUpdate(event: String, buildParams: JSONObject): JSONObject {
        val enProp = getPropName(event)
        var obj = enProp.pollIntermittent()
        if (obj != null) {
            JOUtils.mergeJSONObject(buildParams, obj)
        } else {
            obj = buildParams
        }
        enProp.saveObject(obj)
        return obj
    }

    fun save(event: String, jo: JSONObject) {
        event saveObject jo
        CALogs.i(CAConfigs.LOG_INTERMITTENT_RECORD, "CCA.Intermittent.save", jo.toString(2), null)
    }

    fun getPropName(event: String): String {
        return "$event$PROPER_NAME"
    }
}