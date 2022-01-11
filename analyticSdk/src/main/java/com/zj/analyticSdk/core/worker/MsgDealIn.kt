package com.zj.analyticSdk.core.worker

import kotlin.Boolean

internal interface MsgDealIn {
    fun onDeal(isSuccess: Boolean, retry: Boolean, info: EventInfo)
}