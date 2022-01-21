package com.zj.analyticTest.component.rv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zj.analyticSdk.CCAnalytic
import com.zj.analyticTest.R
import com.zj.analyticSdk.expose.ExposeUtils
import com.zj.analyticSdk.expose.p.BaseExposeIn
import com.zj.analyticSdk.expose.p.RecyclerExposeIn

class ExposeActivity : AppCompatActivity(), RecyclerExposeIn<Int>, BaseExposeIn<Int> {

    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        rv = findViewById(R.id.main_rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = VAdapter()
        ExposeUtils.with(this).trackRecyclerView(rv, this, this)
    }

    override fun onStart() {
        super.onStart()
        CCAnalytic.get()?.trackPageStart(ExposeActivity::class.java.simpleName)
    }

    override fun getDataForRecyclerView(recyclerView: RecyclerView, p: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): Int? {
        return (adapter as? VAdapter)?.data?.get(p)
    }

    override fun onAttached(data: Int?) {
        Log.e("------ ", "onAttached $data")
    }

    override fun onDetached(data: Int?) {

        //        Log.e("------ ", "onDetached $data")
    }

    fun notifyAll(view: View) {
        rv.adapter?.notifyDataSetChanged()
    }

    fun notifyItem(view: View) {
        val adapter = rv.adapter as VAdapter
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    fun removeAll(view: View) {
        val adapter = rv.adapter as VAdapter
        adapter.data.clear()
        adapter.notifyDataSetChanged()
    }

    fun addAll(view: View) {
        val adapter = rv.adapter as? VAdapter
        adapter?.data?.addAll(0..20)
        adapter?.notifyItemRangeInserted(adapter.itemCount, adapter.itemCount + 20)
    }
}