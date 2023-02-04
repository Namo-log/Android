package com.example.namo.ui.bottom.home.schedule.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.namo.R
import com.example.namo.databinding.ActivityMapBinding
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMapBinding
    private lateinit var mapView : MapView
    private lateinit var mapViewContainer : ViewGroup

    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        mapView = MapView(this)
        mapViewContainer = binding.mapView
        mapViewContainer.addView(mapView)

        //getLocationPermission()

    }

    private fun getLocationPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val lm : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val userNowLocation : Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
                val uLatitude = userNowLocation.latitude
                val uLongitude = userNowLocation.longitude
                val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude, uLongitude)
                mapView.setMapCenterPoint(uNowPosition, true)
            } catch (e : NullPointerException) {
                Log.e("LOCATION_ERROE", e.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.finishAffinity(this)
                } else {
                    ActivityCompat.finishAffinity(this)
                }

                finish()
            }
        } else {
            Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }
}