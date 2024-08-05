package com.mongmong.namo.presentation.ui.group.schedule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.mongmong.namo.presentation.ui.MainActivity.Companion.GROUP_MEMBER_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.MoimSchduleMemberList
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.ActivityGroupScheduleBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.PickerConverter
import com.mongmong.namo.presentation.utils.PickerConverter.setSelectedTime
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import java.lang.NullPointerException

@AndroidEntryPoint
class GroupScheduleActivity
    : BaseActivity<ActivityGroupScheduleBinding>(R.layout.activity_group_schedule),
    ConfirmDialogInterface {

    private lateinit var getLocationResult : ActivityResultLauncher<Intent>

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private lateinit var getMemberResult : ActivityResultLauncher<Intent>

    private val viewModel : MoimScheduleViewModel by viewModels()

    override fun setup() {
        binding.viewModel = viewModel

        initMapView()
        setInit()
        setResultLocation()
        setResultMember()
        initClickListeners()
        initObservers()
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

    private fun setInit() {
        viewModel.setGroup(intent.getSerializableExtra("group") as Group)
        val nowDay = intent.getLongExtra("nowDay", 0L)
        val moimScheduleBody = intent.getSerializableExtra("moimSchedule") as? MoimScheduleBody
        if (moimScheduleBody != null) { // 모일 일정 수정
            viewModel.setSchedule(moimScheduleBody)
        } else { // 모임 일정 생성
            viewModel.setSchedule(
                MoimScheduleBody(
                    startLong = PickerConverter.getDefaultDate(DateTime(nowDay), true),
                    endLong = PickerConverter.getDefaultDate(DateTime(nowDay), false),
                )
            )
        }

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        binding.scheduleContainerLayout.startAnimation(slideAnimation)
    }

    private fun initClickListeners() {
        // 참여자 클릭
        binding.dialogGroupScheduleMemberTv.setOnClickListener {
            val intent = Intent(this, GroupScheduleMemberActivity::class.java)
            intent.apply {
                putExtra("members", MoimSchduleMemberList(viewModel.group.value!!.groupMembers))
                putExtra("selectedIds", viewModel.getSelectedMemberId().toLongArray())
            }
            getMemberResult.launch(intent)
        }

        initPickerClickListeners()

        // 장소 클릭
        binding.dialogGroupSchedulePlaceLayout.setOnClickListener {
            hideKeyboard()
            getLocationPermission()
        }

        // 길찾기 버튼
        binding.dialogGroupSchedulePlaceKakaoBtn.setOnClickListener {
            hideKeyboard()

//            val url = "kakaomap://route?sp=&ep=${place_y},${place_x}&by=PUBLICTRANSIT"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
        }

        // 닫기 클릭
        binding.dialogGroupScheduleCloseBtn.setOnClickListener {
            finish()
        }

        // 저장 클릭
        binding.dialogGroupScheduleSaveBtn.setOnClickListener {
            if (binding.dialogGroupScheduleTitleEt.text.toString().isEmpty()) {
                Toast.makeText(this, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.isCreateMode()) { // 생성
                insertSchedule()
            } else { // 수정
                editSchedule()
            }
        }

        // 삭제 클릭
        binding.scheduleDeleteBtn.setOnClickListener {
            if (!viewModel.isCreateMode()) {
                // 삭제 확인 다이얼로그 띄우기
                showDialog()
            }
        }
    }

    /** 모임 일정 추가 */
    private fun insertSchedule() {
        viewModel.postMoimSchedule()
        Toast.makeText(this, "모임 일정이 등록되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 모임 일정 수정 */
    private fun editSchedule() {
        viewModel.editMoimSchedule()
        Toast.makeText(this, "모임 일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 모임 일정 삭제 */
    private fun deleteSchedule() {
        viewModel.deleteMoimSchedule()
        Toast.makeText(this, "모임 일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initObservers() {
        viewModel.schedule.observe(this) { schedule ->
            setContent()
        }
    }

    private fun showPicker(clicked: TextView) {
        hideKeyboard()
        val prevClickedPicker = viewModel.prevClickedPicker.value
        if (prevClickedPicker != clicked) {
            prevClickedPicker?.setTextColor(ContextCompat.getColor(this, R.color.textGray))
            clicked.setTextColor(ContextCompat.getColor(this, R.color.mainOrange))
            togglePicker(prevClickedPicker, false)
            togglePicker(clicked, true)
            viewModel.updatePrevClickedPicker(clicked) // prevClickedPicker 값을 현재 clicked로 업데이트
            return
        }
        // 피커 닫기 진행
        clicked.setTextColor(ContextCompat.getColor(this, R.color.textGray))
        togglePicker(clicked, false)
        viewModel.updatePrevClickedPicker(null)
    }

    private fun initPicker() {
        val dateTimePair = viewModel.getDateTime() ?: return
        binding.dialogGroupScheduleStartTimeTp.apply { // 시작 시간
            hour = dateTimePair.first.hourOfDay
            minute = dateTimePair.first.minuteOfHour
        }
        binding.dialogGroupScheduleEndTimeTp.apply { // 종료 시간
            hour = dateTimePair.second.hourOfDay
            minute = dateTimePair.second.minuteOfHour
        }
        binding.dialogGroupScheduleStartDateDp.init(dateTimePair.first.year, dateTimePair.first.monthOfYear - 1, dateTimePair.first.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(dateTimePair.first.withDate(year, monthOfYear + 1, dayOfMonth), null)
        }
        binding.dialogGroupScheduleEndDateDp.init(dateTimePair.second.year, dateTimePair.second.monthOfYear - 1, dateTimePair.second.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            viewModel.updateTime(null, dateTimePair.second.withDate(year, monthOfYear + 1, dayOfMonth))
        }
    }

    private fun initPickerClickListeners() {
        // 시작 시간
        with(binding.dialogGroupScheduleStartTimeTp) {
            val startDateTime = viewModel.getDateTime()?.first!!
            this.hour = startDateTime.hourOfDay
            this.minute = startDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(setSelectedTime(startDateTime, hourOfDay, minute), null)
            }
        }
        // 종료 시간
        with(binding.dialogGroupScheduleEndTimeTp) {
            val endDateTime = viewModel.getDateTime()?.second!!
            this.hour = endDateTime.hourOfDay
            this.minute = endDateTime.minuteOfHour
            this.setOnTimeChangedListener { _, hourOfDay, minute ->
                viewModel.updateTime(null, setSelectedTime(endDateTime, hourOfDay, minute))
            }
        }
        // 시작일 - 날짜
        binding.dialogGroupScheduleStartDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 날짜
        binding.dialogGroupScheduleEndDateTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 시작일 - 시간
        binding.dialogGroupScheduleStartTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
        // 종료일 - 시간
        binding.dialogGroupScheduleEndTimeTv.setOnClickListener {
            showPicker(it as TextView)
        }
    }

    private fun togglePicker(pickerText: TextView?, open: Boolean) {
        pickerText?.let { pickerTextView ->
            val picker: MotionLayout = when (pickerTextView) {
                binding.dialogGroupScheduleStartDateTv -> binding.dialogGroupScheduleStartDateLayout
                binding.dialogGroupScheduleEndDateTv -> binding.dialogGroupScheduleEndDateLayout
                binding.dialogGroupScheduleStartTimeTv -> binding.dialogGroupScheduleStartTimeLayout
                binding.dialogGroupScheduleEndTimeTv -> binding.dialogGroupScheduleEndTimeLayout
                else -> binding.dialogGroupScheduleStartTimeLayout
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

    private fun setResultMember() {
        getMemberResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateMembers(result.data?.getSerializableExtra(GROUP_MEMBER_INTENT_KEY) as MoimSchduleMemberList)
            }
        }
    }


    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.dialogGroupScheduleTitleEt.windowToken, 0)
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

    private fun initMapView() {
        mapView = binding.dialogGroupSchedulePlaceContainer
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
        binding.dialogGroupSchedulePlaceContainer.visibility = ViewGroup.VISIBLE

        // 지도 위치 조정
        val latLng = mapData.second
        // 카메라를 마커의 위치로 이동
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, MapActivity.ZOOM_LEVEL))
        // 마커 추가
        kakaoMap?.labelManager?.layer?.addLabel(LabelOptions.from(latLng).setStyles(MapActivity.setPinStyle(false)))
    }

    private fun showDialog() {
        // 탈퇴 확인 다이얼로그
        val title = "모임 일정을 정말 삭제하시겠어요?"
        val content = "삭제한 모임 일정은\n모든 참여자의 일정에서 삭제됩니다."

        val dialog = ConfirmDialog(this@GroupScheduleActivity, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {
        // 일정 삭제 진행
        deleteSchedule()
    }
}