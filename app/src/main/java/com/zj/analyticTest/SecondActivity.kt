package com.zj.analyticTest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.anno.PageAnalyticParams
import com.zj.analyticSdk.anno.PageInfo

/**
 * 通过在任何 Activity 页面顶部注解 [PageInfo] ，将自动统计页面相关属性。
 * */
@PageInfo(pageName = "SecondActivity")
class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("onStart SecondActivity", "${intent.getStringExtra("111")}")
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

    override fun finish() {
//        intent.putExtra("222", "casca")
//        setResult(200)
        super.finish()
    }

}