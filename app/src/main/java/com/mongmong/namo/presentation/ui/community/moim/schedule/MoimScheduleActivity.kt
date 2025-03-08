package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimScheduleBinding
import com.mongmong.namo.domain.model.MoimCreateInfo
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.enums.SuccessType
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity
import com.mongmong.namo.presentation.ui.community.moim.diary.MoimDiaryDetailActivity
import com.mongmong.namo.presentation.ui.community.moim.MoimFragment.Companion.MOIM_EDIT_KEY
import com.mongmong.namo.presentation.ui.community.moim.schedule.adapter.MoimParticipantRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity
import com.mongmong.namo.presentation.ui.common.ConfirmDialog
import com.mongmong.namo.presentation.ui.common.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.ui.community.moim.MoimFragment.Companion.MOIM_CREATE_KEY
import com.mongmong.namo.presentation.ui.community.moim.schedule.FriendInviteActivity.Companion.MOIM_INVITE_KEY
import com.mongmong.namo.presentation.ui.community.moim.schedule.FriendInviteActivity.Companion.MOIM_PARTICIPANT_ID_KEY
import com.mongmong.namo.presentation.utils.PermissionChecker.hasImagePermission
import com.mongmong.namo.presentation.utils.converter.PickerConverter.setSelectedTime
import dagger.hilt.android.AndroidEntryPoint
import java.lang.NullPointerException

