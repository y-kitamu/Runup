package com.runapp.runup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.concurrent.CountDownLatch


class HistoryFragment : Fragment(), OnRecyclerViewListener {

    private val TAG = "HistoryFragment"

    private lateinit var mRecord: List<Record>
    private lateinit var mViewManager: RecyclerView.LayoutManager
    private lateinit var mViewAdapter: RecyclerView.Adapter<*>

    private val backgroundThread = HandlerThread("backgroundThread").also { it.start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val latch = CountDownLatch(1)
        backgroundHandler.post {
            mRecord = Constant.RECORD_DB.recordDao().getAll()
            latch.countDown()
        }
        latch.await()

        Log.d(TAG, "mRecord.size = ${mRecord.size}")

        mViewManager = LinearLayoutManager(activity)
        mViewAdapter = HistoryRecyclerViewAdapter(mRecord, this, activity!!)

        recycler_history.apply {
            setHasFixedSize(true)
            layoutManager = mViewManager
            adapter = mViewAdapter
        }
    }

    override fun onRecyclerViewClick(v: View, position: Int) {
        val intent = Intent(activity, StatisticActivity::class.java)
        intent.apply {
            putExtra(Constant.ARG_PARAM1, mRecord[position].startDate.time)
            putExtra(Constant.ARG_PARAM2, mRecord[position].endDate.time)
        }
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment().apply {}
    }
}
