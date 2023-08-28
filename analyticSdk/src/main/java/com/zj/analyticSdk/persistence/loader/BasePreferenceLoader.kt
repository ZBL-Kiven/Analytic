package com.zj.analyticSdk.persistence.loader

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs.i
import com.zj.analyticSdk.persistence.DbParams.DbUriMap
import java.lang.IllegalStateException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

@SuppressLint("CommitPrefEdits")
abstract class BasePreferenceLoader internal constructor(context: Context, private val persistentKey: String, private val serializer: PersistentSerializer) : BasePersistent(context) {

    private var item: Any? = null
    private val storedPreferences: Future<SharedPreferences>

    init {
        val sPrefsLoader = SharedPreferencesLoader()
        storedPreferences = sPrefsLoader.loadPreferences(context)
    }

    override fun insert(uri: Uri?, table: DbUriMap, values: ContentValues): Uri? {
        commit(values[table.path])
        return uri
    }

    override fun bulkInsert(uri: Uri?, table: DbUriMap, values: Array<ContentValues>): Int {
        throw IllegalStateException("the sp persistent cannot to use bilk insert !")
    }

    override fun query(uri: Uri, table: DbUriMap, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        val column = table.path
        val matrixCursor = MatrixCursor(arrayOf(column))
        val data = get()
        matrixCursor.addRow(arrayOf(data))
        return matrixCursor
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int {
        commit(null);return 0
    }

    private fun get(): Any? {
        if (item == null) {
            var data: String? = null
            synchronized(storedPreferences) {
                try {
                    val sharedPreferences = storedPreferences.get()
                    if (sharedPreferences != null) {
                        data = sharedPreferences.getString(persistentKey, null)
                    }
                } catch (e: ExecutionException) {
                    i(CAConfigs.LOG_SYSTEM, TAG, "Cannot read distinct ids from sharedPreferences.", e)
                } catch (e: InterruptedException) {
                    i(CAConfigs.LOG_SYSTEM, TAG, "Cannot read distinct ids from sharedPreferences.", e)
                }
                if (data == null) {
                    item = serializer.create()
                    commit(item)
                } else {
                    item = serializer.load(data)
                }
            }
        }
        return item
    }

    private fun commit(item: Any?) {
        this.item = item
        synchronized(storedPreferences) {
            var sharedPreferences: SharedPreferences? = null
            try {
                sharedPreferences = storedPreferences.get()
            } catch (e: ExecutionException) {
                i(CAConfigs.LOG_SYSTEM, TAG, "Cannot read distinct ids from sharedPreferences.", e)
            } catch (e: InterruptedException) {
                i(CAConfigs.LOG_SYSTEM, TAG, "Cannot read distinct ids from sharedPreferences.", e)
            }
            if (sharedPreferences == null) {
                return
            }
            val editor = sharedPreferences.edit()
            if (this.item == null) {
                this.item = serializer.create()
            }
            editor.putString(persistentKey, serializer.save(this.item))
            editor.apply()
        }
    }

    /**
     * Persistent serializers data
     */
    internal interface PersistentSerializer {
        fun load(value: String?): Any?
        fun save(item: Any?): String?
        fun create(): Any?
    }

    companion object {
        private const val TAG = "CCA.PersistentIdentity"
    }
}