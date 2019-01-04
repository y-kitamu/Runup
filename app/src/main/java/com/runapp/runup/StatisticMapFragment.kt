package com.runapp.runup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_statistic_map.*


class StatisticMapFragment : SupportMapFragment(), OnMapReadyCallback {

    private val TAG = "StatisticMapFragment"

    private lateinit var mMap: GoogleMap

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        var prevLocation = (activity as StatisticActivity).mRecord.recordList[0].first[0]
        for (lapRecord in (activity as StatisticActivity).mRecord.recordList) {
            for (record in lapRecord.first) {
                val options = PolylineOptions().apply {
                    add(LatLng(prevLocation.latitude, prevLocation.longitude))
                    add(LatLng(record.latitude, record.longitude))
                    width(Constant.MAP_LINE_WIDTH)
                }
                mMap.addPolyline(options)
                prevLocation = record
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = StatisticMapFragment()
    }
}
