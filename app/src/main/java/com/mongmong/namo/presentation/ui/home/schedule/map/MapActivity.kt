package com.mongmong.namo.presentation.ui.home.schedule.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.mongmong.namo.BuildConfig
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMapBinding
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_X_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_Y_INTENT_KEY
import com.mongmong.namo.presentation.ui.group.schedule.GroupScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.map.adapter.MapRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.map.data.KakaoAPI
import com.mongmong.namo.presentation.ui.home.schedule.map.data.Place
import com.mongmong.namo.presentation.ui.home.schedule.map.data.ResultSearchPlace
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@AndroidEntryPoint
class MapActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMapBinding

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private val mapRVAdapter : MapRVAdapter = MapRVAdapter()

    private val placeList : ArrayList<Place> = arrayListOf()
    private val markerList : ArrayList<Label> = arrayListOf()
    private var selectedPlace : Place = Place()
    private var prevPlace : Place = Place()
    private lateinit var prevLabel: Label

    private var uLatitude : Double = 0.0
    private var uLongitude : Double = 0.0

    @Inject
    lateinit var kakaoService: KakaoAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLocationPermission()

        setAdapter()
        initClickListeners()
        initMapView()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun finish() {
        super.finish()
        mapView.finish()
    }

    private fun initMapView() {
        mapView = binding.mapView
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                // 인증 후 API 가 정상적으로 실행될 때 호출됨
                kakaoMap = map
                setLocation() // 위치 표시
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return ZOOM_LEVEL
            }
        })
    }

    private fun hasPreLocation(): Boolean {
        return !intent.getStringExtra("PREV_PLACE_NAME").isNullOrEmpty()
    }

    private fun setLocation() {
        if (hasPreLocation()) {
            setPreLocation() // 이전 위치로 지도 표시
            return
        }
        setCurrentLocation() // 현재 위치로 지도 표시
    }

    private fun setAdapter() {
        binding.mapRv.apply {
            layoutManager =
                LinearLayoutManager(this@MapActivity, LinearLayoutManager.VERTICAL, false)
            adapter = mapRVAdapter
        }

//        setRVData()
    }

    private fun setPlaceData() {
        mapRVAdapter.addPlaces(placeList)
    }

    // 장소 검색
    private fun searchPlace() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (binding.mapSearchEt.text.isNullOrBlank()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
        else {
            searchPlace(binding.mapSearchEt.text.toString())
        }
    }

    private fun initClickListeners() {
        val targetActivityClass = when(intent.getStringExtra(ORIGIN_ACTIVITY_INTENT_KEY)) {
            "GroupSchedule" -> GroupScheduleActivity::class.java
            "Schedule" -> ScheduleActivity::class.java
            else -> ScheduleActivity::class.java
        }

        binding.mapSearchEt.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                searchPlace()
                true
            } else {
                false
            }
        }

        // 검색 버튼
        binding.mapSearchBtn.setOnClickListener {
            searchPlace() // 장소 검색
        }

        // 검색 리스트에서 선택한 장소
        mapRVAdapter.setItemClickListener( object :
            MapRVAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                val place : Place = placeList[position]
                Log.d("SELECTED_PLACE", place.toString())
                selectedPlace = place // 선택 장소 설정
                val latLng = LatLng.from(place.y, place.x)
                Log.d("MapActivity", "selectedPlace: $selectedPlace\n$latLng")
                // 카메라를 마커의 위치로 이동
                moveCamera(latLng, null)
                // 핀 스타일 변경
                markerList[position].changeStyles(LabelStyles.from(setPinStyle(true)))
                // 장소 취소 & 확인 버튼 표시
                binding.mapBtnLayout.visibility = View.VISIBLE
                // 이전에 선택한 장소 핀 색상은 파란색으로 돌려놓기
                if (prevLabel != markerList[position]) {
                    prevLabel.changeStyles(LabelStyles.from(setPinStyle(false)))
                }
                prevLabel = markerList[position]
            }
        })

        //TODO: 핀 클릭 시 해당 핀 빨간색 + 장소 이름 표시

        // 취소 버튼
        binding.cancelBtn.setOnClickListener {
            //TODO: 선택된 핀을 다시 파란색으로 바꾸기 + 취소 & 확인 버튼 없애기
            if (hasPreLocation()) {
                val intent = Intent(this, targetActivityClass)
                intent.putExtra(PLACE_NAME_INTENT_KEY, prevPlace.place_name)
                intent.putExtra(PLACE_X_INTENT_KEY, prevPlace.x)
                intent.putExtra(PLACE_Y_INTENT_KEY, prevPlace.y)
                setResult(RESULT_OK, intent)
            }
            finish()
        }
        // 확인 버튼
        binding.selectBtn.setOnClickListener {
            val intent = Intent(this, targetActivityClass)
            intent.putExtra(PLACE_NAME_INTENT_KEY, selectedPlace.place_name)
            intent.putExtra(PLACE_X_INTENT_KEY, selectedPlace.x)
            intent.putExtra(PLACE_Y_INTENT_KEY, selectedPlace.y)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    // 장소 검색
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
                setPlaceData() // 장소 리스트 표시
                addMarkers() // 지도 핀 표시
            }

            override fun onFailure(call: Call<ResultSearchPlace>, t: Throwable) {
                Log.w("SEARCH_PLACE", "통신 실패 : ${t.message}")
            }
        })
    }

    // 검색해서 나온 장소에 핀 표시
    private fun addMarkers() {
        // 기존 핀 삭제
        kakaoMap?.labelManager?.removeAllLabelLayer()
        markerList.clear()
        if (placeList.isEmpty()) { // 검색 결과가 없을 경우
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        for (i in placeList) { // 핀 추가
            val latLng = LatLng.from(i.y, i.x) // 위치

            val point = kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng)
                .setStyles(setPinStyle(false))
                .setTexts(i.place_name) // 장소 이름 표시
            )
            point!!.isClickable = true
            // 첫 번째 장소로 카메라 표시
            if (i == placeList[0]) {
                moveCamera(latLng, ZOOM_LEVEL - 3) // 지도를 조금 더 넓게 표시
                prevLabel = point // 임의로 최초 핀을 선택된 핀으로 설정
            }
            markerList.add(point)
        }
    }

    // 기존에 선택된 장소 표시
    private fun setPreLocation() {
        prevPlace.x = intent.getDoubleExtra("PREV_PLACE_X", 0.0)
        prevPlace.y = intent.getDoubleExtra("PREV_PLACE_Y", 0.0)
        prevPlace.place_name = intent.getStringExtra("PREV_PLACE_NAME").toString()
        val latLng = LatLng.from(prevPlace.y, prevPlace.x) // 위치
        kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng)
            .setStyles(setPinStyle(true))
            .setTexts(prevPlace.place_name) // 장소 이름 표시
        )
        moveCamera(latLng, null)
    }

    // 카메라를 현재 위치로 이동
    private fun setCurrentLocation() {
        moveCamera(LatLng.from(uLatitude, uLongitude), null)
    }

    // 카메라 이동
    private fun moveCamera(latLng: LatLng, zoomLevel: Int?) {
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, zoomLevel ?: ZOOM_LEVEL))
    }

    // 위치 권한 확인
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
            Log.d("MapActivity", "userLocation: ($uLatitude, $uLongitude)")
        } catch (e : NullPointerException) {
            Log.e("LOCATION_ERROR", e.toString())
            ActivityCompat.finishAffinity(this)

            finish()
        }
    }

    companion object {
        const val API_KEY = BuildConfig.KAKAO_REST_KEY
        const val ZOOM_LEVEL = 18
        fun setPinStyle(isSelected: Boolean): LabelStyle {
            if (isSelected) { // 선택된 핀
                return LabelStyle.from(
                    R.drawable.ic_pin_selected
                ).setTextStyles(20, R.color.black)
            }
            return LabelStyle.from( // 기본 정
                R.drawable.ic_pin_default
            )
        }
    }
}