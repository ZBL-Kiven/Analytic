@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.zj.analyticSdk.recorder

import android.os.Bundle
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticSdk.anno.PageInfo
import org.json.JSONObject
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

internal object AppUtils : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private const val TAG = "CCA.AppUtils"

    private var curActiveInfo: WeakReference<Activity>? = null
    private var application: Application? = null
    private var isInitLifecycleCallback = false
    private var runningTasksNum: Int = 0
    private var isAppInBackgroundCurrent = false

    private var curAppState = ""
    private var pausingPage: String = ""
    private var lastPage: String = ""
    private var curPage: String = ""

    fun init(context: Context) {
        if (isInitLifecycleCallback) return
        application = context as? Application
        if (application == null) application = context.applicationContext as? Application
        if (application == null) throw IllegalStateException("app status exception ! and the application context cannot be found through Context")
        isInitLifecycleCallback = application?.let {
            it.registerActivityLifecycleCallbacks(this)
            it.registerComponentCallbacks(this)
            true
        } ?: false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.e(TAG, "onActivityCreated: ")
        val pn = getActivityAnnotationsInfo(activity) ?: return
        CCAnalytic.get()?.startTimer(pn)
        curPage = getActivityAnnotationsInfo(activity) ?: ""
    }

    override fun onActivityStarted(activity: Activity) {
        Log.e(TAG, "onActivityStarted: ")
        val pn = getActivityAnnotationsInfo(activity) ?: return
        curPage = pn
        val ev = CCAnalytic.getConfig().getEventNameBuilder()
        val properties = JSONObject()
        properties.put(ev.activityPageNameEvent(), pn)
        properties.put(ev.activityReferPageEvent(), pausingPage)
        properties.put(ev.activityStartTimeEvent(), "${System.currentTimeMillis()}")
        AopParser.getCurFollowedPageParams(activity, properties)
        CCAnalytic.get()?.trackEvent(ev.activityPageStarted(), properties)
        CCAnalytic.get()?.resumeTimer(pn)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityResumed(activity: Activity) {
        Log.e(TAG, "onActivityResumed: ")
        curPage = getActivityAnnotationsInfo(activity) ?: ""
        runningTasksNum++
        if (isAppInBackgroundCurrent) {
            isAppInBackgroundCurrent = false
            curAppState = "foreground"
        }
        if (curActiveInfo?.get() != activity) {
            curActiveInfo = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e(TAG, "onActivityPaused: ")
        runningTasksNum = runningTasksNum--.coerceAtLeast(0)
        pausingPage = getActivityAnnotationsInfo(activity) ?: ""
    }

    override fun onActivityStopped(activity: Activity) {
        Log.e(TAG, "onActivityStopped: ")
        val pn = getActivityAnnotationsInfo(activity) ?: return
        CCAnalytic.get()?.pauseTimer(pn)
        val ev = CCAnalytic.getConfig().getEventNameBuilder()
        val event = CCAnalytic.get()?.finishTimer(pn)
        val properties = JSONObject()
        properties.put(ev.activityPageNameEvent(), pn)
        properties.put(ev.activityReferPageEvent(), lastPage)
        properties.put(ev.activityTimeDurationEvent(), "${event?.duration(TimeUnit.MILLISECONDS)}")
        properties.put(ev.activityStartTimeEvent(), "${event?.getCreateTime()}")
        properties.put(ev.activityEndTimeEvent(), "${System.currentTimeMillis()}")
        AopParser.getCurFollowedPageParams(activity, properties)
        CCAnalytic.get()?.trackEvent(ev.activityPageFinished(), properties)
        lastPage = pn
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.e(TAG, "onActivityDestroyed: ")
        if (curActiveInfo?.get() == activity) {
            curActiveInfo?.clear()
        }
    }

    override fun onLowMemory() {}

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onTrimMemory(level: Int) {
        if (!isAppInBackgroundCurrent && level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isAppInBackgroundCurrent = true
            curAppState = "background"
        }
    }

    fun getCurAppState(): String {
        return curAppState
    }

    fun getCurPageName(): String {
        return curPage
    }

    fun parseCurActParamInfo(obj: JSONObject) {
        curActiveInfo?.get()?.let {
            AopParser.getCurFollowedPageParams(it, obj)
        }
    }

    private fun getActivityAnnotationsInfo(activity: Activity): String? {
        val cls = activity::class.java
        if (cls.isAnnotationPresent(PageInfo::class.java)) {
            val ano = cls.getAnnotation(PageInfo::class.java)
            if (ano != null) {
                val pageName = ano.pageName
                if (pageName.isEmpty()) {
                    CALogs.i(TAG, "the pageName is empty for ${activity.javaClass.canonicalName}, the analytics may lose this route node!")
                } else {
                    return pageName
                }
            }
        } else {
            CALogs.i(TAG, "the annotation : PageInfo.class is not present in ${activity.javaClass.canonicalName} , the analytics may lose this route node!")
        }
        return null
    }

    fun destroy() {
        application?.unregisterActivityLifecycleCallbacks(this)
        application?.unregisterComponentCallbacks(this)
        isInitLifecycleCallback = false
        runningTasksNum = 0
    }
}
