package com.mongmong.namo.presentation.ui.home.schedule.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleActivity
import com.mongmong.namo.presentation.ui.community.diary.MoimDiaryDetailActivity
import com.mongmong.namo.presentation.ui.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.map.adapter.MapRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.map.data.KakaoAPI
import com.mongmong.namo.presentation.ui.home.schedule.map.data.Place
import com.mongmong.namo.presentation.ui.home.schedule.map.data.ResultCoord2Address
import com.mongmong.namo.presentation.ui.home.schedule.map.data.ResultSearchPlace
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@AndroidEntryPoint
class MapActivity : BaseActivity<ActivityMapBinding>(R.layout.activity_map) {
    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private val mapRVAdapter : MapRVAdapter = MapRVAdapter()

    private val placeList : ArrayList<Place> = arrayListOf()
    private val markerList : ArrayList<Label> = arrayListOf()
    private var selectedPlace : Place = Place()
    private lateinit var prevLabel: Label

    private var uLatitude : Double = 0.0
    private var uLongitude : Double = 0.0

    @Inject
    lateinit var kakaoService: KakaoAPI

    override fun setup() {
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
                // 가준 위치 업데이트
                kakaoMap?.setOnCameraMoveEndListener { kakaoMap, cameraPosition, gestureType ->
                    uLatitude = cameraPosition.position.latitude
                    uLongitude = cameraPosition.position.longitude
                }
                // 라벨 스타일 변경
                kakaoMap?.setOnLabelClickListener { kakaoMap, labelLayer, label ->
                    prevLabel.changeStyles(LabelStyles.from(setPinStyle(false)))
                    label.changeStyles(LabelStyles.from(setPinStyle(true)))
                    prevLabel = label
                }
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return ZOOM_LEVEL
            }
        })
    }

    private fun hasPreLocation(): Boolean {
        return !intent.getStringExtra(PLACE_NAME_KEY).isNullOrEmpty()
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
            "GroupSchedule" -> MoimScheduleActivity::class.java
            "Schedule" -> ScheduleActivity::class.java
            "MoimDiary" -> MoimDiaryDetailActivity::class.java
            else -> ScheduleActivity::class.java
        }

        // 뒤로가기 버튼
        binding.mapBackCv.setOnClickListener {
            finish()
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
                prevLabel = markerList[position] // 마커 업데이트
            }
        })

        // 취소 버튼
        binding.cancelBtn.setOnClickListener {
            // 선택된 핀 다시 파란색으로 표시
            prevLabel.changeStyles(LabelStyles.from(setPinStyle(false)))
            // 줌 레벨 살짝 낮추기
            moveCamera(LatLng.from(uLatitude, uLongitude), ZOOM_LEVEL - 3)
            // 아이템 체크 표시 삭제
            mapRVAdapter.setSelectedPosition(-1)
            // 취소 & 확인 버튼 없애기
            binding.mapBtnLayout.visibility = View.GONE
        }
        // 확인 버튼
        binding.selectBtn.setOnClickListener {
            val intent = Intent(this, targetActivityClass)
            intent.apply {
                putExtra(PLACE_ID_KEY, selectedPlace.id)
                putExtra(PLACE_NAME_KEY, selectedPlace.place_name)
                putExtra(PLACE_X_KEY, selectedPlace.x)
                putExtra(PLACE_Y_KEY, selectedPlace.y)
            }
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
        selectedPlace.x = intent.getDoubleExtra(PLACE_X_KEY, 0.0)
        selectedPlace.y = intent.getDoubleExtra(PLACE_Y_KEY, 0.0)
        selectedPlace.place_name = intent.getStringExtra(PLACE_NAME_KEY).toString()
        val latLng = LatLng.from(selectedPlace.y, selectedPlace.x) // 위치
        prevLabel = kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng)
            .setStyles(setPinStyle(true))
            .setTexts(selectedPlace.place_name) // 장소 이름 표시
        )!!
        markerList.add(prevLabel)
        moveCamera(latLng, null)
        // 아이템 표시
        setPreLocationItem(selectedPlace.x, selectedPlace.y)
        Log.d("PlaceInfo", "selectedPlace: $selectedPlace")
    }

    private fun setPreLocationItem(placeX: Double, placeY: Double) {
        // 카카오 API를 이용해 좌표로 주소 정보 가져오기
        val call = kakaoService.getPlaceInfo("KakaoAK $API_KEY", placeX.toString(), placeY.toString())

        call.enqueue(object : Callback<ResultCoord2Address> {
            override fun onResponse(
                call: Call<ResultCoord2Address>,
                response: Response<ResultCoord2Address>
            ) {
                if (response.isSuccessful) {
                    val placeInfo = response.body()?.documents?.firstOrNull()
                    Log.d("PlaceInfo", placeInfo.toString())
                    if (placeInfo != null) {
                        selectedPlace.address_name = placeInfo.address.address_name
                        selectedPlace.road_address_name = placeInfo.road_address?.address_name.toString()
                        // 선택된 장소를 리사이클러뷰 아이템에 표시
                        placeList.clear()
                        placeList.add(selectedPlace)
                        setPlaceData()
                        mapRVAdapter.setSelectedPosition(0) // 첫 번째 아이템에 체크 표시
                        }
                }
            }

            override fun onFailure(call: Call<ResultCoord2Address>, t: Throwable) {
                Log.d("MapActivity", "좌표로 주소 정보 불러오기 실패\n${t.message}")
            }
        })
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
            uLatitude = userNowLocation.latitude // y
            uLongitude = userNowLocation.longitude // x
            Log.d("MapActivity", "userLocation: ($uLongitude, $uLatitude)")
        } catch (e : NullPointerException) {
            Log.e("LOCATION_ERROR", e.toString())
            ActivityCompat.finishAffinity(this)

            finish()
        }
    }

    companion object {
        const val API_KEY = BuildConfig.KAKAO_REST_KEY
        const val ZOOM_LEVEL = 18 // 선택 장소를 보여줄 때의 줌레벨
        fun setPinStyle(isSelected: Boolean): LabelStyle {
            if (isSelected) { // 선택된 핀
                return LabelStyle.from(
                    R.drawable.ic_pin_selected
                ).setTextStyles(20, R.color.black)
            }
            return LabelStyle.from( // 기본 핀
                R.drawable.ic_pin_default
            )
        }
        const val PLACE_ID_KEY = "PLACE_ID"
        const val PLACE_NAME_KEY = "PLACE_NAME"
        const val PLACE_X_KEY = "PLACE_X"
        const val PLACE_Y_KEY = "PLACE_Y"
    }
}