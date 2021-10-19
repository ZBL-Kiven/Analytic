package com.zj.analyticTest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.zj.analyticSdk.CCAnalytic
import com.zj.cf.annotations.Constrain
import com.zj.cf.fragments.ConstrainFragment

@Constrain(id = "TestConsFragment", backMode = 1)
class TestConsFragment : ConstrainFragment() {

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return ImageView(requireContext())
    }

    override fun onCreate() {
        super.onCreate()
        rootView?.layoutParams = FrameLayout.LayoutParams(1000, 1000)
        rootView?.setPadding(20, 20, 20, 20)
        (rootView as? ImageView)?.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun onStarted() {
        super.onStarted()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }
}