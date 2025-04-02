package com.example.geofancing

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.geofancing.Broadcast.GeofenceBroadcastReceiver
import com.example.geofancing.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var etRadius: EditText
    private lateinit var btnSetGeofence: Button

    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        etRadius = binding.etRadius
        btnSetGeofence = binding.btnSetGeofence

        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Geofencing Client
        geofencingClient = LocationServices.getGeofencingClient(this)


       locationpermisson()
        // Set Geofence on button click
        btnSetGeofence.setOnClickListener {
            val radius = etRadius.text.toString().toFloatOrNull()
            if (selectedLatLng != null && radius != null) {
                addGeofence(selectedLatLng!!, radius)
            } else {
                Toast.makeText(this, "Select a location and enter a radius!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun locationpermisson() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        PermissionHelper.requestPermissions(this, permissions, 100)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionHelper.handlePermissionsResult(permissions, grantResults) { granted ->
            if (granted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Default location (New York)
        val defaultLocation = LatLng(40.7128, -74.0060)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Set marker on tap
        mMap.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Draw a temporary circle with default radius
            mMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(100.0)
                    .strokeColor(Color.RED)
                    .fillColor(0x22FF0000)
            )
        }
    }

    private fun addGeofence(latLng: LatLng, radius: Float) {
        val geofence = Geofence.Builder()
            .setRequestId("UserGeofence")
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geofence Set!", Toast.LENGTH_SHORT).show()
                Log.d("Geofence", "Geofence added successfully")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to set Geofence", Toast.LENGTH_SHORT).show()
                Log.e("Geofence", "Failed: ${it.message}")
            }
    }
}
