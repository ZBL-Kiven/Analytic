package com.zj.analyticSdk

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicInteger

object UTL {

    private const val SHARED_PREF_EDITS_FILE = "sp_cc_analytics_file"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREF_EDITS_FILE, Context.MODE_PRIVATE)
    }
}

internal infix fun <T> T.tryAnother(el: () -> T?): T {
    return try {
        el() ?: this
    } catch (e: Exception) {
        CALogs.printStackTrace(e);this
    }
}

internal infix fun AtomicInteger.leastDownTo(another: Int) {
    val v = (this.get() - 1).coerceAtLeast(another)
    set(v)
}

internal infix fun AtomicInteger.plus(another: Int) {
    val v = this.get() + another
    set(v)
}


interface EventNameBuilder {

    fun activityPageNameEvent(): String = "page_name"

    fun activityReferPageEvent(): String = "refer_page_name"

    fun activityTimeDurationEvent(): String = "duration"

    fun activityEndTimeEvent(): String = "quit_page_time"

    fun activityStartTimeEvent(): String = "enter_page_time"

    fun activityPageStarted(): String = "enter_page"

    fun activityPageFinished(): String = "view_page"

    fun appVersionName(): String = "app_version_name"

    fun osType(): String = "os"

    fun eventName(): String = "event_name"

    fun eventTime(): String = "event_time"

    fun eventId(): String = "event_record_id"
}

object DefaultEventNameBuilder : EventNameBuilder