package com.zj.analyticSdk.core.worker

import com.zj.analyticSdk.core.HandleType
import org.json.JSONObject

internal class EventInfo private constructor(val handleType: HandleType) {

    companion object {

        fun record(eventName: String, jsonObject: JSONObject, intermittentType: Boolean, withData: Any? = null): EventInfo {
            return EventInfo(HandleType.ANALYTIC).apply {
                data = RecordInfo(eventName, jsonObject, intermittentType, withData)
            }
        }

        fun flushIntermittentInfo(eventName: String? = null): EventInfo {
            return EventInfo(HandleType.FLUSH_INTERMITTENT).apply {
                data = FlushInfo(eventName)
            }
        }

        fun check(): EventInfo {
            return EventInfo(HandleType.CHECK)
        }

        fun upload(): EventInfo {
            return EventInfo(HandleType.UPLOAD)
        }

        fun clear(): EventInfo {
            return EventInfo(HandleType.CLEAR)
        }
    }

    var data: Any? = null; private set

    data class RecordInfo(val eventName: String, val jsonObject: JSONObject, val intermittentType: Boolean = false, val withData: Any? = null)

    data class FlushInfo(val eventName: String?)
}