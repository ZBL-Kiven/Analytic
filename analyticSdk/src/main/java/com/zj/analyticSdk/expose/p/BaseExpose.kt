package com.zj.analyticSdk.expose.p

import android.view.View
import com.zj.analyticSdk.R
import com.zj.analyticSdk.recorder.AppUtils

internal abstract class BaseExpose<T, V : View>(v: V, private val bex: BaseExposeIn<T>) : View.OnAttachStateChangeListener {

    private val id = v.hashCode().toString()
    private val l = AppUtils.StatusChangeListener(v.context.hashCode(), {
        if (v.isAttachedToWindow) {
            onApplicationLayerChanged(it)
        }
    })

    init {
        initWithView(v)
    }

    private fun initWithView(v: V) {
        v.addOnAttachStateChangeListener(this)
        AppUtils.addOnAppStateChangeListener(id, l)
        if (v.isAttachedToWindow) {
            v.post { onInit(v) }
        }
    }

    final override fun onViewAttachedToWindow(v: View) {
        if (l.lastState.get()) {
            l.lastState.set(false)
        }
        val v1 = cast<View, V>(v) ?: return
        onInit(v1)
    }

    final override fun onViewDetachedFromWindow(v: View) {
        v.setTag(R.id.view_expose_binder_id, null)
        if (!l.lastState.get()) {
            l.lastState.set(true)
        }
        release()
        AppUtils.removeAppStateChangeListener(id)
        v.removeOnAttachStateChangeListener(this)
    }

    open fun onInit(v: V) {}
    abstract fun release()
    abstract fun onApplicationLayerChanged(inBackground: Boolean)

    protected fun attach(data: T?) {
        bex.onAttached(data)
        l.lastState.set(false)
    }

    protected fun detach(data: T?) {
        bex.onDetached(data)
        l.lastState.set(true)
    }

    private fun <X, Y> cast(x: X): Y? {
        return kotlin.runCatching {
            @Suppress("UNCHECKED_CAST") (x as? Y)
        }.getOrNull()
    }
}