@AndroidEntryPoint
class MoimScheduleActivity : BaseActivity<ActivityMoimScheduleBinding>(R.layout.activity_moim_schedule),
    ConfirmDialogInterface {

    private lateinit var getLocationResult : ActivityResultLauncher<Intent>

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private lateinit var participantAdapter: MoimParticipantRVAdapter

    private val viewModel : MoimScheduleViewModel by viewModels()

    private lateinit var getInviteResultData: ActivityResultLauncher<Intent>

    override fun setup() {
        binding.viewModel = viewModel

        initViews()
        initMapView()
        setResultLocation()
        initClickListeners()
        initObserve()
        initInviteResultData()
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

    private fun initViews() {
        viewModel.moimScheduleId = intent.getLongExtra(MOIM_ID_KEY, 0L)
        viewModel.setMoimSchedule()

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        binding.moimScheduleContainerLayout.startAnimation(slideAnimation)
    }

    private fun initClickListeners() {
        // editText에서 완료 클릭 시
        binding.moimScheduleTitleEt.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard() // 키보드 내림
                handled = true
            }
            handled
        }

        // 커버 이미지 설정
        binding.moimScheduleCoverImgIv.setOnClickListener {
            // 앨범 권한 확인 후 연결
            getGallery()
        }

        // 친구 초대 버튼 클릭
        binding.moimScheduleAddParticipantTv.setOnClickListener {
            // 친구 초대 화면으로 이동
            val intent = Intent(this, FriendInviteActivity::class.java).apply {
                putExtra(MOIM_INVITE_KEY, viewModel.moimSchedule.value!!.moimId)
                if (viewModel.getParticipantUserIdList().isNotEmpty()) putExtra(MOIM_PARTICIPANT_ID_KEY, viewModel.getParticipantUserIdList().toLongArray())
            }
            getInviteResultData.launch(intent)
        }

        // 게스트 초대 버튼 클릭 (편집 모드)
        binding.moimScheduleAddGuestTv.setOnClickListener {
            // 게스트 초대 다이얼로그
            GuestInviteDialog().show(this.supportFragmentManager, "GuestInviteDialog")
        }

        // 장소 클릭
        binding.moimSchedulePlaceLayout.setOnClickListener {
            hideKeyboard()
            getLocationPermission()
        }

        // 길찾기 버튼
        binding.moimSchedulePlaceKakaoBtn.setOnClickListener {
            hideKeyboard()

//            val url = "kakaomap://route?sp=&ep=${place_y},${place_x}&by=PUBLICTRANSIT"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
        }

        // 수락한 친구 일정 보기 버튼 클릭
        binding.moimScheduleLookInviteFriendScheduleBtn.setOnClickListener {
            // 캘린더 화면으로 이동
            startActivity(
                Intent(this, CommunityCalendarActivity::class.java).apply {
                    putExtra("isFriendCalendar", false)
                    putExtra("moim", viewModel.moimSchedule.value!!)
                }
            )
        }

        // 활동 기록하기 버튼 클릭 (편집 모드)
        binding.moimScheduleRecordActivityBtn.setOnClickListener {
            startActivity(
                Intent(this, MoimDiaryDetailActivity::class.java)
                    .putExtra("scheduleId", viewModel.moimSchedule.value?.moimId)
            )
        }

        // 닫기 클릭
        binding.moimScheduleCloseBtn.setOnClickListener {
            finish()
        }

        // 생성/저장 버튼 클릭
        binding.moimScheduleSaveBtn.setOnClickListener {
            if (viewModel.isCreateMode()) {
                insertSchedule() // 생성
            } else {
                editSchedule() // 수정
            }
        }

        // 삭제 클릭
        binding.moimScheduleDeleteBtn.setOnClickListener {
            // 삭제 확인 다이얼로그 띄우기
            showCustomDialog(getString(R.string.dialog_moim_delete_title), R.string.dialog_moim_delete_content, R.string.delete, 0)
        }
    }

    private fun initInviteResultData() {
        getInviteResultData = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            // 변경사항 확인
            val isEdited = result.data?.getBooleanExtra(MOIM_EDIT_KEY, false)
            if (isEdited == true) viewModel.getMoimScheduleDetailInfo() // 변경 사항이 있다면 업데이트
        }
    }

    /** 모임 일정 추가 */
    private fun insertSchedule() {
        viewModel.postMoimSchedule()
    }

    /** 모임 일정 수정 */
    private fun editSchedule() {
        viewModel.onClickEditButton()
    }

    /** 모임 일정 삭제 */
    private fun deleteSchedule() {
        viewModel.deleteMoimSchedule()
    }

    private fun initObserve() {
        viewModel.moimSchedule.observe(this) { schedule ->
            if (schedule != null) {
                initPickerClickListeners()
                setContent()
            }
            if (schedule.participants.isNotEmpty()) {
                 setParticipantAdapter()
            }
        }

        viewModel.successState.observe(this) { successState ->
            Log.e("MoimScheduleACT", "API 요청 성공 여부: ${successState.isSuccess}")
            if (successState.isSuccess) { // 요청이 성공한 경우
                var message = ""
                message = when (successState.type) {
                    SuccessType.ADD -> "모임 일정이 등록되었습니다."
                    SuccessType.EDIT -> "모임 일정이 수정되었습니다."
                    SuccessType.DELETE -> "모임 일정이 삭제되었습니다."
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // 편집여부 전달 (업데이트)
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(MOIM_EDIT_KEY, successState.isSuccess) // 편집 여부
                    if (successState.type == SuccessType.ADD) putExtra(MOIM_CREATE_KEY, MoimCreateInfo(
                        moimId = viewModel.createdMoimId,
                        title = viewModel.moimSchedule.value?.title.toString()
                    )) // 친구 초대 팝업 표시용
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun showPicker(clicked: TextView) {
        hideKeyboard()
        val prevClickedPicker = viewModel.prevClickedPicker.value
        if (prevClickedPicker != clicked) {
            prevClickedPicker?.setTextColor(ContextCompat.getColor(this, R.color.main_text))
            clicked.setTextColor(ContextCompat.getColor(this, R.color.main))
            togglePicker(prevClickedPicker, false)
            togglePicker(clicked, true)
            viewModel.updatePrevClickedPicker(clicked) // prevClickedPicker 값을 현재 clicked로 업데이트
            return
        }
        // 피커 닫기 진행
        clicked.setTextColor(ContextCompat.getColor(this, R.color.main_text))
        togglePicker(clicked, false)
        viewModel.updatePrevClickedPicker(null)
    }

    private fun initPicker() {
        val period = viewModel.getDateTime() ?: return
        binding.moimScheduleStartTimeTp.apply { // 시작 시간
            hour = period.startDate.hourOfDay
            minute = period.startDate.minuteOfHour
        }
        binding.moimScheduleEndTimeTp.apply { // 종료 시간
            hour = period.endDate.hourOfDay
            minute = period.endDate.minuteOfHour
        }
        binding.moimScheduleStartDateDp.init(period.startDate.year, period.startDate.monthOfYear - 1, period.startDate.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(period.startDate.withDate(year, monthOfYear + 1, dayOfMonth), null)
        }
        binding.moimScheduleEndDateDp.init(period.endDate.year, period.endDate.monthOfYear - 1, period.endDate.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(null, period.endDate.withDate(year, monthOfYear + 1, dayOfMonth))
        }
    }

    private fun initPickerClickListeners() {
        // 시작 시간
        with(binding.moimScheduleStartTimeTp) {
            val startDateTime = viewModel.getDateTime()?.startDate!!
            this.hour = startDateTime.hourOfDay
            this.minute = startDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(setSelectedTime(startDateTime, hourOfDay, minute), null)
            }
        }
        // 종료 시간
        with(binding.moimScheduleEndTimeTp) {
            val endDateTime = viewModel.getDateTime()?.endDate!!
            this.hour = endDateTime.hourOfDay
            this.minute = endDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(null, setSelectedTime(endDateTime, hourOfDay, minute))
            }
        }
        // 시작일 - 날짜
        binding.moimScheduleStartDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 날짜
        binding.moimScheduleEndDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 시작일 - 시간
        binding.moimScheduleStartTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 시간
        binding.moimScheduleEndTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
    }

    private fun togglePicker(pickerText: TextView?, open: Boolean) {
        pickerText?.let { pickerTextView ->
            val picker: MotionLayout = when (pickerTextView) {

                binding.moimScheduleStartDateTv -> binding.moimScheduleStartDateLayout
                binding.moimScheduleEndDateTv -> binding.moimScheduleEndDateLayout
                binding.moimScheduleStartTimeTv -> binding.moimScheduleStartTimeLayout
                binding.moimScheduleEndTimeTv -> binding.moimScheduleEndTimeLayout
                else -> binding.moimScheduleStartTimeLayout
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

    private fun getLocationPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(ORIGIN_ACTIVITY_INTENT_KEY, "GroupSchedule")
                val placeData = viewModel.getPlace()
                if (placeData != null) {
                    intent.apply {
                        putExtra("PREV_PLACE_NAME", placeData.first)
                        putExtra("PREV_PLACE_X", placeData.second.longitude)
                        putExtra("PREV_PLACE_Y", placeData.second.latitude)
                    }
                }
                getLocationResult.launch(intent)
            } catch (e : NullPointerException) {
                Log.e("LOCATION_ERROR", e.toString())
            }
        }
    }

    private fun setParticipantAdapter() {
        participantAdapter = MoimParticipantRVAdapter(viewModel.moimSchedule.value!!.participants)
        binding.moimScheduleParticipantRv.apply {
            adapter = participantAdapter
            layoutManager = FlexboxLayoutManager(context).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
            }
        }
    }

    //TODO: 게스트 리사이클러뷰 연결

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.moimScheduleTitleEt.windowToken, 0)
    }

    private fun setResultLocation() {
        getLocationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updatePlace(
                    result.data?.getStringExtra(MapActivity.PLACE_NAME_KEY)!!,
                    result.data?.getDoubleExtra(MapActivity.PLACE_X_KEY, 0.0)!!,
                    result.data?.getDoubleExtra(MapActivity.PLACE_Y_KEY, 0.0)!!
                )
                setMapContent()
            }
        }
    }

    @SuppressLint("IntentReset")
    private fun getGallery() {
        if (hasImagePermission(this)) {
            val galleryIntent = Intent(Intent.ACTION_PICK).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                type = "image/*"
                data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            getImage.launch(galleryIntent)
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }
            ActivityCompat.requestPermissions(this, permissions, 200)
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // 서버에 이미지를 업로드
                viewModel.updateImage(uri)
            }
        }
    }

    private fun initMapView() {
        mapView = binding.moimSchedulePlaceContainer
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

    private fun setContent() {
        // 시작일, 종료일
        initPicker()
        // 장소
        setMapContent()
    }

    // 모임 위치 표시
    private fun setMapContent() {
        kakaoMap?.labelManager?.layer?.removeAll()
        val mapData = viewModel.getPlace() ?: return
        Log.d("GroupScheduleActivity", mapData.toString())
//        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.VISIBLE
        binding.moimSchedulePlaceContainer.visibility = ViewGroup.VISIBLE

        // 지도 위치 조정
        val latLng = mapData.second
        // 카메라를 마커의 위치로 이동
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, MapActivity.ZOOM_LEVEL))
        // 마커 추가
        kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng).setStyles(MapActivity.setPinStyle(false)))
    }

    private fun showCustomDialog(title: String, content: Int, buttonText: Int, id: Int) {
        val dialog = ConfirmDialog(this@MoimScheduleActivity, title, getString(content), getString(buttonText), id)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {
        when (id) {
            0 -> deleteSchedule() // 일정 삭제 진행
            1 -> { // 친구 초대 화면으로 이동
                val intent = Intent(this, FriendInviteActivity::class.java).apply {
                    putExtra(MOIM_INVITE_KEY, viewModel.moimSchedule.value!!.moimId)
                }
                getInviteResultData.launch(intent)
            }
        }
    }

    companion object {
        const val MOIM_ID_KEY = "moimId"
    }
}