package com.zj.analyticSdk.core.router

import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.MsgDealIn
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.recorder.BasePropertyCollector
import com.zj.analyticSdk.utils.JOUtils
import java.lang.Exception

internal class RecordTask(private val info: EventInfo, private val handleIn: MsgDealIn) : Runnable {

    override fun run() {
        try {
            val recordInfo = info.data as EventInfo.RecordInfo
            val defaultParams = BasePropertyCollector.getBaseProperties(recordInfo.eventName)
            val params = CCAnalytic.getConfig().getEventParams(recordInfo.eventName, recordInfo.withData, defaultParams)
            JOUtils.mergeJSONObject(recordInfo.jsonObject, params)
            val buildParams = CCAnalytic.getConfig().beforeEvent(recordInfo.eventName, params) ?: throw NullPointerException("recording params has changed by CAConfig.beforeEvent")
            DBHelper.getInstance().addJSON(buildParams)
            CALogs.i("CCA.RecordTask", buildParams.toString(), null)
            handleIn.onDeal(isSuccess = true, retry = false, info = info)
        } catch (e: Exception) {
            handleIn.onDeal(isSuccess = false, retry = false, info = info)
            CALogs.i("CCA.RecordTask", "record error", e)
        }
    }
}