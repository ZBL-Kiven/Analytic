package com.zj.analyticTest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.anno.PageAnalyticParams
import com.zj.analyticSdk.expose.ExposeUtils
import com.zj.analyticSdk.expose.p.BaseExposeIn
import org.json.JSONObject
import java.util.*

class ThirdActivity : AppCompatActivity(), BaseExposeIn<Int> {

    var p: Int = 0; get() = field++

    @PageAnalyticParams("thirdActivity_pending") var pa = "aczxzascs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        val v = findViewById<View>(R.id.third_tv)
        ExposeUtils.with(this).track(v, 1002, this)
        v.setOnClickListener {
            startActivity(Intent(it.context, ThirdActivity::class.java))
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                CCAnalytic.get()?.trackEvent("Dots!!!!!", "aaaaa" to "$p", intermittentType = true)
            }
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName, this)
        val jo = JSONObject()
        jo.put("begin_game_time", "${System.currentTimeMillis()}")
        jo.put("play_duration", "3000")
        jo.put("1", "ashdjbajshfjasbdq")
        jo.put("2", "ashdjbajshfjasbdq")
        jo.put("3", "ashdjbajshfjasbdq")
        jo.put("4", "ashdjbajshfjasbdq")
        jo.put("5", "ashdjbajshfjasbdq")
        CCAnalytic.get()?.trackEvent("Dots!!!!!", jo, intermittentType = true)
    }

    override fun onAttached(data: Int?) {
        Log.e("------- ", "onAttached  $data   ${this.hashCode()}")
    }

    override fun onDetached(data: Int?) {
        CCAnalytic.get()?.flushAllIntermittentInfo()
    }

    fun dotOne(view: View) {
        CCAnalytic.get()?.trackEvent("Dots!!!!!", "aaaaa" to "$p", intermittentType = true)
    }

    fun flushDots(view: View) {
        CCAnalytic.get()?.flushIntermittentInfo("Dots!!!!!")
    }
}