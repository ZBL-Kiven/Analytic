package com.zj.analyticTest.component.vp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zj.analyticTest.R

class P2Adapter : RecyclerView.Adapter<P2Adapter.Holder>() {

    var data = (1..1000).toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.main_item_txt).text = "${data[position]}"
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v)
}