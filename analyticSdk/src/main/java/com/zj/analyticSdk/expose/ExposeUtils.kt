package com.zj.analyticSdk.expose

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.zj.analyticSdk.R
import com.zj.analyticSdk.expose.i.*
import com.zj.analyticSdk.expose.p.*

object ExposeUtils {

    fun with(lifecycleOwner: LifecycleOwner): Expose {
        return Expose(lifecycleOwner)
    }
}

@Suppress("unused")
class Expose internal constructor(private val lifecycleOwner: LifecycleOwner) {

    fun <T> trackRecyclerView(view: RecyclerView, rex: RecyclerExposeIn<T>, exposeIn: BaseExposeIn<T>) {
        checkOrAddExposer(view, exposeIn) { v, e ->
            RecycleViewExposer(lifecycleOwner, v, rex, e)
        }
    }

    fun <T> trackViewPager(view: ViewPager, vex: ViewPagerExposeIn<T>, exposeIn: BaseExposeIn<T>) {
        checkOrAddExposer(view, exposeIn) { v, e ->
            ViewPagerExposer(lifecycleOwner, v, vex, e)
        }
    }

    fun <T> trackViewPager2(view: ViewPager2, v2ex: ViewPager2ExposeIn<T>, exposeIn: BaseExposeIn<T>) {
        checkOrAddExposer(view, exposeIn) { v, e ->
            ViewPager2Exposer(lifecycleOwner, v, v2ex, e)
        }
    }

    fun <T> track(v: View, d: T?, exposeIn: BaseExposeIn<T>) {
        checkOrAddExposer(v, exposeIn) { v1, e ->
            ViewExposer(lifecycleOwner, v1, d, e)
        }
    }

    private inline fun <reified V : View, reified CLS : BaseExpose<T, *>, T> checkOrAddExposer(view: V, exposeIn: BaseExposeIn<T>, onCreate: (V, BaseExposeIn<T>) -> CLS) {
        if (view.getTag(R.id.view_expose_binder_id) != null) {
            (view.getTag(R.id.view_expose_binder_id) as? CLS)?.release()
        }
        view.setTag(R.id.view_expose_binder_id, onCreate(view, exposeIn))
    }
}