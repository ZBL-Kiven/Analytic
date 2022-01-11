package com.zj.analyticSdk.core.router

import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.MsgDealIn
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.recorder.BasePropertyCollector
import com.zj.analyticSdk.recorder.PageTracker
import com.zj.analyticSdk.utils.IntermittentTimerUtils
import java.lang.Exception

internal class RecordTask(private val info: EventInfo, private val handleIn: MsgDealIn) : Runnable {

    override fun run() {
        var isSuccess = false
        try {
            val recordInfo = info.data as EventInfo.RecordInfo
            val obj = if (recordInfo.intermittentType) {
                IntermittentTimerUtils.putOrUpdate(recordInfo.eventName, recordInfo.jsonObject)
            } else {
                recordInfo.jsonObject
            }
            val defaultParams = BasePropertyCollector.getBaseProperties(recordInfo.eventName)
            val params = CCAnalytic.getConfig().addDefaultParam(recordInfo.eventName, recordInfo.withData, defaultParams)
            if (recordInfo.eventName != CCAnalytic.getConfig().getEventNameBuilder().onPageFinished()) {
                PageTracker.trackPageInfo(obj)
            }
            val merged = CCAnalytic.getConfig().onMergeProperties(recordInfo.eventName, obj, params) ?: throw NullPointerException("recording params must not be null ,transfer by CAConfig.onMergeProperties")
            if (recordInfo.intermittentType) {
                IntermittentTimerUtils.save(recordInfo.eventName, merged)
            } else {
                val buildParams = CCAnalytic.getConfig().beforeEvent(recordInfo.eventName, merged) ?: throw NullPointerException("recording params must not be null ,transfer by CAConfig.beforeEvent")
                DBHelper.getInstance().addJSON(buildParams)
                CALogs.i(CAConfigs.LOG_BEFORE_EVENT, "CCA.RecordTask", buildParams.toString(), null)
            }
            isSuccess = true
        } catch (e: Exception) {
            CALogs.i(CAConfigs.LOG_RECORD, "CCA.RecordTask", "record error", e)
        } finally {
            handleIn.onDeal(isSuccess = isSuccess, retry = false, info = info)
        }
    }
}