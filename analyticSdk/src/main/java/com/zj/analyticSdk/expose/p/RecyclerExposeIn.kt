package com.zj.analyticSdk.expose.p

import androidx.recyclerview.widget.RecyclerView

interface RecyclerExposeIn<T> {
    fun getDataForRecyclerView(recyclerView: RecyclerView, p: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): T?
}