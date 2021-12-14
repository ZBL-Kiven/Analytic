package com.zj.analyticTest.component.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zj.analyticTest.R

class VAdapter : RecyclerView.Adapter<VAdapter.Holder>() {

    var data = (1..1000).toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.initData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        private val tv = itemView.findViewById<TextView>(R.id.main_item_txt)

        fun initData(d: Int) {
            tv.text = "$d"
        }
    }
}