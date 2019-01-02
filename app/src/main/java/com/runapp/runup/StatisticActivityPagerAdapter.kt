package com.runapp.runup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class StatisticActivityPagerAdapter(fm: FragmentManager):
    FragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return Constant.StatisticTabFragments.values().first { position == it.idx }.title
    }

    override fun getCount(): Int {
        return Constant.StatisticTabFragments.values().size
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            Constant.StatisticTabFragments.MAP.idx -> {
                return StatisticMapFragment.newInstance()
            }
            Constant.StatisticTabFragments.LAP.idx -> {
                return StatisticLapFragment.newInstance()
            }
            Constant.StatisticTabFragments.GRAPH.idx -> {
                return StatisticGraphFragment.newInstance()
            }
        }
        return StatisticMapFragment.newInstance()
    }
}