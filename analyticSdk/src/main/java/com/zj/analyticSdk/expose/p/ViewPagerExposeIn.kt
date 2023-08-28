package com.zj.analyticSdk.expose.p

import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

interface ViewPagerExposeIn<T> {
    fun getDataForViewPager(pager: ViewPager, p: Int, adapter: PagerAdapter): T?
}