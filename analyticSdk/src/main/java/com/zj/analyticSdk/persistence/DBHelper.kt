package com.zj.analyticSdk.persistence

import android.content.Context
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.persistence.encrypt.CCAnalyticsEncrypt
import com.zj.analyticSdk.persistence.operator.DataOperation
import com.zj.analyticSdk.persistence.operator.EncryptDataOperation
import com.zj.analyticSdk.persistence.operator.EventDataOperation
import com.zj.analyticSdk.persistence.operator.PersistentDataOperation
import com.zj.analyticSdk.tryAnother
import org.json.JSONException
import org.json.JSONObject

@Suppress("unused")
class DBHelper private constructor(context: Context, packageName: String, dataEncrypt: CCAnalyticsEncrypt? = null) {

    private val mDbParams: DbParams = DbParams.getInstance(packageName)

    private var mTrackEventOperation: DataOperation = if (dataEncrypt != null) {
        EncryptDataOperation(context.applicationContext, dataEncrypt)
    } else {
        EventDataOperation(context.applicationContext)
    }
    private val mPersistentOperation = PersistentDataOperation(context.applicationContext)

    companion object {
        private var instance: DBHelper? = null

        fun getInstance(context: Context, packageName: String, dataEncrypt: CCAnalyticsEncrypt?): DBHelper {
            if (instance == null) {
                instance = DBHelper(context, packageName, dataEncrypt)
            }
            return instance!!
        }

        fun getInstance(): DBHelper {
            checkNotNull(instance) { "The static method getInstance(Context context, String packageName) should be called before calling getInstance()" }
            return instance!!
        }
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j the JSON to record
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    fun addJSON(j: JSONObject): Int {
        val code: Int = mTrackEventOperation.insertData(mDbParams.eventUri, j)
        return if (code == 0) {
            mTrackEventOperation.queryDataCount(mDbParams.eventUri)
        } else code
    }

    /**
     * Removes all events from table
     */
    fun deleteAllEvents() {
        mTrackEventOperation.deleteData(mDbParams.eventUri, DbParams.DB_DELETE_ALL)
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param lastId the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupEvents(lastId: String): Int {
        mTrackEventOperation.deleteData(mDbParams.eventUri, lastId)
        return mTrackEventOperation.queryDataCount(mDbParams.eventUri)
    }

    fun commitAppStartTime(appStartTime: Long) {
        try {
            mPersistentOperation.insertData(mDbParams.appStartTimeUri, JSONObject().put(DbParams.VALUE, appStartTime))
        } catch (e: JSONException) {
            CALogs.printStackTrace(e)
        }
    }

    val appStartTime: Long
        get() {
            val values = mPersistentOperation.queryData(mDbParams.appStartTimeUri, 1)
            return 0L tryAnother { values?.get(0)?.toLong() }
        }

    fun commitAppEndTime(appEndTime: Long) {
        try {
            mPersistentOperation.insertData(mDbParams.appEndUri, JSONObject().put(DbParams.VALUE, appEndTime))
        } catch (e: JSONException) {
            CALogs.printStackTrace(e)
        }
    }

    val appEndTime: Long
        get() {
            val values = mPersistentOperation.queryData(mDbParams.appEndUri, 1)
            return 0L tryAnother { values?.get(0)?.toLong() }
        }

    fun commitLoginId(loginId: String?) {
        try {
            mPersistentOperation.insertData(mDbParams.loginIdUri, JSONObject().put(DbParams.VALUE, loginId))
        } catch (e: JSONException) {
            CALogs.printStackTrace(e)
        }
    }

    val loginId: String
        get() {
            val values = mPersistentOperation.queryData(mDbParams.loginIdUri, 1)
            return "" tryAnother { values?.get(0) }
        }

    fun commitDeviceId(deviceId: String?) {
        try {
            mPersistentOperation.insertData(mDbParams.deviceIdUri, JSONObject().put(DbParams.VALUE, deviceId))
        } catch (e: JSONException) {
            CALogs.printStackTrace(e)
        }
    }

    val deviceId: String
        get() {
            val values = mPersistentOperation.queryData(mDbParams.deviceIdUri, 1)
            return "" tryAnother { values?.get(0) }
        }

    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(limit: Int): Array<String?>? {
        return mTrackEventOperation.queryData(mDbParams.eventUri, limit)
    }

}