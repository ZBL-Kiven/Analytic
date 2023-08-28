package com.zj.analyticSdk.expose

import java.util.concurrent.atomic.AtomicBoolean

data class StatusChangeListener(val activityHashCode: Int, val l: (Boolean) -> Unit) {

    fun setLastState(value: Boolean) {
        if (lastState.get() != value) {
            lastState.set(value)
            l.invoke(value)
        }
    }

    fun getLastState(): Boolean {
        return lastState.get()
    }

    private var lastState: AtomicBoolean = AtomicBoolean(false)
}