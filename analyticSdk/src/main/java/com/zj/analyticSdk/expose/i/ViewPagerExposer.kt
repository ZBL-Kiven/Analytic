package com.zj.analyticSdk.expose.i

import android.database.DataSetObserver
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.zj.analyticSdk.expose.p.BaseExpose
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.ViewPagerExposeIn
import com.zj.analyticSdk.utils.PreviousAbleInt

internal class ViewPagerExposer<T>(private val pager: ViewPager, private val exposeIn: ViewPagerExposeIn<T>, bex: BaseExposeIn<T>) : BaseExpose<T, ViewPager>(pager, bex), ViewPager.OnAdapterChangeListener, ViewPager.OnPageChangeListener {

    private var currentItemPositionIsDetached = false
    private var mayNotifyCurrent = false
    private var pai = PreviousAbleInt(pager.currentItem)
    private val adapterObserver = object : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            checkToNotifyInit()
        }
    }

    override fun onInit(v: ViewPager) {
        v.addOnPageChangeListener(this)
        val data = getDataForPosition(v.currentItem)
        if (data != null) {
            attach(data)
        } else {
            mayNotifyCurrent = true
            v.adapter?.registerDataSetObserver(adapterObserver)
            v.addOnAdapterChangeListener(this)
        }
    }

    override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
        checkToNotifyInit()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (currentItemPositionIsDetached) {
            currentItemPositionIsDetached = false
            detach(getDataForPosition(pai.getPreviousValue()))
        }
    }

    override fun onPageSelected(position: Int) {
        if (!currentItemPositionIsDetached) {
            attach(getDataForPosition(position))
        }
        pai set position
        currentItemPositionIsDetached = true
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onApplicationLayerChanged(inBackground: Boolean) {
        val index = pager.currentItem
        if (index !in 0..(pager.adapter?.count ?: return)) return
        if (inBackground) detach(getDataForPosition(index)) else attach(getDataForPosition(index))
    }

    override fun release() {
        removeInitListener()
        kotlin.runCatching {
            pager.removeOnPageChangeListener(this)
        }
    }

    private fun checkToNotifyInit() {
        if (mayNotifyCurrent) {
            mayNotifyCurrent = false
            val data = getDataForPosition(pager.currentItem)
            attach(data)
        }
        removeInitListener()
    }

    private fun removeInitListener() {
        kotlin.runCatching {
            pager.adapter?.unregisterDataSetObserver(this.adapterObserver)
        }
        kotlin.runCatching {
            pager.removeOnAdapterChangeListener(this@ViewPagerExposer)
        }
    }

    private fun getDataForPosition(position: Int): T? {
        return kotlin.runCatching { exposeIn.getDataForViewPager(pager, position, pager.adapter ?: return null) }.getOrNull()
    }
}