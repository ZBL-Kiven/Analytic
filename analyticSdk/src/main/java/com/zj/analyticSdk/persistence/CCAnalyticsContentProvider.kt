package com.zj.analyticSdk.persistence

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.zj.analyticSdk.persistence.DbParams.matchWith

class CCAnalyticsContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val tab = matchWith(uri)
        return tab.getLoader(context)?.insertToUri(uri, tab, values)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val tab = matchWith(uri)
        return tab.getLoader(context)?.delete(uri, selection, selectionArgs) ?: 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val tab = matchWith(uri)
        return tab.getLoader(context)?.bulkInsertToUri(uri, tab, values) ?: 0
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val tab = matchWith(uri)
        return tab.getLoader(context)?.queryByUri(uri, tab, projection, selection, selectionArgs, sortOrder)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}