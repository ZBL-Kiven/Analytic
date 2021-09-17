package com.zj.analyticSdk.core.worker

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.router.CheckTask
import com.zj.analyticSdk.core.HandleType
import com.zj.analyticSdk.core.router.RecordTask
import com.zj.analyticSdk.core.router.UploadTask
import com.zj.analyticSdk.persistence.DBHelper
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.random.Random

internal class AnalyticsMessageHandler(looper: Looper) : Handler(looper), MsgDealIn {

    private val worker = Executors.newFixedThreadPool(HandleType.values().size)
    private val handleMap = ConcurrentHashMap<HandleType, Boolean>()
    private var maxUploadIntervalNum = 0
        set(value) {
            field = if (value > CCAnalytic.getConfig().uploadMaxSize) {
                if (CCAnalytic.getConfig().autoUploadAble()) {
                    if (hasMessages(HandleType.UPLOAD.code)) {
                        removeMessages(HandleType.UPLOAD.code)
                        handleMap[HandleType.UPLOAD] = false
                    } else {
                        if (curTypeInHandle(HandleType.UPLOAD)) return
                    }
                    sendMessage(EventInfo.upload(), 0)
                };0
            } else value
        }

    private fun curTypeInHandle(type: HandleType): Boolean {
        return handleMap[type] ?: false
    }

    fun sendMessage(it: EventInfo, delay: Long): Boolean {
        if (curTypeInHandle(it.handleType)) return false
        handleMap[it.handleType] = true
        if (delay <= 0) {
            sendMessage(Message.obtain().apply {
                this.what = it.handleType.code
                this.obj = it
            })
        } else {
            sendMessageDelayed(Message.obtain().apply {
                this.what = it.handleType.code
                this.obj = it
            }, delay)
        }
        return true
    }

    override fun handleMessage(msg: Message) {
        val info = msg.obj as EventInfo
        when (msg.what) {
            HandleType.CHECK.code -> {
                CheckTask().run()
            }
            HandleType.UPLOAD.code -> {
                worker.execute(UploadTask(info, this))
            }
            HandleType.ANALYTIC.code -> {
                maxUploadIntervalNum++
                RecordTask(info, this).run()
            }
            HandleType.CLEAR.code -> {
                maxUploadIntervalNum = 0
                DBHelper.getInstance().deleteAllEvents()
            }
        }
    }

    private fun uploadAndNext() {
        if (hasMessages(HandleType.UPLOAD.code)) return
        val uploadInterval = CCAnalytic.getConfig().uploadInterval
        if (uploadInterval < 1000) throw IllegalStateException("too often ,the upload interval [$uploadInterval ms] is really you want ?!")
        sendMessage(EventInfo.upload(), uploadInterval + Random.nextInt(500))
    }

    override fun onDeal(isSuccess: Boolean, retry: Boolean, info: EventInfo) {
        handleMap[info.handleType] = false
        if (info.handleType == HandleType.UPLOAD) {
            maxUploadIntervalNum = 0
            if (CCAnalytic.getConfig().autoUploadAble()) uploadAndNext()
            return
        } else {
            WorkManagerQueue.onDeal(isSuccess, retry, info)
        }
    }
}