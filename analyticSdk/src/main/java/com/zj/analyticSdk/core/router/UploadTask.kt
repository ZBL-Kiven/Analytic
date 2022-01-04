package com.zj.analyticSdk.core.router


import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.data.DataUploader
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.MsgDealIn

internal class UploadTask(private val info: EventInfo, private val handleIn: MsgDealIn) : Runnable {

    override fun run() {
        CCAnalytic.getApplication().let {
            DataUploader(it).sendData { success ->
                this@UploadTask.handleIn.onDeal(success, false, info)
            }
        }
    }
}