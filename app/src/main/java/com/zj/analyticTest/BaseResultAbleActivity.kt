package com.zj.analyticTest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import java.io.Serializable

@Suppress("unused")
open class BaseResultAbleActivity : AppCompatActivity() {

    private val activityResultLauncher: ActivityResultLauncher<RequestInfo>

    init {
        activityResultLauncher = this.registerForActivityResult(CusActivityContract(), ::onActivityResult)
    }

    companion object {

        private val onRequestActivityState = mutableMapOf<String, MutableMap<Int, (Int, Int, Intent?) -> Unit>>()

        @JvmStatic
        @MainThread
        fun BaseResultAbleActivity.startActivity(clsPath: String, flags: Int? = null, params: Array<Pair<String, Any?>>? = null) {
            val intent = Intent()
            val cn = ComponentName(this, clsPath)
            intent.component = cn
            if (flags != null) intent.flags = flags
            params?.forEach {
                val value = it.second
                if (value is Serializable) {
                    intent.putExtra(it.first, value)
                } else {
                    throw IllegalArgumentException("intent params as must be a Serializable object!!")
                }
            }
            this.startActivity(intent)
        }

        /**
         * This method provides a new mechanism for Activity jump.
         * @param run If you need to get the return result of the upper layer from the callback,
         * the upper layer needs to use the current Intent when setResult instead of new Intent
         * 否则，onActivityForResult 将回调至 [onResult]
         * */
        @JvmStatic
        @MainThread
        fun BaseResultAbleActivity.startActivityForResult(clsPath: String, requestCode: Int, flags: Int? = null, activityOptionsCompat: ActivityOptionsCompat? = null, params: Array<Pair<String, Any?>>? = null, run: (Int, Int, Intent?) -> Unit) {
            val param = mutableMapOf<String, Any?>()
            params?.forEach { param[it.first] = it.second }
            if (!onRequestActivityState.containsKey(clsPath)) {
                onRequestActivityState[clsPath] = mutableMapOf()
            }
            val rqCods = arrayListOf(requestCode)
            val cached = onRequestActivityState[clsPath]
            if (!cached.isNullOrEmpty()) {
                cached.keys.forEach {
                    rqCods.add(it)
                }
            }
            onRequestActivityState[clsPath]?.put(requestCode, run)
            val input = RequestInfo(clsPath, rqCods, flags, param)
            this.activityResultLauncher.launch(input, activityOptionsCompat)
        }

        /**
         * @see startActivityForResult
         **/
        @JvmStatic
        @MainThread
        fun BaseResultAbleActivity.startActivityForResult(cls: Class<*>, requestCode: Int, flags: Int? = null, activityOptionsCompat: ActivityOptionsCompat? = null, params: Array<Pair<String, Any?>>? = null, run: (Int, Int, Intent?) -> Unit) {
            this.startActivityForResult(cls::class.java.name, requestCode, flags, activityOptionsCompat, params, run)
        }
    }

    private fun onActivityResult(result: Triple<RequestInfo, Int, Intent?>) {
        val rqInfo = result.first
        val rcl = onRequestActivityState[rqInfo.cls]
        if (!rcl.isNullOrEmpty()) {
            rqInfo.requestCods.forEach {
                rcl.remove(it)?.invoke(it, result.second, result.third) ?: onResult(it, result.second, result.third)
            }
            if (rcl.isEmpty()) {
                onRequestActivityState.remove(rqInfo.cls)
            }
        }
    }

    open fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("RBaseActivity.onResult", "the activity requestCode $requestCode No suitable callback interface is found, the data will complete the callback in this extension function!")
    }

    private data class RequestInfo(val cls: String, val requestCods: MutableList<Int>, val flags: Int?, val params: Map<String, Any?>)

    private class CusActivityContract : ActivityResultContract<RequestInfo, Triple<RequestInfo, Int, Intent?>>() {

        private lateinit var rqInfo: RequestInfo

        override fun createIntent(context: Context, input: RequestInfo): Intent {
            val i = Intent()
            val cn = ComponentName(context, input.cls)
            i.component = cn
            input.flags?.let { i.flags = it }
            input.params.forEach {
                val value = it.value
                if (value is Serializable) {
                    i.putExtra(it.key, value)
                } else {
                    throw IllegalArgumentException("intent params as must be a Serializable object!!")
                }
            }
            this.rqInfo = input
            return i
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Triple<RequestInfo, Int, Intent?> {
            return Triple(rqInfo, resultCode, intent)
        }
    }

    override fun onDestroy() {
        activityResultLauncher.unregister()
        super.onDestroy()
    }
}