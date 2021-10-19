package com.zj.analyticSdk.utils

import android.os.SystemClock
import com.zj.analyticSdk.CALogs
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Suppress("unused")
internal object TimerTrackerUtils {

    private val mTrackTimer: ConcurrentHashMap<String, EventTimer> = ConcurrentHashMap()

    fun startNewTimer(name: String) {
        mTrackTimer[name] = EventTimer()
    }

    fun pauseTimer(name: String) {
        mTrackTimer[name]?.setTimerState(true)
    }

    fun resumeTimer(name: String) {
        if (mTrackTimer.containsKey(name)) {
            mTrackTimer[name]?.setTimerState(false)
        } else {
            startNewTimer(name)
        }
    }

    fun endTimer(name: String): EventTimer? {
        return mTrackTimer.remove(name)
    }

    fun peekTimer(name: String): EventTimer? {
        return mTrackTimer[name]
    }

    fun getTimerAccumulateDuration(name: String, timeUnit: TimeUnit): Long {
        return mTrackTimer[name]?.duration(timeUnit) ?: 0L
    }
}

class EventTimer {

    var createTime: Long = System.currentTimeMillis()
    private var startTime: Long = SystemClock.elapsedRealtime()
    private var endTime: Long = -1
    private var eventAccumulatedDuration: Long = 0
    private var isPaused = false

    fun duration(timeUnit: TimeUnit): Long {
        endTime = if (isPaused) {
            startTime
        } else {
            if (endTime < 0) SystemClock.elapsedRealtime() else endTime
        }
        val duration = endTime - startTime + eventAccumulatedDuration
        return try {
            if (duration < 0 || duration > 24 * 60 * 60 * 1000) {
                return 0L
            }
            val durationFloat: Float = when (timeUnit) {
                TimeUnit.MILLISECONDS -> {
                    duration.toFloat()
                }
                TimeUnit.SECONDS -> {
                    duration / 1000.0f
                }
                TimeUnit.MINUTES -> {
                    duration / 1000.0f / 60.0f
                }
                TimeUnit.HOURS -> {
                    duration / 1000.0f / 60.0f / 60.0f
                }
                else -> {
                    duration.toFloat()
                }
            }
            if (durationFloat < 0) 0L else durationFloat.toLong()
        } catch (e: Exception) {
            CALogs.printStackTrace(e);0L
        }
    }

    fun setTimerState(isPaused: Boolean) {
        this.isPaused = isPaused
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (isPaused) {
            eventAccumulatedDuration = eventAccumulatedDuration + elapsedRealtime - startTime
        }
        startTime = elapsedRealtime
    }
}