package com.zj.analyticTest

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.anno.PageAnalyticParams
import com.zj.analyticTest.component.rv.ExposeActivity
import com.zj.analyticTest.component.vp.ViewPagerExposeActivity
import com.zj.analyticTest.component.vp2.ViewPager2ExposeActivity
import com.zj.cf.setConstrainFragmentLifecycleCallBack


class MainActivity : BaseResultAbleActivity() {

    /**
     * 无限制，可同时注解多个元素
     * 使用 [PageAnalyticParams] 注解一个元素，将在埋点发生时自动埋入 ,仅在当前 Activity 生命周期内有效，支持使用 getter 方法返回动态参数。
     * 其中注解的 value 为埋点 key 值。
     * 使用场景：当前 Channel Name、当前在 TAB 的什么位置、当前交互的弹窗属性、页面属性 等。
     * 此字段仍可正常使用。
     * */
    @PageAnalyticParams("page_title") var pageTitle: Int = 0; get() = field++
    @PageAnalyticParams("page_title1") var pageTitle1: Int = 0; get() = field++
    @PageAnalyticParams("page_title2") var pageTitle2: Int = 0; get() = field++
    @PageAnalyticParams("page_title3") var pageTitle3: Int = 0; get() = field++


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setConstrainFragmentLifecycleCallBack { lifecycle, s, s2 ->
            Log.e("------ ", "$lifecycle  $s   , $s2")
        }
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName, this)
    }

    /**
     * 测试使用，不需要手动调用。
     * */
    fun toNext(view: View) {
        startActivityForResult(ThirdActivity::class.java.name, 400, params = arrayOf("111" to "asdasd")) { i, i2, intent ->
            Log.e("start act result ===> ", "rq = $i   rsp = $i2  param = ${intent?.getStringExtra("222")}")
        }
    }

    fun exposePager(view: View) {
        startActivity(Intent(view.context, ViewPagerExposeActivity::class.java))
    }

    fun expose(view: View) {
        startActivity(Intent(view.context, ExposeActivity::class.java))
    }

    fun exposeVp2(view: View) {
        startActivity(Intent(view.context, ViewPager2ExposeActivity::class.java))
    }

    fun exposeView(view: View) {
        startActivity(Intent(view.context, ThirdActivity::class.java))
    }

    fun analyticOnce(view: View) {
        CCAnalytic.get()?.trackPageStart("test_once")
        CCAnalytic.get()?.trackEvent("track 00000000")
        CCAnalytic.get()?.restoreToLastPage()
        CCAnalytic.get()?.trackEvent("track 11111111")
    }
}