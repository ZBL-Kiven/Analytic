package com.zj.analyticSdk.core.router

import android.util.Log
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.core.data.DataUploader
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.MsgDealIn

internal class UploadTask(private val info: EventInfo, private val handleIn: MsgDealIn) : Runnable {

    override fun run() {
        CCAnalytic.getApplication().let {
            DataUploader(it).sendData { success ->
                if (success) Log.e("----- ", "some file  uploaded!")
                this@UploadTask.handleIn.onDeal(success, false, info)
            }
        }
    }
}