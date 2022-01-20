package com.zj.analyticSdk.expose.p

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.zj.analyticSdk.R
import com.zj.analyticSdk.expose.StatusChangeListener


internal abstract class BaseExpose<T, V : View>(private val lifecycleOwner: LifecycleOwner, private val v: V, private val bex: BaseExposeIn<T>) : View.OnAttachStateChangeListener, LifecycleEventObserver {

    private val l = StatusChangeListener(v.context.hashCode()) {
        if (v.isAttachedToWindow) {
            onApplicationLayerChanged(it)
        }
    }

    init {
        initWithView()
    }

    private fun initWithView() {
        lifecycleOwner.lifecycle.addObserver(this)
        v.addOnAttachStateChangeListener(this)
        if (v.isAttachedToWindow) {
            v.post { onInit(v) }
        }
    }

    final override fun onViewAttachedToWindow(v: View) {
        l.setLastState(false)
        val v1 = cast<View, V>(v) ?: return
        onInit(v1)
    }

    final override fun onViewDetachedFromWindow(v: View) {
        kotlin.runCatching {
            lifecycleOwner.lifecycle.removeObserver(this)
        }
        v.setTag(R.id.view_expose_binder_id, null)
        l.setLastState(true)
        release()
        v.removeOnAttachStateChangeListener(this)
    }

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                l.setLastState(false)
            }
            Lifecycle.Event.ON_PAUSE -> {
                l.setLastState(true)
            }
            else -> {
            }
        }
    }

    open fun onInit(v: V) {}
    abstract fun release()
    abstract fun onApplicationLayerChanged(inBackground: Boolean)

    protected fun attach(data: T?) {
        postOrUse {
            bex.onAttached(data)
        }
        l.setLastState(false)
    }

    protected fun detach(data: T?) {
        postOrUse {
            bex.onDetached(data)
        }
        l.setLastState(true)
    }

    private fun postOrUse(use: () -> Unit) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.STARTED || lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            use()
        } else {
            v.post(use)
        }
    }

    private fun <X, Y> cast(x: X): Y? {
        return kotlin.runCatching {
            @Suppress("UNCHECKED_CAST") (x as? Y)
        }.getOrNull()
    }
}