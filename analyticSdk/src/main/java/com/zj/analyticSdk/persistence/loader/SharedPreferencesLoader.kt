package com.zj.analyticSdk.persistence.loader

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.*

internal class SharedPreferencesLoader {

    companion object {
        private const val prefsName = "com.zj.analyticSdk.analytics.android.SP"
    }

    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    fun loadPreferences(context: Context): Future<SharedPreferences> {
        val loadSharedPrefs = LoadSharedPreferences(context, prefsName)
        val task = FutureTask(loadSharedPrefs)
        mExecutor.execute(task)
        return task
    }

    private class LoadSharedPreferences(private val mContext: Context, private val mPrefsName: String) : Callable<SharedPreferences> {
        override fun call(): SharedPreferences {
            return mContext.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
        }
    }
}