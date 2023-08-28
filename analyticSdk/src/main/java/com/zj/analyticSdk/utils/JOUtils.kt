package com.zj.analyticSdk.utils

import com.zj.analyticSdk.CALogs
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("SameParameterValue", "unused")
object JOUtils {

    private const val yMdFormat = "yyyy-MM-dd"
    private const val yMdHmsFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    private val formatMaps = ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat?>>()

    fun mergeJSONObject(source: JSONObject, dest: JSONObject): JSONObject? {
        return try {
            val superPropertiesIterator = source.keys()
            while (superPropertiesIterator.hasNext()) {
                val key = superPropertiesIterator.next()
                val value = source[key]
                if (value is Date) {
                    dest.put(key, formatDate(value, Locale.getDefault()))
                } else {
                    dest.put(key, value)
                }
            }
            dest
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex);null
        }
    }

    private fun formatDate(date: Date, locale: Locale?): String {
        var formatString = ""
        val simpleDateFormat: SimpleDateFormat = getDateFormat(yMdHmsFormat, locale) ?: return formatString
        try {
            formatString = simpleDateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            CALogs.printStackTrace(e)
        }
        return formatString
    }


    fun getZoneOffset(): Int? {
        try {
            val cal = Calendar.getInstance(Locale.getDefault())
            val zoneOffset = cal[Calendar.ZONE_OFFSET] + cal[Calendar.DST_OFFSET]
            return -zoneOffset / (1000 * 60)
        } catch (ex: java.lang.Exception) {
            CALogs.printStackTrace(ex)
        }
        return null
    }


    @Synchronized
    private fun getDateFormat(patten: String, locale: Locale?): SimpleDateFormat? {
        var dateFormatThreadLocal: ThreadLocal<SimpleDateFormat?>? = formatMaps[patten]
        if (null == dateFormatThreadLocal) {
            dateFormatThreadLocal = object : ThreadLocal<SimpleDateFormat?>() {
                override fun initialValue(): SimpleDateFormat? {
                    var simpleDateFormat: SimpleDateFormat? = null
                    try {
                        simpleDateFormat = if (locale == null) {
                            SimpleDateFormat(patten, Locale.getDefault())
                        } else {
                            SimpleDateFormat(patten, locale)
                        }
                    } catch (e: java.lang.Exception) {
                        CALogs.printStackTrace(e)
                    }
                    return simpleDateFormat
                }
            }
            if (null != dateFormatThreadLocal.get()) {
                formatMaps[patten] = dateFormatThreadLocal
            }
        }
        return dateFormatThreadLocal.get()
    }

}