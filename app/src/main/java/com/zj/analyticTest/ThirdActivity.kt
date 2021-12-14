package com.zj.analyticTest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.expose.ExposeUtils
import com.zj.analyticSdk.expose.p.BaseExposeIn

class ThirdActivity : AppCompatActivity(), BaseExposeIn<Int> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        val v = findViewById<View>(R.id.third_tv)
        ExposeUtils.with(v, 1002, this)
        v.setOnClickListener {
            startActivity(Intent(it.context, ThirdActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }

    override fun onAttached(data: Int?) {
        Log.e("------- ", "onAttached  $data   ${this.hashCode()}")
    }

    override fun onDetached(data: Int?) {
//        Log.e("------- ", "onDetached  $data   ${this.hashCode()}")
    }
}