@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.zj.analyticSdk.recorder

import android.os.Bundle
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

internal object AppUtils : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private const val TAG = "CCA.AppUtils"

    private var curActiveInfo: WeakReference<Activity>? = null
    private var application: Application? = null
    private var isInitLifecycleCallback = false
    private var runningTasksNum: Int = 0
    private var isAppInBackgroundCurrent = false

    private var curAppState = ""

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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityResumed(activity: Activity) {
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
        runningTasksNum = runningTasksNum--.coerceAtLeast(0)
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (curActiveInfo?.get() == activity) {
            curActiveInfo?.clear()
        }
        if (runningTasksNum <= 0) {
            PageTracker.analyticPageLeave(true)
        }
    }

    override fun onLowMemory() {}

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onTrimMemory(level: Int) {
        if (!isAppInBackgroundCurrent && level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isAppInBackgroundCurrent = true
            curAppState = "background"
            PageTracker.analyticPageLeave(true)
        }
    }

    fun getCurAppState(): String {
        return curAppState
    }

    fun destroy() {
        application?.unregisterActivityLifecycleCallbacks(this)
        application?.unregisterComponentCallbacks(this)
        isInitLifecycleCallback = false
        runningTasksNum = 0
    }
}
