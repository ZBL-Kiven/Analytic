package com.zj.analyticSdk.expose.p

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

interface ViewPager2ExposeIn<T> {
    fun getDataForViewPager2(pager: ViewPager2, p: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): T?
}