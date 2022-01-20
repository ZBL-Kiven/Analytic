package com.zj.analyticTest.component.vp2

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.expose.ExposeUtils
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.ViewPager2ExposeIn
import com.zj.analyticTest.R

class ViewPager2ExposeActivity : AppCompatActivity(), ViewPager2ExposeIn<Int>, BaseExposeIn<Int> {

    private lateinit var vp: ViewPager2
    private lateinit var v: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_vp2)
        vp = findViewById(R.id.main_vp)
        v = findViewById(R.id.main_btn_vp2)
        initView()
    }

    private fun initView() {
        ExposeUtils.with(this).trackViewPager2(vp, this, this)
        val adapter = P2Adapter()
        vp.adapter = adapter
        v.setOnClickListener {


        }
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }

    override fun onAttached(data: Int?) {
        Log.e("------ ", "onViewAttached $data")
    }

    override fun onDetached(data: Int?) { //        Log.e("------ ", "onViewDetached $data")
    }

    override fun getDataForViewPager2(pager: ViewPager2, p: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): Int? {
        return (adapter as? P2Adapter)?.data?.get(p)
    }
}