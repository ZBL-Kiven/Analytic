package com.zj.analyticSdk.core.worker

internal interface MsgDealIn {
    fun onDeal(isSuccess: Boolean, retry: Boolean, info: EventInfo)
}