package com.zj.analyticTest.component.vp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.zj.analyticTest.R

class PAdapter : PagerAdapter() {

    var data = (1..1000).toMutableList()

    override fun getCount(): Int {
        return data.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return `object` == view
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = LayoutInflater.from(container.context).inflate(R.layout.item_match, container, false)
        v.findViewById<TextView>(R.id.main_item_txt).text = "${data[position]}"
        container.addView(v)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) container.removeView(`object`)
    }
}