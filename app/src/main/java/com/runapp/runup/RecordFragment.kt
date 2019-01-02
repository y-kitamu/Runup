package com.runapp.runup

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_record.*


class RecordFragment : Fragment(), OnMapReadyCallback {
    // TODO: 加速度計をつかって GPS が弱いときでも精度向上させる
    private val TAG = "RecordFragment" // TODO : remove this line when release

    private var mHasGPSPermission = false
    private var mIsGPSEnabled = false
    private var mIsNetworkEnabled = false

    private lateinit var mMap: GoogleMap
    private lateinit var mOptions: PolylineOptions
    private var mPolylines: MutableList<Polyline> = mutableListOf()

    private lateinit var mLocManager: LocationManager
    private lateinit var mLocProvider: LocationProvider
    private lateinit var mLatLon: LatLng
    private lateinit var mLocation: Location
    private var mRecord = Record()

    private var mIsRecording = false

    private val mHandler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, getFuncname())
        super.onViewCreated(view, savedInstanceState)

        checkGPSPermission()
        if (mHasGPSPermission) {
            mLocManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocProvider = mLocManager.getProvider(LocationManager.GPS_PROVIDER)

            // TODO: 2つを組み合わせて高速化 & 精度アップ http://ria10.hatenablog.com/entry/20121109/1352470160
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, locationListener)
            mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, locationListener)

            showButtons()
            btn_start.isClickable = ::mLocation.isInitialized && ::mMap.isInitialized
            btn_start.setOnClickListener {
                mIsGPSEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                mIsNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                    AlertDialog.Builder(activity)
                        .setTitle("GPS")
                        .setMessage("GPSがオフになっています。設定からGPSをオンにしてください。")
                        .setPositiveButton("OK") { _, _ -> startSettingIntent() }
                        .setNegativeButton("CANCEL") { _, _ -> }
                        .show()
                }

                if (mIsGPSEnabled || mIsNetworkEnabled) {
                    Log.d(TAG, "start recording")
                    drawLineOnMap()
                    mRecord = Record().apply { this.start(mLocation) }
                    mIsRecording = true
                    showButtons()
                    setTimeAndSpeed()
                    mHandler.post(timerRunnable)
                } else {
                    Log.d(TAG, "can not start recording")
                }
            }
            btn_pause.setOnClickListener {
                if (mIsRecording) {
                    mHandler.removeCallbacks(timerRunnable)
                    btn_pause.text = getString(R.string.str_record_resume)
                } else {
                    mHandler.post(timerRunnable)
                    btn_pause.text = getString(R.string.str_record_pause)
                }
                mIsRecording = !mIsRecording
            }
            btn_lap.setOnClickListener {
                mRecord.lap()
            }
            btn_stop.setOnClickListener {
                mHandler.removeCallbacks(timerRunnable)
                mIsRecording = false
                showButtons()
                mRecord.stop()

                for (polyline in mPolylines) {
                    polyline.remove()
                }
                mPolylines.clear()

                val intent = Intent(activity, StatisticActivity::class.java).apply {
                    putExtra(Constant.ARG_PARAM1, mRecord.startDate.time)
                    putExtra(Constant.ARG_PARAM2, mRecord.endDate.time)
                }
                startActivity(intent)

                mRecord = Record()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, getFuncname())
        super.onActivityCreated(savedInstanceState)
        record_map.onCreate(savedInstanceState?.getBundle(Constant.RECORD_MAPVIEW_BUNDLE_KEY))
        record_map.getMapAsync(this)
    }

    override fun onStart() {
        Log.d(TAG, getFuncname())
        super.onStart()
        record_map.onStart()
    }

    override fun onResume() {
        Log.d(TAG, getFuncname())
        super.onResume()
        record_map.onResume()
        setTimeAndSpeed()
    }

    override fun onPause() {
        Log.d(TAG, getFuncname())
        super.onPause()
        record_map.onPause()
    }

    override fun onStop() {
        Log.d(TAG, getFuncname())
        super.onStop()
        record_map.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, getFuncname())
        super.onDestroyView()
        record_map.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, getFuncname())
        super.onSaveInstanceState(outState)
        record_map.onSaveInstanceState(outState.getBundle(Constant.RECORD_MAPVIEW_BUNDLE_KEY) ?: Bundle())
    }

    override fun onLowMemory() {
        Log.d(TAG, getFuncname())
        super.onLowMemory()
        record_map.onLowMemory()
    }

    private fun checkGPSPermission() {
        mHasGPSPermission = ContextCompat.checkSelfPermission(activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!mHasGPSPermission) {
            ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constant.Permissions.ACCESS_FINE_LOCATION.idx)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constant.Permissions.ACCESS_FINE_LOCATION.idx -> {
                mHasGPSPermission = grantResults.isNotEmpty() &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED // 短絡評価
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, getFuncname())
        mMap = googleMap
        btn_start.isClickable = ::mLocation.isInitialized
        updateCameraPosition()

        if (mHasGPSPermission) {
            try {
                mMap.isMyLocationEnabled = true
            } catch (error: SecurityException) {
                Log.e(TAG, "SecurityException", error)
            }
        }
    }

    private fun startSettingIntent() {
        val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(settingIntent)
        mIsGPSEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        mIsNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showButtons() {
        if (mIsRecording) {
            start_btn_layout.visibility = View.GONE
            record_btn_layout.visibility = View.VISIBLE
        } else {
            start_btn_layout.visibility = View.VISIBLE
            record_btn_layout.visibility = View.GONE
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            Log.d(TAG, getFuncname())
            if (location != null) {
                Log.d(TAG, "location != null")
                val isFirstData = !::mLatLon.isInitialized

                mLatLon = LatLng(location.latitude, location.longitude)
                mLocation = location

                if (isFirstData) {
                    btn_start.isClickable = ::mMap.isInitialized
                    updateCameraPosition()
                }

                if (mIsRecording) {
                    updateCameraPosition()
                    drawLineOnMap()
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }

    private fun updateCameraPosition() {
        if (::mLatLon.isInitialized && ::mMap.isInitialized) {
            val cameraPosition = CameraPosition.Builder().target(mLatLon).zoom(15.0f).build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun drawLineOnMap() {
        if (::mOptions.isInitialized) {
            if (::mMap.isInitialized) {
                mOptions.apply {
                    add(mLatLon)
                    color(calcColorMap(mLocation.speed))
                }
                mPolylines.add(mMap.addPolyline(mOptions))
            }
        }
        mOptions = PolylineOptions().apply {
            add(mLatLon)
            width(Constant.MAP_LINE_WIDTH)
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            mRecord.duration++
            mHandler.postDelayed(this, 100)
            if (mRecord.duration % 10 == 0) {
                mRecord.add(mLocation)
                setTimeAndSpeed()
            }
        }
    }

    private fun setTimeAndSpeed() {
        val speed = if (mRecord.duration == 0) 0.0f else 36f * mRecord.distance / mRecord.duration
        text_time.text = String.format(getString(R.string.str_record_time),
            mRecord.duration / 600, mRecord.duration / 10 % 60)
        text_speed.text = String.format(getString(R.string.str_record_speed), speed)
    }

    companion object {
        fun newInstance(): RecordFragment =
                RecordFragment().apply {
                    arguments = Bundle().apply {}
                }
    }
}
