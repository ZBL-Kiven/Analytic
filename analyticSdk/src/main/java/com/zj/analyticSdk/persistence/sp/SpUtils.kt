package com.zj.analyticSdk.persistence.sp

import android.content.Context
import android.content.SharedPreferences
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.persistence.DbParams
import com.zj.analyticSdk.utils.IntermittentTimerUtils.PROPER_NAME
import com.zj.analyticSdk.utils.IntermittentTimerUtils.getPropName
import org.json.JSONObject

internal object SpUtils {

    private val sp: SharedPreferences by lazy {
        val app = CCAnalytic.getApplication()
        return@lazy app.getSharedPreferences("${DbParams.DATABASE_NAME}.sp", Context.MODE_PRIVATE)
    }

    infix fun String.saveObject(element: JSONObject) {
        val s = element.toString(2)
        kotlin.runCatching {
            sp.edit().putString(this, s).apply()
        }
    }

    fun String.pollIntermittent(): JSONObject? {
        val s = kotlin.runCatching {
            val s = sp.getString(this, null) ?: return null
            JSONObject(s)
        }.getOrNull()
        sp.edit().let {
            if (!this.endsWith(PROPER_NAME)) {
                it.remove(getPropName(this))
            }
            it.remove(this)
        }.apply()
        return s
    }

    fun pollAllIntermittent(): List<JSONObject> {
        val lst = arrayListOf<JSONObject>()
        sp.all?.forEach { (k, v) ->
            if (!k.endsWith(PROPER_NAME)) {
                val s = v.toString()
                if (s.isNotEmpty()) lst.add(JSONObject(s))
            }
        }
        sp.edit().clear().apply()
        return lst
    }
}