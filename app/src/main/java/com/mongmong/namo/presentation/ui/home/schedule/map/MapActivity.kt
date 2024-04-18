package com.mongmong.namo.presentation.ui.home.schedule.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_X_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_Y_INTENT_KEY
import com.mongmong.namo.databinding.ActivityMapBinding
import com.mongmong.namo.BuildConfig
import com.mongmong.namo.presentation.ui.group.schedule.GroupScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.map.adapter.MapRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.map.data.KakaoAPI
import com.mongmong.namo.presentation.ui.home.schedule.map.data.Place
import com.mongmong.namo.presentation.ui.home.schedule.map.data.ResultSearchPlace
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@AndroidEntryPoint
class MapActivity() : AppCompatActivity() {
    private lateinit var binding : ActivityMapBinding
    private lateinit var mapView : MapView

    private val mapRVAdapter : MapRVAdapter = MapRVAdapter()

    private val placeList : ArrayList<Place> = arrayListOf()
    private val markerList : ArrayList<MapPOIItem> = arrayListOf()
    private var selectedPlace : Place = Place()
    private var prevPlace : Place = Place()

    private var uLatitude : Double = 0.0
    private var uLongitude : Double = 0.0

    @Inject
    lateinit var kakaoService: KakaoAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = MapView(this)
        binding.mapView.addView(mapView)

        getLocationPermission()

        val hasPreLocation = if (intent.getStringExtra("PREV_PLACE_NAME").isNullOrEmpty()) {
            setCurrentLocation()
            false
        } else {
            setPreLocation()
            true
        }

        setAdapter()
        setClickListener(hasPreLocation)
    }

    private fun setAdapter() {
        binding.mapRv.apply {
            layoutManager =
                LinearLayoutManager(this@MapActivity, LinearLayoutManager.VERTICAL, false)
            adapter = mapRVAdapter
        }

        setRVData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setRVData() {
        mapRVAdapter.addPlaces(placeList)
        mapRVAdapter.notifyDataSetChanged()
    }

    private fun searchStartDate() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (binding.mapSearchEt.text.isNullOrBlank()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
        else {
            searchPlace(binding.mapSearchEt.text.toString())
        }
    }

    private fun setClickListener(hasPreLocation: Boolean) {
        val targetActivityClass = when(intent.getStringExtra(ORIGIN_ACTIVITY_INTENT_KEY)) {
            "GroupSchedule" -> GroupScheduleActivity::class.java
            "Schedule" -> ScheduleActivity::class.java
            else -> ScheduleActivity::class.java
        }

        binding.mapSearchEt.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                searchStartDate()
                true
            } else {
                false
            }
        }

        binding.mapSearchBtn.setOnClickListener {
            searchStartDate()
        }

        mapRVAdapter.setItemClickListener( object :
            MapRVAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                val place : Place = placeList[position]
                Log.d("SELECTED_PLACE", place.toString())
                selectedPlace = place

                val mapPoint = MapPoint.mapPointWithGeoCoord(place.y, place.x)
                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                mapView.selectPOIItem(markerList[position], true)
                binding.mapBtnLayout.visibility = View.VISIBLE
            }
        })

        binding.cancelBtn.setOnClickListener {
            if (hasPreLocation) {
                val intent = Intent(this, targetActivityClass)
                intent.putExtra(PLACE_NAME_INTENT_KEY, prevPlace.place_name)
                intent.putExtra(PLACE_X_INTENT_KEY, prevPlace.x)
                intent.putExtra(PLACE_Y_INTENT_KEY, prevPlace.y)
                setResult(RESULT_OK, intent)
            }
            finish()
        }

        binding.selectBtn.setOnClickListener {
            val intent = Intent(this, targetActivityClass)
            intent.putExtra(PLACE_NAME_INTENT_KEY, selectedPlace.place_name)
            intent.putExtra(PLACE_X_INTENT_KEY, selectedPlace.x)
            intent.putExtra(PLACE_Y_INTENT_KEY, selectedPlace.y)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun finish() {
        binding.mapView.removeView(mapView)
        super.finish()
    }

    private fun searchPlace(keyword : String) {
        val call = kakaoService.getSearchPlace("KakaoAK $API_KEY", keyword, uLongitude.toString(), uLatitude.toString(), "distance")

        call.enqueue(object : Callback<ResultSearchPlace> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ResultSearchPlace>,
                response: Response<ResultSearchPlace>
            ) {
                Log.d("SEARCH_PLACE", " row : ${response.raw()}")
                Log.d("SEARCH_PLACE", " body : ${response.body()}")

                placeList.clear()
                placeList.addAll(response.body()!!.documents as ArrayList<Place>)
                setRVData()
                addMarkers()
            }

            override fun onFailure(call: Call<ResultSearchPlace>, t: Throwable) {
                Log.w("SEARCH_PLACE", "통신 실패 : ${t.message}")
            }
        })
    }

    private fun addMarkers() {
        mapView.removeAllPOIItems()
        markerList.clear()

        if (placeList.isNullOrEmpty()) {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
        else {
            for (i in placeList) {
                val point = MapPOIItem()
                point.apply {
                    itemName = i.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(i.y.toDouble(), i.x.toDouble())
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.RedPin
                }

                if (i == placeList[0]) {
                    mapView.setMapCenterPointAndZoomLevel(point.mapPoint, 1, true)
                }

                mapView.addPOIItem(point)
                markerList.add(point)
            }
        }
    }

    private fun setPreLocation() {
        prevPlace.x = intent.getDoubleExtra("PREV_PLACE_X", 0.0)
        prevPlace.y = intent.getDoubleExtra("PREV_PLACE_Y", 0.0)
        prevPlace.place_name = intent.getStringExtra("PREV_PLACE_NAME").toString()

        val prevPosition = MapPoint.mapPointWithGeoCoord(prevPlace.y, prevPlace.x)
        mapView.setMapCenterPoint(prevPosition, true)

        val point = MapPOIItem()
        point.apply {
            itemName = prevPlace.place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(prevPlace.y, prevPlace.x)
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }

        mapView.addPOIItem(point)
        mapView.selectPOIItem(point, true)

//        binding.mapSearchEt.setText(name)

    }


    private fun setCurrentLocation() {
        val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude, uLongitude)
        mapView.setMapCenterPoint(uNowPosition, true)
    }

    private fun getLocationPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val userNowLocation : Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
            uLatitude = userNowLocation.latitude
            uLongitude = userNowLocation.longitude
        } catch (e : NullPointerException) {
            Log.e("LOCATION_ERROR", e.toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.finishAffinity(this)
            } else {
                ActivityCompat.finishAffinity(this)
            }

            finish()
        }
    }

    companion object {
        const val API_KEY = BuildConfig.KAKAO_REST_KEY
    }
}