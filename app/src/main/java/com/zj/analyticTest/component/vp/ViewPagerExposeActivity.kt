package com.zj.analyticTest.component.vp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.expose.ExposeUtils
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.ViewPagerExposeIn
import com.zj.analyticTest.R

class ViewPagerExposeActivity : AppCompatActivity(), ViewPagerExposeIn<Int>, BaseExposeIn<Int> {

    private lateinit var vp: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_vp)
        vp = findViewById(R.id.main_vp)
        initView()
    }

    private fun initView() {
        ExposeUtils.with(this).trackViewPager(vp, this, this)
        findViewById<View>(R.id.main_btn).setOnClickListener {
            val adapter = PAdapter()
            vp.adapter = adapter
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

    override fun getDataForViewPager(pager: ViewPager, p: Int, adapter: PagerAdapter): Int? {
        return (adapter as? PAdapter)?.data?.get(p)
    }
}