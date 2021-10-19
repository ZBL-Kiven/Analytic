package com.zj.analyticSdk.core.router

import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.MsgDealIn
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.recorder.BasePropertyCollector
import com.zj.analyticSdk.recorder.PageTracker
import java.lang.Exception

internal class RecordTask(private val info: EventInfo, private val handleIn: MsgDealIn) : Runnable {

    override fun run() {
        try {
            val recordInfo = info.data as EventInfo.RecordInfo
            val defaultParams = BasePropertyCollector.getBaseProperties(recordInfo.eventName)
            val params = CCAnalytic.getConfig().addDefaultParam(recordInfo.eventName, recordInfo.withData, defaultParams)
            if (recordInfo.eventName != CCAnalytic.getConfig().getEventNameBuilder().onPageFinished()) {
                PageTracker.trackPageInfo(recordInfo.jsonObject)
            }
            val merged = CCAnalytic.getConfig().onMergeProperties(recordInfo.eventName, recordInfo.jsonObject, params) ?: throw NullPointerException("recording params must not be null ,transfer by CAConfig.onMergeProperties")
            val buildParams = CCAnalytic.getConfig().beforeEvent(recordInfo.eventName, merged) ?: throw NullPointerException("recording params must not be null ,transfer by CAConfig.beforeEvent")
            DBHelper.getInstance().addJSON(buildParams)
            CALogs.i("CCA.RecordTask", buildParams.toString(), null)
            handleIn.onDeal(isSuccess = true, retry = false, info = info)
        } catch (e: Exception) {
            handleIn.onDeal(isSuccess = false, retry = false, info = info)
            CALogs.i("CCA.RecordTask", "record error", e)
        }
    }
}