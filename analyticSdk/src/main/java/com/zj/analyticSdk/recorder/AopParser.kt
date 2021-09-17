package com.zj.analyticSdk.recorder

import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.anno.PageAnalyticParams
import org.json.JSONObject
import java.lang.Exception

object AopParser {

    fun getCurFollowedPageParams(obj: Any, prop: JSONObject) {
        try {
            val cls = obj.javaClass
            val fields = cls.declaredFields
            for (f in fields) {
                if (!f.isAccessible) f.isAccessible = true
                if (f.isAnnotationPresent(PageAnalyticParams::class.java)) {
                    val ano = f.getAnnotation(PageAnalyticParams::class.java)
                    val elementName = ano.value
                    val value = try {
                        val fir = f.name.first().uppercaseChar()
                        val el = f.name.drop(1)
                        val getterName = "get$fir$el"
                        cls.getDeclaredMethod(getterName).invoke(obj)
                    } catch (e: Exception) {
                        f[obj]
                    }
                    prop.put(elementName, value)
                }
            }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
    }
}