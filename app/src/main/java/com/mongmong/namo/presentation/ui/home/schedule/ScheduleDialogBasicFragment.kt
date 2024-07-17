package com.mongmong.namo.presentation.ui.home.schedule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentScheduleDialogBasicBinding
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.presentation.utils.PickerConverter
import com.mongmong.namo.presentation.utils.PickerConverter.setSelectedDate
import com.mongmong.namo.presentation.utils.PickerConverter.setSelectedTime
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class ScheduleDialogBasicFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogBasicBinding
    private val args : ScheduleDialogBasicFragmentArgs by navArgs()

    private var prevClicked : TextView? = null

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private lateinit var getResult : ActivityResultLauncher<Intent>

    private val viewModel : PersonalScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_dialog_basic, container, false)

        binding.apply {
            viewModel = this@ScheduleDialogBasicFragment.viewModel
            lifecycleOwner = this@ScheduleDialogBasicFragment
        }

        initObservers()
        initMapView()
        initClickListeners()
        setInit()
        setEditTextChangedListener()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.updatePlace(
                    it.data?.getStringExtra(MainActivity.PLACE_NAME_INTENT_KEY)!!,
                    it.data?.getDoubleExtra(MainActivity.PLACE_X_INTENT_KEY, 0.0)!!,
                    it.data?.getDoubleExtra(MainActivity.PLACE_Y_INTENT_KEY, 0.0)!!
                )
                setMapContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                Toast.makeText(requireContext(), "알림 권한이 허용되었습니다. 알림 등록을 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "설정에서 알림 권한을 허용 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setInit() {
        viewModel.getCategories()
        // 정보 세팅
        if (args.schedule != null) {
            viewModel.setSchedule(args.schedule)
        } else {
            viewModel.setSchedule(
                Schedule(
                    startLong = PickerConverter.getDefaultDate(DateTime(args.nowDay), true),
                    endLong = PickerConverter.getDefaultDate(DateTime(args.nowDay), false)
                )
            )
        }
        Log.d("ScheduleDialogFrag", "schedule: ${viewModel.schedule.value}")
    }

    private fun setEditTextChangedListener() {
        binding.dialogScheduleTitleEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) {
                // 일정 제목 업데이트
                viewModel.updateTitle(binding.dialogScheduleTitleEt.text.toString())
            }
        })
    }

    private fun initClickListeners() {
        // 카테고리 클릭
        binding.dialogScheduleCategoryLayout.setOnClickListener {
            hideKeyBoard()

            val action = viewModel.schedule.value?.let { schedule ->
                ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentToScheduleDialogCategoryFragment(
                    schedule
                )
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }

        // 장소 클릭
        binding.dialogSchedulePlaceLayout.setOnClickListener {
            hideKeyBoard()
            getLocationPermission()
        }

        // 길찾기 버튼
        binding.dialogSchedulePlaceKakaoBtn.setOnClickListener {
            hideKeyBoard()

//            val url = "kakaomap://route?sp=&ep=${place.y},${place.x}&by=PUBLICTRANSIT"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
        }

        // 닫기 클릭
        binding.dialogScheduleCloseBtn.setOnClickListener {
            requireActivity().finish()
        }

        // 저장 클릭
        binding.dialogScheduleSaveBtn.setOnClickListener {
            if (!isValidInput()) return@setOnClickListener

            // 모임 일정일 경우
            if (viewModel.isMoimSchedule()) {
                // 카테고리 수정
                editMoimScheduleCategory()
                return@setOnClickListener
            }
            // 개인 일정일 경우
            if (viewModel.isCreateMode()) {
                // 일정 생성
                insertData()
            } else {
                // 일정 수정
                updateData()
            }
        }
    }

    private fun initPickerClickListeners() {
        // 시작 시간
        with(binding.dialogScheduleStartTimeTp) {
            val startDateTime = viewModel.getDateTime()?.first!!
            this.hour = startDateTime.hourOfDay
            this.minute = startDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(setSelectedTime(startDateTime, hourOfDay, minute), null)
            }
        }
        // 종료 시간
        with(binding.dialogScheduleEndTimeTp) {
            val endDateTime = viewModel.getDateTime()?.second!!
            this.hour = endDateTime.hourOfDay
            this.minute = endDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(null, setSelectedTime(endDateTime, hourOfDay, minute))
            }
        }
        // 시작일 - 날짜
        binding.dialogScheduleStartDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 날짜
        binding.dialogScheduleEndDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 시작일 - 시간
        binding.dialogScheduleStartTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 시간
        binding.dialogScheduleEndTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
    }

    private fun isValidInput(): Boolean {
        // 제목 미입력
        if (binding.dialogScheduleTitleEt.text.toString().isEmpty()) {
            Toast.makeText(context, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        // 시작일 > 종료일
        if (viewModel.isInvalidDate()) {
            Toast.makeText(context, "시작일이 종료일보다 느릴 수 없습니다.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /** 일정 추가 **/
    private fun insertData() {
        // 현재 일정의 상태가 추가 상태임을 나타냄
//        schedule.state = RoomState.ADDED.state
//        schedule.isUpload = UploadState.IS_NOT_UPLOAD.state
//        schedule.serverId = 0

        // 새 일정 등록
        viewModel.addSchedule()
    }

    /** 일정 수정 **/
    private fun updateData() {
        // 현재 일정의 상태가 수정 상태임을 나타냄
//        schedule.state = RoomState.EDITED.state
//        schedule.isUpload = UploadState.IS_NOT_UPLOAD.state

        // 일정 편집
        viewModel.editSchedule()
        Toast.makeText(requireContext(), "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        // 뒤로가기
        requireActivity().finish()
    }

    /** 모임 일정 카테고리 수정 */
    private fun editMoimScheduleCategory() {
        viewModel.editMoimScheduleCategory()
    }

    private fun hideKeyBoard() {
        val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.dialogScheduleTitleEt.windowToken, 0)
    }

    //Location Map Zone
    private fun initMapView() {
        mapView = binding.dialogSchedulePlaceContainer
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
                setMapContent()
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return MapActivity.ZOOM_LEVEL
            }
        })
    }

    private fun setMapContent() {
        kakaoMap?.labelManager?.layer?.removeAll()
        val mapData = viewModel.getPlace() ?: return
        Log.d("ScheduleBasicFragment", mapData.toString())
//        binding.dialogSchedulePlaceKakaoBtn.visibility = View.VISIBLE
        binding.dialogSchedulePlaceContainer.visibility = View.VISIBLE

        // 지도 위치 조정
        val latLng = mapData.second
        Log.d("ScheduleBasicFragment", latLng.toString())
        // 카메라를 마커의 위치로 이동
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, MapActivity.ZOOM_LEVEL))
        // 마커 추가
        kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng).setStyles(MapActivity.setPinStyle(false)))
    }

    private fun getLocationPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val userNowLocation : Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!

                val intent = Intent(requireActivity(), MapActivity::class.java)
                intent.putExtra(MainActivity.ORIGIN_ACTIVITY_INTENT_KEY, "Schedule")
                val placeData = viewModel.getPlace()
                if (placeData != null) {
                    intent.apply {
                        putExtra("PREV_PLACE_NAME", placeData.first)
                        putExtra("PREV_PLACE_X", placeData.second.longitude)
                        putExtra("PREV_PLACE_Y", placeData.second.latitude)
                    }
                }
                getResult.launch(intent)

            } catch (e : NullPointerException) {
                Log.e("LOCATION_ERROR", e.toString())
                ActivityCompat.finishAffinity(requireActivity())
            }

        } else {
            Toast.makeText(context, "위치 권한 허용 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }


    // Content Zone
    private fun setContent() {
        // 시작일, 종료일
        initPicker()
        // 장소
        setMapContent()
    }

    private fun initPicker(){
        val dateTimePair = viewModel.getDateTime() ?: return
        Log.d("INIT_PICKER_TEXT", "start: ${dateTimePair.first}\nend: ${dateTimePair.second}")
        binding.dialogScheduleStartTimeTp.apply { // 시작 시간
            hour = dateTimePair.first.hourOfDay
            minute = dateTimePair.first.minuteOfHour
        }
        binding.dialogScheduleEndTimeTp.apply { // 종료 시간
            hour = dateTimePair.second.hourOfDay
            minute = dateTimePair.second.minuteOfHour
        }
        binding.dialogScheduleStartDateDp.init(dateTimePair.first.year, dateTimePair.first.monthOfYear - 1, dateTimePair.first.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(dateTimePair.first.withDate(year, monthOfYear + 1, dayOfMonth), null)
        }
        binding.dialogScheduleEndDateDp.init(dateTimePair.second.year, dateTimePair.second.monthOfYear - 1, dateTimePair.second.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(null, dateTimePair.second.withDate(year, monthOfYear + 1, dayOfMonth))
        }
    }

    private fun showPicker(clicked : TextView) {
        hideKeyBoard()
        prevClicked = if (prevClicked != clicked) {
            prevClicked?.setTextColor(resources.getColor(R.color.textGray))
            clicked.setTextColor(resources.getColor(R.color.mainOrange))
            togglePicker(prevClicked, false)
            togglePicker(clicked, true)
            clicked // prevClicked 값을 현재 clicked로 업데이트
        } else {
            clicked.setTextColor(resources.getColor(R.color.textGray))
            togglePicker(clicked, false)
            null
        }
    }

    private fun togglePicker(pickerText: TextView?, open: Boolean) {
        pickerText?.let { pickerTextView ->
            val picker: MotionLayout = when (pickerTextView) {
                binding.dialogScheduleStartDateTv -> binding.dialogScheduleStartDateLayout
                binding.dialogScheduleEndDateTv -> binding.dialogScheduleEndDateLayout
                binding.dialogScheduleStartTimeTv -> binding.dialogScheduleStartTimeLayout
                binding.dialogScheduleEndTimeTv -> binding.dialogScheduleEndTimeLayout
                else -> binding.dialogScheduleStartTimeLayout
            }

            picker.let {
                if (open) {
                    it.transitionToEnd()
                } else {
                    it.transitionToStart()
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.schedule.observe(requireActivity()) { schedule ->
            setContent()
            if (schedule != null) {
                initPickerClickListeners()
            }
        }

        viewModel.categoryList.observe(requireActivity()) {categoryList ->
            if (categoryList.isNotEmpty()) viewModel.findCategoryById()
        }

        viewModel.category.observe(requireActivity()) { category ->
            if (category.categoryId != 0L && viewModel.getScheduleCategoryId() == 0L) viewModel.setCategory()
        }

        viewModel.isComplete.observe(requireActivity()) { isComplete ->
            // 추가 작업이 완료된 후 뒤로가기
            if (isComplete) {
                requireActivity().finish()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 777
    }
}