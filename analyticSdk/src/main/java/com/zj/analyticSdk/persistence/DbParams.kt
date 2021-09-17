package com.zj.analyticSdk.persistence

import android.net.Uri

class DbParams private constructor(packageName: String) {


    companion object {
        /**
         * Table name in the database
         */
        const val TABLE_EVENTS = "events"

        /**
         * Name database
         */
        const val DATABASE_NAME = "cc_analytic_data"

        /**
         * Database version number
         */
        const val DATABASE_VERSION = 1

        /**
         * Event table fields
         */
        const val KEY_DATA = "data"

        /**
         * Database status
         */
        const val DB_OUT_OF_MEMORY_ERROR = -2
        const val VALUE = "value"
        const val GZIP_DATA_EVENT = "1"
        const val GZIP_DATA_ENCRYPT = "9"
        const val DB_DELETE_ALL = "DB_DELETE_ALL"
        const val KEY_CREATED_AT = "created_at"
        const val TABLE_APP_START_TIME = "app_start_time"
        const val TABLE_APP_END_TIME = "app_end_time"
        const val TABLE_DEVICE_ID = "app_device_id"
        const val TABLE_LOGIN_ID = "events_login_id"
        private var instance: DbParams? = null

        fun getInstance(packageName: String): DbParams {
            if (instance == null) {
                instance = DbParams(packageName)
            }
            return instance!!
        }
    }

    /**
     * get Event Uri
     */
    val eventUri: Uri = Uri.parse("content://$packageName.CCAnalyticsContentProvider/$TABLE_EVENTS")

    /**
     * get AppStartTime Uri
     */
    val appStartTimeUri: Uri = Uri.parse("content://$packageName.CCAnalyticsContentProvider/$TABLE_APP_START_TIME")

    /**
     * get AppEndTime Uri
     */
    val appEndUri: Uri = Uri.parse("content://$packageName.CCAnalyticsContentProvider/$TABLE_APP_END_TIME")

    /**
     * get LoginId 的 Uri
     */
    val loginIdUri: Uri = Uri.parse("content://$packageName.CCAnalyticsContentProvider/$TABLE_LOGIN_ID")

    /**
     * get deviceId 的 Uri
     */
    val deviceIdUri: Uri = Uri.parse("content://$packageName.CCAnalyticsContentProvider/$TABLE_DEVICE_ID")

}