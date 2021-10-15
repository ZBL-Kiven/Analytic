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
@PageInfo(pageName = "MainActivity test")
class MainActivity : BaseResultAbleActivity() {

    /**
     * 无限制，可同时注解多个元素
     * 使用 [PageAnalyticParams] 注解一个元素，将在埋点发生时自动埋入 ,仅在当前 Activity 生命周期内有效，支持使用 getter 方法返回动态参数。
     * 其中注解的 value 为埋点 key 值。
     * 使用场景：当前 Channel Name、当前在 TAB 的什么位置、当前交互的弹窗属性、页面属性 等。
     * 此字段仍可正常使用。
     * */
    @PageAnalyticParams("page_title") var pageTitle: Int = 0; get() = field++


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
        startActivityForResult(SecondActivity::class.java.name, 300, params = arrayOf("111" to "asdasd")) { i, i2, intent ->
            Log.e("start act result ===> ", "rq = $i   rsp = $i2  param = ${intent?.getStringExtra("222")}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onActivityResult ", "rq = $requestCode   rsp = $resultCode  param = ${data?.getStringExtra("222")}")
    }

}