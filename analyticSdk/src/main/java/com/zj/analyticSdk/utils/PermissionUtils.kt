package com.zj.analyticSdk.utils

import android.content.Context
import android.content.pm.PackageManager
import com.zj.analyticSdk.CALogs

object PermissionUtils {

    private const val TAG = "CCA.Utils"

    @JvmStatic
    fun checkHasPermission(context: Context, permission: String): Boolean {
        return try {
            var contextCompat: Class<*>? = null
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat")
            } catch (e: Exception) {
            }
            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat")
                } catch (e: Exception) {
                }
            }
            if (contextCompat == null) {
                return true
            }
            val checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", Context::class.java, String::class.java)
            val result = checkSelfPermissionMethod.invoke(null, *arrayOf(context, permission)) as Int
            if (result != PackageManager.PERMISSION_GRANTED) {
                CALogs.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"$permission\" />")
                return false
            }
            true
        } catch (e: Exception) {
            CALogs.i(TAG, e.toString())
            true
        }
    }
}