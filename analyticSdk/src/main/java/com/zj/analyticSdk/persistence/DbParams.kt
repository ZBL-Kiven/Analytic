package com.zj.analyticSdk.persistence

import android.content.Context
import android.content.UriMatcher
import android.net.Uri
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.persistence.data.PersistentChannel
import com.zj.analyticSdk.persistence.loader.BasePersistent
import com.zj.analyticSdk.persistence.loader.PersistentLoader
import java.lang.Exception


object DbParams {

    /* Name database */
    const val DATABASE_NAME = "cc_analytic_data"

    /* Database version number */
    const val DATABASE_VERSION = 1

    /* Event table fields */
    const val KEY_DATA = "data"

    /* Database status */
    const val DB_OUT_OF_MEMORY_ERROR = -2

    const val VALUE = "value"
    const val GZIP_DATA_EVENT = "1"
    const val GZIP_DATA_ENCRYPT = "9"
    const val DB_DELETE_ALL = "DB_DELETE_ALL"
    const val KEY_CREATED_AT = "created_at"

    private const val EVENTS = 1
    private const val CHANNEL = 2

    const val TABLE_EVENTS = "events"
    const val TABLE_CHANNEL = "channel"

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    fun init(context: Context) {
        val packageName: String = try {
            context.applicationContext.packageName
        } catch (e: UnsupportedOperationException) {
            "com.zj.analyticSdk"
        }
        val authority = "$packageName.${CCAnalyticsContentProvider::class.java.simpleName}"
        DbUriMap.init(packageName)
        DbUriMap.values().forEach {
            uriMatcher.addURI(authority, it.path, it.code)
        }
    }

    @JvmStatic
    fun matchWith(uri: Uri): DbUriMap {
        val code = uriMatcher.match(uri)
        return DbUriMap.values().first { it.code == code }
    }

    enum class DbUriMap(val code: Int, val path: String, val type: Class<*>) {

        //default data base
        TAB_EVENT(EVENTS, TABLE_EVENTS, PersistentLoader::class.java),

        //sp channel record
        TAB_CHANNEL(CHANNEL, TABLE_CHANNEL, PersistentChannel::class.java);

        companion object {

            lateinit var packageName: String
            private val cachedPersistent = hashMapOf<String, BasePersistent>()

            fun init(packageName: String) {
                this.packageName = packageName
            }
        }

        fun getLoader(context: Context?): BasePersistent? {

            val cached = cachedPersistent[this.path]
            if (cached != null) return cached

            if (context == null) return null
            val cls = this.type
            val constructor = try {
                cls.getDeclaredConstructor(Context::class.java, String::class.java)
            } catch (e: Exception) {
                CALogs.printStackTrace(e);null
            } ?: return null
            return constructor.newInstance(context, this.path) as BasePersistent
        }

        fun getUri(): Uri {
            return Uri.parse("content://$packageName.${CCAnalyticsContentProvider::class.java.simpleName}/$path")
        }
    }
}