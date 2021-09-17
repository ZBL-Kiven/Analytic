package com.zj.analyticSdk

import android.util.Log
import java.lang.Exception

internal object CALogs {

    @JvmStatic
    fun printStackTrace(ex: Exception) {
        Log.e("------ cc-analytics", "onError : case \n${ex.message}")
    }

    @JvmStatic
    fun i(tag: String, s: String?, e: Exception? = null) {
        Log.i("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
    }

    @JvmStatic
    fun e(tag: String, s: String?, e: Exception? = null) {
        Log.e("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
    }
}