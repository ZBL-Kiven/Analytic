package com.zj.analyticTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.zj.analyticSdk.CCAnalytic
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.BaseTabFragment
import com.zj.cf.startFragmentByNewTask

class TestFragment1 : BaseTabFragment() {

    @Container lateinit var view1: FrameLayout

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        view1 = FrameLayout(requireContext())
        view1.id = R.id.view_id
        view1.setPadding(10, 10, 10, 10)
        view1.setBackgroundResource(R.drawable.ic_launcher_foreground)
        view1.setOnClickListener {
            startFragmentByNewTask(TestConsFragment::class.java, view1, Bundle(), { true })
        }
        return view1
    }

    override fun onResumed() {
        super.onResumed()
        CCAnalytic.get()?.trackPageStart(this::class.java.simpleName)
    }

}