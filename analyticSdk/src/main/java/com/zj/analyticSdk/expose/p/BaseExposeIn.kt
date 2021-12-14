package com.zj.analyticSdk.expose.p

interface BaseExposeIn<T> {

    fun onAttached(data: T?) {}

    fun onDetached(data: T?) {}
}