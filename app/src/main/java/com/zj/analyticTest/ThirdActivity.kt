package com.zj.analyticTest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zj.analyticSdk.CCAnalytic

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

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }
}