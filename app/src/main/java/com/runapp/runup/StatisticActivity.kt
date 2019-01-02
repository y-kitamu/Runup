package com.runapp.runup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.design.widget.TabLayout
import android.util.Log
import kotlinx.android.synthetic.main.activity_statistic.*
import java.util.*
import java.util.concurrent.CountDownLatch

class StatisticActivity : AppCompatActivity() {
    private val TAG = "StatisticActivity"

    private lateinit var mStartDate: Date
    private lateinit var mEndDate: Date
    lateinit var mRecord: Record

    private val backgroundThread = HandlerThread("backgroundThread").also { it.start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        mStartDate = Date(intent.getLongExtra(Constant.ARG_PARAM1, 201801010000))
        mEndDate = Date(intent.getLongExtra(Constant.ARG_PARAM2, 201801010000))

        val cal = Calendar.getInstance().also { it.time = mStartDate }
        toolbar_text.text = String.format(getString(R.string.statistic_activity_name),
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))

        val latch = CountDownLatch(1)
        backgroundHandler.post {
            val records = Constant.RECORD_DB.recordDao().getRecordOf(mStartDate, mEndDate)
            if (records.isNotEmpty()) {
                mRecord = records[0]
                mRecord.loadDataFromJson()
            }
            latch.countDown()
        }
        latch.await()

        if (!::mRecord.isInitialized) {
            finish()
            return
        }

        val pagerAadapter = StatisticActivityPagerAdapter(supportFragmentManager)
        viewpager.apply {
            offscreenPageLimit = Constant.StatisticTabFragments.values().size
            adapter = pagerAadapter
        }
        tablayout.apply {
            tabMode = TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL
        }
    }
}
