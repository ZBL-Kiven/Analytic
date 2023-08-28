package com.zj.analyticSdk.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils
import com.zj.analyticSdk.CALogs.printStackTrace

internal object AppInfoUtils {

    /**
     * 获取 App 的 ApplicationId
     *
     * @param context Context
     * @return ApplicationId
     */
    private fun getProcessName(context: Context?): String {
        if (context == null) return ""
        try {
            return context.applicationInfo.processName
        } catch (ex: Exception) {
            printStackTrace(ex)
        }
        return ""
    }

    /**
     * 获取 App 版本号
     *
     * @param context Context
     * @return App 的版本号
     */
    fun getAppVersionName(context: Context?): String {
        if (context == null) return ""
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: Exception) {
            printStackTrace(e)
        }
        return ""
    }

    /**
     * 获取主进程的名称
     *
     * @param context Context
     * @return 主进程名称
     */
    fun getMainProcessName(context: Context?): String {
        if (context == null) {
            return ""
        }
        var progressName = ""
        try {
            progressName = getProcessName(context)
        } catch (ex: Exception) {
            printStackTrace(ex)
        }
        if (TextUtils.isEmpty(progressName)) {
            val packageName = context.applicationContext.packageName
            val appInfo = context.applicationContext.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            progressName = appInfo.metaData?.getString("com.zj.analyticSdk.analytics.android.MainProcessName") ?: ""
        }
        return progressName
    }

    /**
     * 判断当前进程名称是否为主进程
     *
     * @param context         Context
     * @param mainProcessName 进程名
     * @return 是否主进程
     */
    fun isMainProcess(context: Context?, mainProcessName: String): Boolean {
        if (context == null) {
            return false
        }
        if (TextUtils.isEmpty(mainProcessName)) {
            return true
        }
        val currentProcess = getCurrentProcessName(context.applicationContext)
        return TextUtils.isEmpty(currentProcess) || mainProcessName == currentProcess
    }

    /**
     * 获得当前进程的名字
     *
     * @param context Context
     * @return 进程名称
     */
    private fun getCurrentProcessName(context: Context): String? {
        try {
            val pid = Process.myPid()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
            val runningAppProcessInfoList = activityManager.runningAppProcesses
            if (runningAppProcessInfoList != null) {
                for (appProcess in runningAppProcessInfoList) {
                    if (appProcess != null) {
                        if (appProcess.pid == pid) {
                            return appProcess.processName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            printStackTrace(e)
        }
        return null
    }
}