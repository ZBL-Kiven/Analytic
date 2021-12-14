package com.zj.analyticSdk.utils

class PreviousAbleInt(initValue: Int) {

    private var value: Int = initValue
    private var last = initValue
    private var theoreticalNextValue: Int? = null
    private var isGroup: Boolean? = null

    infix fun set(cur: Int) {
        value = cur
        when (isGroup) {
            null -> {
                isGroup = if (value == last) null else value > last
            }
            true -> {
                if (value <= last) {
                    isGroup = false
                    last = value + 1
                } else {
                    last = value - 1
                }
            }
            false -> {
                if (value >= last) {
                    isGroup = true
                    last = value - 1
                } else {
                    last = value + 1
                }
            }
        }
        theoreticalNextValue = if (last == value) null else if (last > value) value - 1 else value + 1
    }

    fun getPreviousValue(): Int {
        return last
    }

    fun peek(): Int {
        return value
    }
}