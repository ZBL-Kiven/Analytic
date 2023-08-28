package com.zj.analyticSdk.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import com.zj.analyticSdk.CALogs.printStackTrace
import com.zj.analyticSdk.utils.PermissionUtils.checkHasPermission

internal object NetworkUtils {

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun networkType(context: Context): String {

        return try {
            if (!checkHasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return "NULL"
            } // Wifi
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = manager.activeNetwork
                    if (network != null) {
                        val capabilities = manager.getNetworkCapabilities(network)
                        if (capabilities != null) {
                            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return "WIFI"
                            } else if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                                return "NULL"
                            }
                        }
                    } else {
                        return "NULL"
                    }
                } else {
                    var networkInfo = manager.activeNetworkInfo
                    if (networkInfo == null || !networkInfo.isConnected) {
                        return "NULL"
                    }
                    networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
                        return "WIFI"
                    }
                }
            }

            // Mobile network
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkType = telephonyManager.networkType
            if (networkType == TelephonyManager.NETWORK_TYPE_GPRS || networkType == TelephonyManager.NETWORK_TYPE_EDGE || networkType == TelephonyManager.NETWORK_TYPE_CDMA || networkType == TelephonyManager.NETWORK_TYPE_1xRTT || networkType == TelephonyManager.NETWORK_TYPE_IDEN) {
                return "2G"
            } else if (networkType == TelephonyManager.NETWORK_TYPE_UMTS || networkType == TelephonyManager.NETWORK_TYPE_EVDO_0 || networkType == TelephonyManager.NETWORK_TYPE_EVDO_A || networkType == TelephonyManager.NETWORK_TYPE_HSDPA || networkType == TelephonyManager.NETWORK_TYPE_HSUPA || networkType == TelephonyManager.NETWORK_TYPE_HSPA || networkType == TelephonyManager.NETWORK_TYPE_EVDO_B || networkType == TelephonyManager.NETWORK_TYPE_EHRPD || networkType == TelephonyManager.NETWORK_TYPE_HSPAP) {
                return "3G"
            } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                return "4G"
            } else if (networkType == TelephonyManager.NETWORK_TYPE_NR) {
                return "5G"
            }
            "NULL"
        } catch (e: Exception) {
            "NULL"
        }
    }

    /**
     * 是否有可用网络
     *
     * @param context Context
     * @return true：网络可用，false：网络不可用
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {
        return if (!checkHasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            false
        } else try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = cm.activeNetwork
                    if (network != null) {
                        val capabilities = cm.getNetworkCapabilities(network)
                        if (capabilities != null) {
                            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                        }
                    }
                } else {
                    val networkInfo = cm.activeNetworkInfo
                    return networkInfo != null && networkInfo.isConnected
                }
            }
            false
        } catch (e: Exception) {
            printStackTrace(e)
            false
        }
    }

    fun isShouldFlush(networkType: String, flushNetworkPolicy: Int): Boolean {
        return toNetworkType(networkType) and flushNetworkPolicy != 0
    }

    private fun toNetworkType(networkType: String): Int {

        when (networkType) {
            "NULL" -> {
                return NetworkType.TYPE_ALL
            }
            "WIFI" -> {
                return NetworkType.TYPE_WIFI
            }
            "2G" -> {
                return NetworkType.TYPE_2G
            }
            "3G" -> {
                return NetworkType.TYPE_3G
            }
            "4G" -> {
                return NetworkType.TYPE_4G
            }
            "5G" -> {
                return NetworkType.TYPE_5G
            }
            else -> return NetworkType.TYPE_ALL
        }
    }

    object NetworkType {
        const val TYPE_2G = 1 //2G
        const val TYPE_3G = 1 shl 1 //3G
        const val TYPE_4G = 1 shl 2 //4G
        const val TYPE_WIFI = 1 shl 3 //WIFI
        const val TYPE_5G = 1 shl 4 //5G
        const val TYPE_ALL = 0xFF //ALL
    }
}