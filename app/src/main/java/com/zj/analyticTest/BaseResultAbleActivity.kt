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

open class BaseResultAbleActivity : AppCompatActivity() {

    private val activityResultLauncher: ActivityResultLauncher<RequestInfo>

    private data class RequestInfo(val cls: String, val requestCode: Int, val flags: Int?, val params: Map<String, Any?>)
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
            val input = RequestInfo(clsPath, requestCode, flags, param)
            onRequestActivityState[clsPath]?.put(requestCode, run)
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

    init {
        activityResultLauncher = this.registerForActivityResult(object : ActivityResultContract<RequestInfo, Triple<Int, Int, Intent?>>() {

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
                i.putExtra("cus_request_code", input.requestCode)
                return i
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Triple<Int, Int, Intent?> {
                val rq = intent?.getIntExtra("cus_request_code", -1) ?: -1
                return Triple(rq, resultCode, intent)
            }
        }) {
            onRequestActivityState[this::class.java.name]?.remove(it.first)?.invoke(it.first, it.second, it.third) ?: onResult(it.first, it.second, it.third)
            if (onRequestActivityState[this::class.java.name].isNullOrEmpty()) onRequestActivityState.remove(this::class.java.name)
        }
    }

    open fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("RBaseActivity.onResult", "the activity requestCode $requestCode No suitable callback interface is found, the data will complete the callback in this extension function!")
    }

    override fun finish() {
        super.finish()
        onRequestActivityState.remove(this::class.java.name)
    }
}