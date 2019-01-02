package com.runapp.runup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Constant.init(this)

        // TODO: 本番では削除
        deleteDatabase(Constant.RECORD_DB_BASENAME + ".db")

        // TODO: 戻るボタンでもとのタブに戻れるようにする。
        val pagerAdapter = MainActivityPagerAdapter(supportFragmentManager)
        viewpager.apply {
            offscreenPageLimit = Constant.MainTabFragments.values().size
            adapter = pagerAdapter
        }
        tablayout.apply {
            tabMode = TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL
        }
    }

    companion object {

    }
}
