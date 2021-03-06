package com.example.geofencingex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlin.concurrent.thread

class GeofenceBroadcastReceiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        Log.d("GeofenceBR" , "지오펜싱 브로드캐스트 리시버 시작!!")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceBR", errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition    // 발생 이벤트 타입

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            val transitionMsg = when(geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Enter"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "Exit"
                Geofence.GEOFENCE_TRANSITION_DWELL -> "Dwell"
                else -> "-"
            }

            Log.d("GeofenceBR" , "$triggeringGeofences")

            triggeringGeofences.forEach {
                Log.d("GeofenceBR","${it.requestId} - $transitionMsg")
                Toast.makeText(context, "${it.requestId} - $transitionMsg", Toast.LENGTH_LONG).show()

            }

        } else {
            Toast.makeText(context, "Unknown", Toast.LENGTH_LONG).show()
        }
    }
}