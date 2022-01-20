package com.zj.analyticSdk.expose.i

import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.expose.p.BaseExpose
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.ViewPager2ExposeIn
import com.zj.analyticSdk.utils.PreviousAbleInt

internal class ViewPager2Exposer<T>(lifecycleOwner: LifecycleOwner, private val pager: ViewPager2, private val exposeIn: ViewPager2ExposeIn<T>, bex: BaseExposeIn<T>) : BaseExpose<T, ViewPager2>(lifecycleOwner, pager, bex) {

    private var currentItemPositionIsDetached = pager.adapter != null
    private var pai = PreviousAbleInt(pager.currentItem)

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (currentItemPositionIsDetached) {
                currentItemPositionIsDetached = false
                val last = pai.getPreviousValue()
                if (pai.peek() == last) return
                bex.onDetached(getDataForPosition(last))
            }
        }

        override fun onPageSelected(position: Int) {
            if (!currentItemPositionIsDetached) {
                bex.onAttached(getDataForPosition(position))
            }
            pai set position
            currentItemPositionIsDetached = true
        }
    }

    override fun onInit(v: ViewPager2) {
        if (currentItemPositionIsDetached) {
            currentItemPositionIsDetached = false
            onPageChangeCallback.onPageSelected(pai.peek())
        }
        v.registerOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onApplicationLayerChanged(inBackground: Boolean) {
        val index = pager.currentItem
        if (index !in 0..(pager.adapter?.itemCount ?: return)) return
        if (inBackground) detach(getDataForPosition(index)) else attach(getDataForPosition(index))
    }

    override fun release() {
        kotlin.runCatching {
            pager.unregisterOnPageChangeCallback(onPageChangeCallback)
        }
    }

    private fun getDataForPosition(position: Int): T? {
        return kotlin.runCatching { exposeIn.getDataForViewPager2(pager, position, pager.adapter ?: return null) }.getOrNull()
    }
}