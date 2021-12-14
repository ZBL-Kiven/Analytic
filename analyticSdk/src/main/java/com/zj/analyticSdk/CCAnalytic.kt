package com.zj.analyticSdk

import android.app.Application
import com.zj.analyticSdk.utils.AppInfoUtils
import com.zj.analyticSdk.utils.TimerTrackerUtils
import com.zj.analyticSdk.core.worker.EventInfo
import com.zj.analyticSdk.core.worker.WorkManagerQueue
import com.zj.analyticSdk.persistence.DBHelper
import com.zj.analyticSdk.persistence.encrypt.CCAnalyticsEncrypt
import com.zj.analyticSdk.recorder.AppUtils
import com.zj.analyticSdk.recorder.PageTracker
import com.zj.analyticSdk.utils.EventTimer
import org.json.JSONObject

@Suppress("MemberVisibilityCanBePrivate", "unused")
class CCAnalytic<T : CAConfigs>(private val config: T) {

    companion object {

        private lateinit var application: Application
        private lateinit var progressName: String

        private var instance: CCAnalytic<*>? = null

        internal fun getConfig(): CAConfigs {
            return instance?.config ?: throw NullPointerException("the config is null , are you called CCAnalytic.init at first? ")
        }

        internal fun isMainProgress(): Boolean {
            return AppInfoUtils.isMainProcess(application, progressName)
        }

        internal fun getApplication(): Application {
            if (::application.isInitialized) {
                return application
            } else throw NullPointerException("the application is null , are you called CCAnalytic.init at first? ")
        }

        fun init(app: Application, caConfigs: CAConfigs, dataEncrypt: CCAnalyticsEncrypt? = null) {
            this.application = app
            this.progressName = AppInfoUtils.getMainProcessName(app)
            AppUtils.init(app)
            DBHelper.getInstance(app, dataEncrypt)
            if (instance == null) {
                synchronized(CCAnalytic) {
                    if (instance == null) {
                        instance = CCAnalytic(caConfigs)
                    }
                }
            }
        }

        fun get(): CCAnalytic<*>? {
            return instance
        }
    }

    fun startTimer(eventName: String) {
        TimerTrackerUtils.startNewTimer(eventName)
    }

    fun pauseTimer(eventName: String) {
        TimerTrackerUtils.pauseTimer(eventName)
    }

    fun resumeTimer(eventName: String) {
        TimerTrackerUtils.resumeTimer(eventName)
    }

    fun finishTimer(eventName: String): EventTimer? {
        return TimerTrackerUtils.endTimer(eventName)
    }

    internal fun peekTimer(eventName: String): EventTimer? {
        return TimerTrackerUtils.peekTimer(eventName)
    }

    @SafeVarargs
    fun trackEvent(eventName: String, vararg params: Pair<String, Any>) {
        this.trackEvent(eventName, arrayToMap(*params))
    }

    fun trackEvent(eventName: String, params: Map<String, Any?>) {
        this.trackEvent(eventName, JSONObject(params))
    }

    fun trackEvent(eventName: String, params: Map<String, Any?>, withData: Any?) {
        this.trackEvent(eventName, JSONObject(params), withData)
    }

    fun trackPageStart(pageName: String, followedInfo: Any? = null, properties: JSONObject? = null) {
        PageTracker.onPageStart(pageName, followedInfo, properties)
    }

    fun trackPageEnd(properties: JSONObject? = null) {
        PageTracker.onPageEnd(properties)
    }

    fun uploadTest() {
        if (config.autoUploadAble()) {
            CALogs.e("CCA.Test", "can not call upload with auto upload configuration running!")
            return
        }
        WorkManagerQueue.push(EventInfo.upload())
    }

    fun trackEvent(eventName: String, jsonObject: JSONObject, withData: Any? = null) {
        if (eventName.isNotEmpty() || jsonObject.length() > 0) {
            val info = EventInfo.record(eventName, jsonObject, withData)
            WorkManagerQueue.push(info)
        }
    }

    @SafeVarargs
    private fun arrayToMap(vararg paramsDefault: Pair<String, Any>?): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        if (!paramsDefault.isNullOrEmpty()) {
            for (p in paramsDefault) {
                if (p != null) {
                    map[p.first] = p.second
                }
            }
        }
        return map
    }
}