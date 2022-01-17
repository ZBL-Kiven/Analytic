package com.zj.analyticSdk.recorder

import com.zj.analyticSdk.CCAnalytic
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate")
internal object PageTracker {

    private var cachedPageInfo: PageInfo? = null
    private var lastPageInfo: PageInfo? = null

    fun onPageStart(pageName: String, followedInfo: Any? = null, properties: JSONObject? = null) {
        when (pageName) {
            cachedPageInfo?.pageName -> {
                if (cachedPageInfo?.followedInfo?.get() != followedInfo) {
                    cachedPageInfo?.followedInfo = WeakReference(followedInfo)
                }
            }
            else -> {
                onPageEnd()
                CCAnalytic.get()?.startTimer(pageName)
                cachedPageInfo = PageInfo(pageName, WeakReference(followedInfo))
                val ev = CCAnalytic.getConfig().getEventNameBuilder()
                CCAnalytic.get()?.trackEvent(ev.onPageStarted(), properties ?: JSONObject())
            }
        }
    }

    fun onPageEnd(properties: JSONObject? = null, backgroundOnly: Boolean = false) {
        analyticPageLeave(properties = properties)
        if (!backgroundOnly && cachedPageInfo != null) {
            lastPageInfo = cachedPageInfo?.copy()
        }
        cachedPageInfo = null
    }

    fun analyticPageLeave(resetTimer: Boolean = false, properties: JSONObject? = null) {
        val pageName = cachedPageInfo?.pageName ?: return
        val prop = properties ?: JSONObject()
        val ev = CCAnalytic.getConfig().getEventNameBuilder()
        trackPageInfo(prop)
        val event = if (resetTimer) {
            CCAnalytic.get()?.pauseTimer(pageName)
            CCAnalytic.get()?.peekTimer(pageName)
        } else {
            CCAnalytic.get()?.finishTimer(pageName)
        }
        prop.put(ev.timeDurationEvent(), "${event?.duration(TimeUnit.MILLISECONDS)}")
        prop.put(ev.onEndTimeEvent(), "${System.currentTimeMillis()}")
        CCAnalytic.get()?.trackEvent(ev.onPageFinished(), prop)
    }

    fun trackPageInfo(properties: JSONObject): JSONObject {
        val ev = CCAnalytic.getConfig().getEventNameBuilder()
        if (properties.has(ev.pageNameEvent())) {
            val page = properties.optString(ev.pageNameEvent())
            if (page != cachedPageInfo?.pageName) return properties
        }
        val last = lastPageInfo
        val first = cachedPageInfo
        if (first != null) {
            val start = CCAnalytic.get()?.peekTimer(first.pageName)?.createTime
            properties.put(ev.pageNameEvent(), first.pageName)
            properties.put(ev.referPageEvent(), last?.pageName)
            properties.put(ev.onStartTimeEvent(), "$start")
            first.followedInfo?.get()?.let {
                AopParser.getCurFollowedPageParams(it, properties)
            }
        }
        return properties
    }

    private data class PageInfo(val pageName: String, var followedInfo: WeakReference<Any?>? = null) {
        fun copy(): PageInfo {
            return PageInfo(pageName, followedInfo)
        }
    }
}