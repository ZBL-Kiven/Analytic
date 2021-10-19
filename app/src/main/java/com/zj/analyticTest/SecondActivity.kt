package com.zj.analyticTest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.CCAnalytic
import com.google.android.material.tabs.TabLayout
import com.zj.cf.fragments.BaseTabFragment
import com.zj.cf.managers.TabFragmentManager
import java.lang.NullPointerException


class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

    /**
     * 埋点方法
     * */
    fun analyticOne(view: View) {
        CCAnalytic.get()?.trackEvent("test event", "event" to "click")
    }

    /**
     * 测试使用，不需要手动调用。
     * */
    fun toNext(view: View) {
        startActivity(Intent(this, ThirdActivity::class.java))
    }

}