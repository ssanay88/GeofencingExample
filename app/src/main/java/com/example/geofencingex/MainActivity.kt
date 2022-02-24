package com.example.geofencingex

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQ_ACCESS_FINE_LOCATION = 100
    private val MY_PERMISSIONS_REQ_ACCESS_BACKGROUND_LOCATION = 101

    var mLocationManager: LocationManager? = null    // 위치 서비스에 접근하는 클래스를 제공
    var mLocationListener: LocationListener? = null    // 위치가 변할 때 LocationManager로부터 notification을 받는 용도

    // 첫번째 지오펜싱 리스트
    val geofenceList: MutableList<Geofence> by lazy {
        mutableListOf(
            getGeofence("우리집", Pair(35.1389,129.1056)),
            getGeofence("GS25", Pair(35.1367,129.1035)),
            getGeofence("저기저기", Pair(35.1325,129.0984))
        )
    }

    // 두번째 지오펜싱 리스트
    val geofenceList2: MutableList<Geofence> by lazy {
        mutableListOf(
            getGeofence("먼곳", Pair(35.1336,129.0897)),
            getGeofence("대연초", Pair(35.1374,129.0920)),
            getGeofence("우리집", Pair(35.1389,129.1056))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        AddGeofences()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                var lat = 0.0
                var lng = 0.0
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                    Log.d("로그", " 현재 위치는 $lat , $lng ")
                }

                Toast.makeText(this@MainActivity,"현재 위치는 $lat , $lng 입니다.",Toast.LENGTH_SHORT).show()


            }
        }

        mLocationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3000L,
            30f, mLocationListener as LocationListener
        )



    }

    private fun AddGeofences() {
        checkPermission()
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity, "add Success", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "add Fail", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }


    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시 부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(list)    // Geofence 리스트 추가
        }.build()
    }

    private fun getGeofence(reqId: String, geo: Pair<Double, Double>, radius: Float = 100f): Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)    // 이벤트 발생시 BroadcastReceiver에서 구분할 id
            .setCircularRegion(geo.first, geo.second, radius)    // 위치 및 반경(m)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)        // Geofence 만료 시간
            .setLoiteringDelay(1000)                            // 머물기 체크 시간
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER                // 진입 감지시
                        or Geofence.GEOFENCE_TRANSITION_EXIT    // 이탈 감지시
                        or Geofence.GEOFENCE_TRANSITION_DWELL)    // 머물기 감지시
            .build()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQ_ACCESS_FINE_LOCATION,
            MY_PERMISSIONS_REQ_ACCESS_BACKGROUND_LOCATION -> {
                grantResults.apply {
                    if (this.isNotEmpty()) {
                        this.forEach {
                            if (it != PackageManager.PERMISSION_GRANTED) {
                                checkPermission()
                                return
                            }
                        }
                    } else {
                        checkPermission()
                    }
                }
            }
        }
    }


    private fun checkPermission() {
        val permissionAccessFineLocationApproved = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (permissionAccessFineLocationApproved) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundLocationPermissionApproved = ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED

                if (!backgroundLocationPermissionApproved) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        MY_PERMISSIONS_REQ_ACCESS_BACKGROUND_LOCATION
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQ_ACCESS_FINE_LOCATION
            )
        }
    }



}