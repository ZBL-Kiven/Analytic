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

    /**
     * @see trackEvent
     * */
    @SafeVarargs
    fun trackEvent(eventName: String, vararg params: Pair<String, Any>, intermittentType: Boolean = false) {
        this.trackEvent(eventName, arrayToMap(*params), intermittentType = intermittentType)
    }

    /**
     * @see trackEvent
     * */
    fun trackEvent(eventName: String, params: Map<String, Any?>, intermittentType: Boolean = false) {
        this.trackEvent(eventName, JSONObject(params), intermittentType = intermittentType)
    }

    /**
     * @see trackEvent
     * */
    fun trackEvent(eventName: String, params: Map<String, Any?>, withData: Any?, intermittentType: Boolean = false) {
        this.trackEvent(eventName, JSONObject(params), withData, intermittentType = intermittentType)
    }

    /**
     * Called before a page cycle needs to be counted,
     * all subsequent events will be added to the properties of this page by default before [PageTracker.onPageEnd].
     * */
    fun trackPageStart(pageName: String, followedInfo: Any? = null, properties: JSONObject? = null) {
        PageTracker.onPageStart(pageName, followedInfo, properties)
    }

    /**
     * End the statistics of the current page,
     * usually it is automatically used by the next trackPageStart,
     * so, you needn't to call this separately in normal use.
     * */
    fun trackPageEnd(properties: JSONObject? = null) {
        PageTracker.onPageEnd(properties)
    }

    /**
     * Go back and restart to the previous page
     * @param withOldRefer Whether to reset the reference of the previous page
     * */
    fun restoreToLastPage(withOldRefer: Boolean = false, properties: JSONObject? = null) {
        PageTracker.endAndRestorePage(withOldRefer, properties)
    }

    /**
     * upload all recorded data now.
     * */
    fun flushAndUploadNow() {
        if (config.autoUploadAble()) {
            CALogs.e(CAConfigs.LOG_SYSTEM, "CCA.Test", "can not call upload with auto upload configuration running!")
            return
        }
        WorkManagerQueue.push(EventInfo.upload())
    }

    /**
     * Add data to the processing queue.
     * @param eventName The alias of the event, is often used to mark the event type separately. It will eventually be merged into the [jsonObject]
     * @param jsonObject data set
     * @param withData allows a special [Any] type parameter to be brought in. It will eventually Will be used in [CAConfigs.addDefaultParam].
     * @param intermittentType Mark it is dot type message, if true, it won't be added to the recorder queue.
     * if current event is exists, the new object will change the property value by the same keys.
     * cached events will remove the null value properties.
     * */
    fun trackEvent(eventName: String, jsonObject: JSONObject, withData: Any? = null, intermittentType: Boolean = false) {
        if (eventName.isNotEmpty() || jsonObject.length() > 0) {
            if (eventName != getConfig().getEventNameBuilder().onPageFinished()) {
                PageTracker.trackPageInfo(jsonObject)
            }
            val info = EventInfo.record(eventName, jsonObject, intermittentType, withData)
            WorkManagerQueue.push(info)
        }
    }

    fun flushIntermittentInfo(eventName: String) {
        WorkManagerQueue.push(EventInfo.flushIntermittentInfo(eventName))
    }

    fun flushAllIntermittentInfo() {
        WorkManagerQueue.push(EventInfo.flushIntermittentInfo())
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