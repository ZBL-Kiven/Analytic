package com.zj.analyticTest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.CCAnalytic
import com.google.android.material.tabs.TabLayout
import com.zj.cf.fragments.BaseTabFragment
import com.zj.cf.managers.TabFragmentManager
import java.lang.NullPointerException


class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        Log.e("onStart SecondActivity", "${intent.getStringExtra("111")}")
        val vp2 = findViewById<ViewPager2>(R.id.main_frag)
        val tab = findViewById<TabLayout>(R.id.main_tab)

        object : TabFragmentManager<Int, BaseTabFragment>(this, vp2, 0, tab, 0, 1, 2) {
            override fun onCreateFragment(d: Int, p: Int): BaseTabFragment {
                return when (d) {
                    0 -> TestFragment()
                    1 -> TestFragment1()
                    2 -> TestFragment2()
                    else -> throw NullPointerException()
                }
            }

            override fun tabConfigurationStrategy(tab: TabLayout.Tab, position: Int) {
                tab.text = "TAB$position"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }

    override fun finish() {
        setResult(-100)
        super.finish()
    }
}