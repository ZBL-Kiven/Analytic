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

    /**
     * the event name of page name, after [onPageStarted].
     * @see CCAnalytic.trackPageStart
     * */
    fun pageNameEvent(): String = "page_name"

    /**
     * the referenced page's event name for current. where it from.
     * @see CCAnalytic.trackPageStart
     * @see CCAnalytic.trackPageEnd
     * */
    fun referPageEvent(): String = "refer_page_name"

    /**
     * one of the page properties event name after [onPageStarted].
     * default used for page stay duration
     * @see CCAnalytic.trackPageStart
     * */
    fun timeDurationEvent(): String = "duration"

    /**
     * one of the page properties event name after [onPageStarted]. quit_page_time as the default.
     * @see CCAnalytic.trackPageStart
     * */
    fun onEndTimeEvent(): String = "quit_page_time"

    /**
     * called on [onPageStarted] . as the event name of entering page time.
     * @see CCAnalytic.trackPageStart
     * */
    fun onStartTimeEvent(): String = "enter_page_time"

    /**
     * events name default for [CCAnalytic.trackPageStart].
     * @see CCAnalytic.trackPageStart
     * */
    fun onPageStarted(): String = "enter_page"

    /**
     * events name default for [CCAnalytic.trackPageEnd].
     * @see CCAnalytic.trackPageEnd
     * */
    fun onPageFinished(): String = "view_page"

    fun appVersionName(): String = "app_version"

    fun osType(): String = "os"

    fun eventName(): String = "event_name"

    /**
     * property event name of event happening time.
     * */
    fun eventTime(): String = "event_time"

    fun eventId(): String = "event_record_id"
}

object DefaultEventNameBuilder : EventNameBuilder