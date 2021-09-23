package com.zj.analyticSdk

import android.util.Log
import java.lang.Exception

internal object CALogs {

    @JvmStatic
    fun printStackTrace(ex: Exception) {
        if (CCAnalytic.getConfig().isLogEnabled()) {
            Log.e("------ cc-analytics", "onError : case \n${ex.message}")
        }
    }

    @JvmStatic
    fun i(tag: String, s: String?, e: Exception? = null) {
        if (CCAnalytic.getConfig().isLogEnabled()) {
            Log.i("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
        }
    }

    @JvmStatic
    fun e(tag: String, s: String?, e: Exception? = null) {
        if (CCAnalytic.getConfig().isLogEnabled()) {
            Log.e("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
        }
    }
}