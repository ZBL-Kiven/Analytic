package com.zj.analyticTest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.anno.PageAnalyticParams
import com.zj.analyticSdk.anno.PageInfo

/**
 * 通过在任何 Activity 页面顶部注解 [PageInfo] ，将自动统计页面相关属性。
 * */
@PageInfo(pageName = "ThirdActivity")
class ThirdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        CCAnalytic.get()?.uploadTest()
    }

}