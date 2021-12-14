package com.zj.analyticSdk.expose.i

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zj.analyticSdk.expose.p.BaseExpose
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.RecyclerExposeIn

internal class RecycleViewExposer<T>(private val recyclerView: RecyclerView, private val exposeIn: RecyclerExposeIn<T>, bex: BaseExposeIn<T>) : BaseExpose<T, RecyclerView>(recyclerView, bex), RecyclerView.OnChildAttachStateChangeListener {

    private var onNotifyChanging = false
    private var registeredAdapter = false

    private val dataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            onNotifyChanging = false
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onNotifyChanging = true
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onNotifyChanging = true
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onNotifyChanging = true
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onNotifyChanging = false
        }
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) onNotifyChanging = false
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    override fun onInit(v: RecyclerView) {
        if ((v.adapter?.itemCount ?: 0) > 0) {
            onApplicationLayerChanged(false)
        }
        v.addOnChildAttachStateChangeListener(this)
        v.addOnScrollListener(onScrollListener)
    }

    override fun onChildViewAttachedToWindow(view: View) {
        if (!registeredAdapter && recyclerView.adapter != null) {
            recyclerView.adapter?.registerAdapterDataObserver(dataObserver)
            registeredAdapter = true
        }
        val data = onChildViewFocusChanged(view)
        if (data != null) attach(data)
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        val data = onChildViewFocusChanged(view)
        if (data != null) detach(data)
    }

    override fun release() {
        kotlin.runCatching {
            recyclerView.removeOnChildAttachStateChangeListener(this)
        }
        kotlin.runCatching {
            recyclerView.adapter?.unregisterAdapterDataObserver(dataObserver)
        }
        kotlin.runCatching {
            recyclerView.removeOnScrollListener(onScrollListener)
        }
    }

    override fun onApplicationLayerChanged(inBackground: Boolean) {
        var lf = -1
        var ls = -1
        when (val it = recyclerView.layoutManager) {
            is LinearLayoutManager -> {
                lf = it.findFirstVisibleItemPosition()
                ls = it.findLastVisibleItemPosition()
            }
            is GridLayoutManager -> {
                lf = it.findFirstVisibleItemPosition()
                ls = it.findLastVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                val itemCount = recyclerView.adapter?.itemCount ?: return
                lf = it.findFirstVisibleItemPositions(IntArray(itemCount)).minOrNull() ?: -1
                ls = it.findLastVisibleItemPositions(IntArray(itemCount)).maxOrNull() ?: -1
            }
        }
        if (lf < 0 || ls < 0 || ls < lf) return
        (lf..ls).forEach {
            if (inBackground) detach(getDataForPosition(it)) else attach(getDataForPosition(it))
        }
    }

    private fun onChildViewFocusChanged(view: View): T? {
        if (onNotifyChanging) return null
        val clp = recyclerView.getChildViewHolder(view)
        val position = clp.layoutPosition
        return getDataForPosition(position)
    }

    private fun getDataForPosition(position: Int): T? {
        val adapter = recyclerView.adapter ?: return null
        return kotlin.runCatching { exposeIn.getDataForRecyclerView(recyclerView, position, adapter) }.getOrNull()
    }
}