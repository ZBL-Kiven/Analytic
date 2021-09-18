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
class DBHelper private constructor(context: Context, dataEncrypt: CCAnalyticsEncrypt? = null) {

    init {
        DbParams.init(context)
    }

    private var mTrackEventOperation: DataOperation = if (dataEncrypt != null) {
        EncryptDataOperation(context.applicationContext, dataEncrypt)
    } else {
        EventDataOperation(context.applicationContext)
    }
    private val mPersistentOperation = PersistentDataOperation(context.applicationContext)

    companion object {
        private var instance: DBHelper? = null

        fun getInstance(context: Context, dataEncrypt: CCAnalyticsEncrypt?): DBHelper {
            if (instance == null) {
                instance = DBHelper(context, dataEncrypt)
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
        val code: Int = mTrackEventOperation.insertData(DbParams.DbUriMap.TAB_EVENT.getUri(), j)
        return if (code == 0) {
            mTrackEventOperation.queryDataCount(DbParams.DbUriMap.TAB_EVENT.getUri())
        } else code
    }

    /**
     * Removes all events from table
     */
    fun deleteAllEvents() {
        mTrackEventOperation.deleteData(DbParams.DbUriMap.TAB_EVENT.getUri(), DbParams.DB_DELETE_ALL)
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param lastId the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupEvents(lastId: String): Int {
        mTrackEventOperation.deleteData(DbParams.DbUriMap.TAB_EVENT.getUri(), lastId)
        return mTrackEventOperation.queryDataCount(DbParams.DbUriMap.TAB_EVENT.getUri())
    }

    fun commitChannelInfo(channel: String?) {
        try {
            mPersistentOperation.insertData(DbParams.DbUriMap.TAB_CHANNEL.getUri(), JSONObject().put(DbParams.VALUE, channel))
        } catch (e: JSONException) {
            CALogs.printStackTrace(e)
        }
    }

    val channelInfo: String
        get() {
            val values = mPersistentOperation.queryData(DbParams.DbUriMap.TAB_CHANNEL.getUri(), 1)
            return "" tryAnother { values?.get(0) }
        }

    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(limit: Int): Array<String?>? {
        return mTrackEventOperation.queryData(DbParams.DbUriMap.TAB_EVENT.getUri(), limit)
    }

}