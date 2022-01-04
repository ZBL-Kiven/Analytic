package com.zj.analyticSdk.persistence.sp

import android.content.Context
import android.content.SharedPreferences
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.persistence.DbParams
import org.json.JSONObject

internal object SpUtils {

    private val sp: SharedPreferences by lazy {
        val app = CCAnalytic.getApplication()
        return@lazy app.getSharedPreferences(DbParams.DATABASE_NAME, Context.MODE_PRIVATE)
    }

    infix fun String.saveObject(element: JSONObject) {
        val s = element.toString(2)
        kotlin.runCatching {
            sp.edit().putString(this, s).apply()
        }
    }

    fun String.poll(): JSONObject? {
        val s = kotlin.runCatching {
            val s = sp.getString(this, null) ?: return null
            JSONObject(s)
        }.getOrNull()
        sp.edit().remove(this).apply()
        return s
    }

    fun pollAll(): List<JSONObject> {
        val lst = arrayListOf<JSONObject>()
        sp.all?.forEach {
            val s = it.value.toString()
            if (s.isNotEmpty()) lst.add(JSONObject(s))
        }
        sp.edit().clear().apply()
        return lst
    }
}