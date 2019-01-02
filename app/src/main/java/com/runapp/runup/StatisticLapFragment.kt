package com.runapp.runup

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class StatisticLapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistic_lap, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            StatisticLapFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
