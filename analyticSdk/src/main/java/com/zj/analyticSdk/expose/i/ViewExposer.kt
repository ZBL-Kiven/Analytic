package com.zj.analyticSdk.expose.i


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.expose.p.BaseExpose
import com.zj.analyticSdk.expose.p.BaseExposeIn
import java.lang.IllegalArgumentException

internal class ViewExposer<T>(view: View, private val data: T?, bex: BaseExposeIn<T>) : BaseExpose<T, View>(view, bex) {

    init {
        when (view) {
            is RecyclerView, is ViewPager, is ViewPager2 -> {
                throw IllegalArgumentException("Use the corresponding Exposer for RecyclerView, ViewPager, ViewPager2 and other components")
            }
        }
    }

    override fun onApplicationLayerChanged(inBackground: Boolean) {
        if (inBackground) detach(data) else attach(data)
    }

    override fun onInit(v: View) {
        attach(data)
    }

    override fun release() {
        detach(data)
    }
}