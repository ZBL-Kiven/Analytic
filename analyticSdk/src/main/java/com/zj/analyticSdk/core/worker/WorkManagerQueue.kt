package com.zj.analyticSdk.core.worker

import android.os.HandlerThread
import com.zj.analyticSdk.leastDownTo
import com.zj.analyticSdk.plus
import java.lang.Exception
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Suppress("unused")
internal object WorkManagerQueue : MsgDealIn {

    private val queue = LinkedBlockingDeque<EventInfo>()
    private var cursor: AtomicInteger = AtomicInteger(0)
    private var inHandle: AtomicBoolean = AtomicBoolean(false)
    private lateinit var handler: AnalyticsMessageHandler

    fun push(info: EventInfo, delay: Long = 0) {
        if (!::handler.isInitialized) {
            initHandler()
        }
        cursor plus 1
        queue.push(info)
        if (!inHandle.get()) onDealNext(delay)
    }

    fun clearAll() {
        push(EventInfo.clear())
        if (!inHandle.get()) onDealNext(0)
    }

    fun checkRunner() {
        push(EventInfo.check())
        if (!inHandle.get()) onDealNext(0)
    }

    private fun onDealNext(delay: Long) {
        if (cursor.get() > 0) {
            try {
                inHandle.set(true)
                val poll = queue.pollFirst()
                poll?.let {
                    if (handler.sendMessage(it, delay)) {
                        cursor leastDownTo 0
                        inHandle.set(false)
                    } else {
                        queue.push(poll)
                    }
                } ?: cursor leastDownTo 0
            } catch (e: Exception) {
                inHandle.set(false)
            }
        }
    }

    override fun onDeal(isSuccess: Boolean, retry: Boolean, info: EventInfo) {
        inHandle.set(false)
        if (!isSuccess) {
            if (retry) push(info, 16)
        } else {
            onDealNext(16)
        }
    }

    private fun initHandler() {
        val thread = object : HandlerThread("com.zj.analyticSdk.core.worker", Thread.MIN_PRIORITY) {}
        thread.start()
        handler = AnalyticsMessageHandler(thread.looper)
        push(EventInfo.upload(), 3000)
    }
}