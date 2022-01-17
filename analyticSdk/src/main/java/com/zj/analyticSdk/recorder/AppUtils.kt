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
import java.util.concurrent.atomic.AtomicBoolean

internal object AppUtils : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private const val TAG = "CCA.AppUtils"

    private var curActiveInfo: WeakReference<Activity>? = null
    private var application: Application? = null
    private var isInitLifecycleCallback = false
    private var runningTasksNum: Int = 0
    private var appStateListeners = mutableMapOf<String, StatusChangeListener>()
    private var inBackgroundCurrent = false
    private var curCode: Int = 0

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
        if (inBackgroundCurrent) {
            inBackgroundCurrent = false
            curAppState = "foreground"
        }
        if (curActiveInfo?.get() != activity) {
            curActiveInfo = WeakReference(activity)
        }
        notifyStateStopped(false, activity.hashCode())
    }

    override fun onActivityPaused(activity: Activity) {
        runningTasksNum = runningTasksNum--.coerceAtLeast(0)
    }

    override fun onActivityStopped(activity: Activity) {
        notifyStateStopped(true, activity.hashCode())
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (curActiveInfo?.get() == activity) {
            curActiveInfo?.clear()
        }
        if (runningTasksNum <= 0) {
            PageTracker.onPageEnd()
        }
    }

    override fun onLowMemory() {}

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onTrimMemory(level: Int) {
        if (!inBackgroundCurrent && level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            inBackgroundCurrent = true
            curAppState = "background"
            notifyStateStopped(true, curActiveInfo?.get()?.hashCode())
            PageTracker.onPageEnd(null, true)
        }
    }

    fun isAppInBackground(): Boolean {
        return curAppState == "background"
    }

    fun addOnAppStateChangeListener(name: String, l: StatusChangeListener) {
        this.appStateListeners[name] = l
    }

    fun removeAppStateChangeListener(name: String) {
        this.appStateListeners.remove(name)
    }

    private fun notifyStateStopped(value: Boolean, code: Int? = null) {
        appStateListeners.forEach {
            val v = it.value
            if (code == null || v.activityHashCode == code) {
                if (v.lastState.get() != value) {
                    v.lastState.set(value)
                    v.l.invoke(value)
                }
            }
        }
    }

    fun destroy() {
        application?.unregisterActivityLifecycleCallbacks(this)
        application?.unregisterComponentCallbacks(this)
        isInitLifecycleCallback = false
        runningTasksNum = 0
    }

    data class StatusChangeListener(val activityHashCode: Int, val l: (Boolean) -> Unit, var lastState: AtomicBoolean = AtomicBoolean(false))
}
