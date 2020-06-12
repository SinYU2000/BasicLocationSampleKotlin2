/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kotlindemos

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.content.Context
import android.hardware.SensorManager
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.ActivityCompat
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.*


class BasicMapDemoActivity :
        AppCompatActivity(),
        OnMapReadyCallback, SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private val sensors: Sensor? = null
    private var thread: Thread? = null
    private var plotData = true

    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var aa=0.0
    var bb=0.0
    var formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var now = formatter.format(Date())

    val ZOOM_LEVEL = 13f
    var SYDNEY = LatLng(aa, bb)
    private lateinit var MarKER: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_map_demo)
        //Log.i(TAG, "aa:$aa bb:$bb")

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mAccelerometer =
                mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensors =
                mSensorManager!!.getSensorList(Sensor.TYPE_ALL)

        for (i in sensors.indices) {
            Log.d(
                    TAG,
                    "onCreate: Sensor " + i + ": " + sensors[i].toString()
            )
        }
        if (mAccelerometer != null) {
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        feedMultiple()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment : SupportMapFragment? =
                supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun feedMultiple() {
        if (thread != null) {
            thread!!.interrupt()
        }
        thread = Thread(Runnable {
            while (true) {
                plotData = true
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        })
        thread!!.start()
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        } else {

        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
                .addOnCompleteListener { taskLocation ->
                    if (taskLocation.isSuccessful && taskLocation.result != null) {

                        val location = taskLocation.result
                        aa = location?.latitude!!
                        bb = location?.longitude!!

                        var mapFragment : SupportMapFragment? =
                                supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                        mapFragment?.getMapAsync(this)

                    } else {
                        Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    }
                }
    }

    private fun checkPermissions() =
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")


        } else {

            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()


                else -> {

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (thread != null) {
            thread!!.interrupt()
        }
        mSensorManager!!.unregisterListener(this)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (plotData) {
            if(event != null) {
                plotData = false
                val xValue = Math.abs(event.values[0])
                val yValue = Math.abs(event.values[1])
                val zValue = Math.abs(event.values[2])
                if (xValue > 12 || yValue > 12 || zValue > 12) {

                    formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    now = formatter.format(Date())
                    MarKER.remove()
                    getLastLocation()


                }else{}
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        SYDNEY = LatLng(aa, bb)
        googleMap ?: return
        with(googleMap) {
            Log.i(TAG, "aa:$aa bb:$bb")
            moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, ZOOM_LEVEL))
            MarKER= addMarker(MarkerOptions().position(SYDNEY).title(now))
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onDestroy() {
        mSensorManager!!.unregisterListener(this@BasicMapDemoActivity)
        thread!!.interrupt()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
