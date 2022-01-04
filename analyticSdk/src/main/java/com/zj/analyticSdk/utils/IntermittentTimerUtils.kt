package com.zj.analyticSdk.utils

import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.IntermittentType
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.persistence.sp.SpUtils
import com.zj.analyticSdk.persistence.sp.SpUtils.poll
import com.zj.analyticSdk.persistence.sp.SpUtils.saveObject
import org.json.JSONObject


internal object IntermittentTimerUtils {

    /**
     * enqueue recorder task for all of cached intermittent data event
     * call in [com.zj.analyticSdk.CCAnalytic.flushIntermittentInfo]
     * or [com.zj.analyticSdk.CCAnalytic.flushAllIntermittentInfo]
     * */
    fun flushIfExists(flushInfo: EventInfo.FlushInfo) {
        if (flushInfo.eventName.isNullOrEmpty()) {
            val data = SpUtils.pollAll()
            data.forEach {
                val eventName = kotlin.runCatching {
                    it.optString(CCAnalytic.getConfig().getEventNameBuilder().eventName())
                }.getOrNull()
                if (!eventName.isNullOrEmpty()) {
                    CCAnalytic.getConfig().beforeEvent(eventName, it)?.let { param ->
                        DBHelper.getInstance().addJSON(param)
                        CALogs.i("CCA.flushIfExists", param.toString(), null)
                    }
                }
            }
        } else {
            val data = flushInfo.eventName.poll() ?: return
            CCAnalytic.getConfig().beforeEvent(flushInfo.eventName, data)?.let { param ->
                DBHelper.getInstance().addJSON(param)
                CALogs.i("CCA.flushIfExists", param.toString(), null)
            }
        }
    }

    /**
     * @see [IntermittentType]
     * */
    fun putOrUpdate(event: String, buildParams: JSONObject, intermittentType: IntermittentType) {
        val obj = event.poll()
        if (obj == null) {
            event saveObject buildParams
            return
        }
        val newKeys = buildParams.keys()
        newKeys.forEach {
            val value = kotlin.runCatching { buildParams.get(it) }.getOrNull()
            when (intermittentType) {
                IntermittentType.REPLACE -> {
                    obj.put(it, value)
                }
                IntermittentType.STAY_IF_NULL -> {
                    if (value != null) {
                        obj.put(it, value)
                    }
                }
                IntermittentType.REMOVE_IF_NULL -> {
                    if (value == null) {
                        obj.remove(it)
                    } else {
                        obj.put(it, value)
                    }
                }
            }
        }
        CALogs.i("CCA.putOrUpdate ,  IntermittentType = ${intermittentType.name}", obj.toString(), null)
        event saveObject obj
    }
}