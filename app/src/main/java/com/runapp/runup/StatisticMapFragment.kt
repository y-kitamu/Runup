package com.runapp.runup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_statistic_map.*


class StatisticMapFragment : Fragment(), OnMapReadyCallback {

    private val TAG = "StatisticMapFragment"

    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistic_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, getFuncname())
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, getFuncname())
        super.onActivityCreated(savedInstanceState)
        history_map.onCreate(savedInstanceState?.getBundle(Constant.HISTORY_MAPVIEW_BUNDLE_KEY))
        history_map.getMapAsync(this)
    }

    override fun onStart() {
        Log.d(TAG, getFuncname())
        super.onStart()
        history_map.onStart()
    }

    override fun onResume() {
        Log.d(TAG, getFuncname())
        super.onResume()
        history_map.onResume()
    }

    override fun onPause() {
        Log.d(TAG, getFuncname())
        super.onPause()
        history_map.onPause()
    }

    override fun onStop() {
        Log.d(TAG, getFuncname())
        super.onStop()
        history_map.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, getFuncname())
        super.onDestroyView()
        history_map.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, getFuncname())
        super.onSaveInstanceState(outState)
        history_map.onSaveInstanceState(outState.getBundle(Constant.HISTORY_MAPVIEW_BUNDLE_KEY) ?: Bundle())
    }

    override fun onLowMemory() {
        Log.d(TAG, getFuncname())
        super.onLowMemory()
        history_map.onLowMemory()
    }

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
