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
    fun i(lga: Int, tag: String, s: String?, e: Exception? = null) {
        if (logEnable(lga)) {
            Log.i("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
        }
    }

    @JvmStatic
    fun e(lga: Int, tag: String, s: String?, e: Exception? = null) {
        if (logEnable(lga)) {
            Log.e("------ cc-analytics", "case :\ntag = $tag\ns = $s\ne = ${e?.message}")
        }
    }

    private fun logEnable(lga: Int): Boolean {
        return CCAnalytic.getConfig().let {
            it.isLogEnabled() && (lga.and(CAConfigs.LOG_SYSTEM) != 0 || it.logFrequency().and(CAConfigs.LOG_ALL) != 0 || it.logFrequency().and(lga) != 0)
        }
    }
}