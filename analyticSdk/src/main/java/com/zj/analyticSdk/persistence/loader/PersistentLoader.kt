package com.zj.analyticSdk.persistence.loader

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.zj.analyticSdk.persistence.data.*
import java.util.concurrent.Future

internal class PersistentLoader private constructor(context: Context) {

    init {
        val sPrefsLoader = SharedPreferencesLoader()
        storedPreferences = sPrefsLoader.loadPreferences(context)
    }

    companion object {
        @Volatile private var instance: PersistentLoader? = null

        private var storedPreferences: Future<SharedPreferences>? = null

        @JvmStatic
        fun initLoader(context: Context) {
            if (instance == null) {
                instance = PersistentLoader(context)
            }
        }

        @JvmStatic
        fun loadPersistent(persistentKey: String?): PersistentIdentity<*>? {
            if (instance == null) {
                throw RuntimeException("you should call 'PersistentLoader.initLoader(Context)' first")
            }
            return if (TextUtils.isEmpty(persistentKey)) {
                null
            } else when (persistentKey) {
                PersistentName.APP_END_TIME -> PersistentAppEnd(storedPreferences)
                PersistentName.APP_START_TIME -> PersistentAppStartTime(storedPreferences)
                PersistentName.LOGIN_ID -> PersistentLoginId(storedPreferences)
                PersistentName.DEVICE_ID -> PersistentDeviceId(storedPreferences)
                else -> null
            }
        }
    }
}