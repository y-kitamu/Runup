package com.runapp.runup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MainActivityPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return Constant.MainTabFragments.values().first { position == it.idx }.title
    }

    override fun getCount(): Int {
        return Constant.MainTabFragments.values().size
    }

    override fun getItem(position: Int): Fragment {
        // TODO: newInstance で毎回新しい fragment を生成して大丈夫？
        // 新しい fragment を追加する場合はここに記述
        when (position) {
            Constant.MainTabFragments.RECORD.idx -> {
                return RecordFragment.newInstance()
            }
            Constant.MainTabFragments.HISTORY.idx -> {
                return HistoryFragment.newInstance()
            }
            Constant.MainTabFragments.COMMUNITY.idx -> {
                return CommunityFragment.newInstance()
            }
        }
        return RecordFragment.newInstance()
    }
}