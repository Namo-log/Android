package com.mongmong.namo.presentation.ui.group.schedule

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.mongmong.namo.presentation.ui.MainActivity.Companion.GROUP_MEMBER_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_X_INTENT_KEY
import com.mongmong.namo.presentation.ui.MainActivity.Companion.PLACE_Y_INTENT_KEY
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.MoimSchduleMemberList
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.ActivityGroupScheduleBinding
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.presentation.ui.home.schedule.MoimScheduleViewModel
import com.mongmong.namo.presentation.ui.home.schedule.map.MapActivity
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.getInterval
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.PickerConverter
import com.mongmong.namo.presentation.utils.PickerConverter.parseDateTimeToDateText
import com.mongmong.namo.presentation.utils.PickerConverter.parseDateTimeToTimeText
import com.mongmong.namo.presentation.utils.PickerConverter.setSelectedTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import java.lang.NullPointerException

@AndroidEntryPoint
class GroupScheduleActivity : AppCompatActivity(), ConfirmDialogInterface {

    private lateinit var binding : ActivityGroupScheduleBinding

    private var isPostOrEdit : Boolean = true

    private lateinit var getLocationResult : ActivityResultLauncher<Intent>

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView
    private var place_name : String = "없음"
    private var place_x : Double = 0.0
    private var place_y : Double = 0.0

    private lateinit var getMemberResult : ActivityResultLauncher<Intent>
    private var originalMembers : MoimSchduleMemberList = MoimSchduleMemberList(listOf())
    private var selectedMembers : MoimSchduleMemberList = MoimSchduleMemberList(listOf())
    private var selectedIds : ArrayList<Long> = arrayListOf()
    private lateinit var group : Group
    private var date = DateTime(System.currentTimeMillis())
    private var postGroupSchedule : AddMoimScheduleRequestBody = AddMoimScheduleRequestBody()
    private var editGroupSchedule : EditMoimScheduleRequestBody = EditMoimScheduleRequestBody()

    private var prevClicked : TextView? = null
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())

    private val viewModel : MoimScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupScheduleBinding.inflate(layoutInflater)

        setContentView(binding.root)

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
        group = intent.getSerializableExtra("group") as Group
        originalMembers.memberList = group.groupMembers

        val nowDay = intent.getLongExtra("nowDay", 0L)
        val moimScheduleBody = intent.getSerializableExtra("moimSchedule") as? MoimScheduleBody
        if (moimScheduleBody != null) { // 모일 일정 수정
            isPostOrEdit = false
            setEditSchedule(moimScheduleBody)
            binding.scheduleDeleteBtn.visibility = View.VISIBLE
            binding.dialogGroupScheduleHeaderTv.text = "일정 편집"
            setContent()
            setMapContent()
        } else { // 모임 일정 생성
            binding.scheduleDeleteBtn.visibility = View.GONE
            binding.dialogGroupScheduleHeaderTv.text = "새 일정"
            if (nowDay != 0L) {
                date = DateTime(nowDay)
            }
            setDateTime(PickerConverter.getDefaultDate(date, true), PickerConverter.getDefaultDate(date, false))

            selectedMembers = originalMembers
            selectedIds.clear()
            for (i in selectedMembers.memberList) {
                selectedIds.add(i.userId)
            }
            setMembers()
        }

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        binding.scheduleContainerLayout.startAnimation(slideAnimation)
    }

    private fun setEditSchedule(moimScheduleBody: MoimScheduleBody) {
        editGroupSchedule.moimScheduleId = moimScheduleBody.moimScheduleId
        editGroupSchedule.name = moimScheduleBody.name
        editGroupSchedule.startLong = moimScheduleBody.startDate
        editGroupSchedule.endLong = moimScheduleBody.endDate
        editGroupSchedule.interval = moimScheduleBody.interval
        editGroupSchedule.x = moimScheduleBody.x
        editGroupSchedule.y = moimScheduleBody.y
        editGroupSchedule.locationName = moimScheduleBody.locationName

        place_name = editGroupSchedule.name
        place_x = editGroupSchedule.x
        place_y = editGroupSchedule.y

        editGroupSchedule.users = moimScheduleBody.users.map { user -> user.userId }
        selectedIds = moimScheduleBody.users.map { user -> user.userId } as ArrayList<Long>
        Log.d("SetEditSchedule", editGroupSchedule.toString())
    }

    private fun initClickListeners() {
        // 참여자 클릭
        binding.dialogGroupScheduleMemberTv.setOnClickListener {
            val intent = Intent(this, GroupScheduleMemberActivity::class.java)
            intent.putExtra("members", originalMembers)
            intent.putExtra("selectedIds", selectedIds.toLongArray())
            getMemberResult.launch(intent)
        }

        /** time & date 클릭 */
        binding.dialogGroupScheduleStartDateTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleStartDateTv)
        }
        // 종료일 - 날짜
        binding.dialogGroupScheduleEndDateTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleEndDateTv)
        }
        // 시작일 - 시간
        binding.dialogGroupScheduleStartTimeTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleStartTimeTv)
        }
        // 종료일 - 시간
        binding.dialogGroupScheduleEndTimeTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleEndTimeTv)
        }

        // 장소 클릭
        binding.dialogGroupSchedulePlaceLayout.setOnClickListener {
            hideKeyboard()
            getLoctionPermission()
        }

        // 길찾기 버튼
        binding.dialogGroupSchedulePlaceKakaoBtn.setOnClickListener {
            hideKeyboard()

            val url = "kakaomap://route?sp=&ep=${place_y},${place_x}&by=PUBLICTRANSIT"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
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
            storeContent()
            Log.d("STORE_GROUP_SCHEDULE", "is Post or Edit ? ${isPostOrEdit}")
            Log.d("STORE_GROUP_SCHEDULE", if (isPostOrEdit) postGroupSchedule.toString() else editGroupSchedule.toString())
            if (isPostOrEdit) { // 생성
                insertSchedule(postGroupSchedule)
            } else { // 수정
                editSchedule(editGroupSchedule)
            }
        }

        // 삭제 클릭
        binding.scheduleDeleteBtn.setOnClickListener {
            if (!isPostOrEdit) {
                // 삭제 확인 다이얼로그 띄우기
                showDialog()
            }
        }
    }

    /** 모임 일정 추가 */
    private fun insertSchedule(moimSchedule: AddMoimScheduleRequestBody) {
        viewModel.postMoimSchedule(moimSchedule)
        Toast.makeText(this, "모임 일정이 등록되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 모임 일정 수정 */
    private fun editSchedule(moimSchedule: EditMoimScheduleRequestBody) {
        viewModel.editMoimSchedule(moimSchedule)
        Toast.makeText(this, "모임 일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 모임 일정 삭제 */
    private fun deleteSchedule(moimScheduleId: Long) {
        viewModel.deleteMoimSchedule(moimScheduleId)
        Toast.makeText(this, "모임 일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initObservers() {
        viewModel.schedule.observe(this) {
            //
        }
    }

    private fun storeContent() {
        if (isPostOrEdit) {
            postGroupSchedule.groupId = group.groupId
            postGroupSchedule.name = binding.dialogGroupScheduleTitleEt.text.toString()
            postGroupSchedule.startLong = startDateTime.millis / 1000
            postGroupSchedule.endLong = endDateTime.millis / 1000
            postGroupSchedule.interval = getInterval(postGroupSchedule.startLong, postGroupSchedule.endLong)
            postGroupSchedule.x = place_x
            postGroupSchedule.y = place_y
            postGroupSchedule.locationName = place_name
            postGroupSchedule.users = selectedIds
        } else {
            editGroupSchedule.name = binding.dialogGroupScheduleTitleEt.text.toString()
            editGroupSchedule.startLong = startDateTime.millis / 1000
            editGroupSchedule.endLong = endDateTime.millis / 1000
            editGroupSchedule.interval = getInterval(editGroupSchedule.startLong, editGroupSchedule.endLong)
            editGroupSchedule.x = place_x
            editGroupSchedule.y = place_y
            editGroupSchedule.locationName = place_name
            editGroupSchedule.users = selectedIds
        }
    }

    private fun setPicker(clicked: TextView) {
        hideKeyboard()
        prevClicked = if (prevClicked != clicked) {
            prevClicked?.setTextColor(resources.getColor(R.color.textGray))
            clicked.setTextColor(resources.getColor(R.color.MainOrange))
            togglePicker(prevClicked, false)
            togglePicker(clicked, true)
            clicked // prevClicked 값을 현재 clicked로 업데이트
        } else {
            clicked.setTextColor(resources.getColor(R.color.textGray))
            togglePicker(clicked, false)
            null
        }
    }


    private fun setContent() {
        binding.dialogGroupScheduleTitleEt.setText(editGroupSchedule.name)

        //참여자 넣어야됨
        binding.dialogGroupScheduleMemberTv.text = group.groupMembers.filter { it.userId in editGroupSchedule.users }.map { it.userName }.joinToString(", ")

        //시작일, 종료일, 시작시간, 종료시간
        setDateTime(PickerConverter.parseLongToDateTime(editGroupSchedule.startLong), PickerConverter.parseLongToDateTime(editGroupSchedule.endLong))

        //장소
        place_name = editGroupSchedule.locationName
        place_x = editGroupSchedule.x
        place_y = editGroupSchedule.y
    }

    private fun setDateTime(start : DateTime, end : DateTime) {
        startDateTime = start
        endDateTime = end

        initPickerText()
    }

    private fun initPickerText() {
        // 시작일
        binding.dialogGroupScheduleStartDateTv.text = parseDateTimeToDateText(startDateTime)
        binding.dialogGroupScheduleStartTimeTv.text = parseDateTimeToTimeText(startDateTime)
        binding.dialogGroupScheduleStartTimeTp.hour = startDateTime.hourOfDay
        binding.dialogGroupScheduleStartTimeTp.minute = startDateTime.minuteOfHour
        // 종료일
        binding.dialogGroupScheduleEndDateTv.text = parseDateTimeToDateText(endDateTime)
        binding.dialogGroupScheduleEndTimeTv.text = parseDateTimeToTimeText(endDateTime)
        binding.dialogGroupScheduleEndTimeTp.hour = endDateTime.hourOfDay
        binding.dialogGroupScheduleEndTimeTp.minute = endDateTime.minuteOfHour

        // picker 리스너
        binding.dialogGroupScheduleStartTimeTp.setOnTimeChangedListener { _, hourOfDay, minute ->
            startDateTime = setSelectedTime(startDateTime, hourOfDay, minute)
            binding.dialogGroupScheduleStartTimeTv.text = parseDateTimeToTimeText(startDateTime)
        }
        binding.dialogGroupScheduleEndTimeTp.setOnTimeChangedListener { _, hourOfDay, minute ->
            endDateTime = setSelectedTime(startDateTime, hourOfDay, minute)
            binding.dialogGroupScheduleEndTimeTv.text = parseDateTimeToTimeText(endDateTime)
        }

        binding.dialogGroupScheduleStartDateDp.init(startDateTime.year, startDateTime.monthOfYear - 1, startDateTime.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            startDateTime = startDateTime.withDate(year, monthOfYear + 1, dayOfMonth)
            if (startDateTime.isAfter(endDateTime)) {
                endDateTime = startDateTime
                binding.dialogGroupScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))
            }
            binding.dialogGroupScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        }
        binding.dialogGroupScheduleEndDateDp.init(endDateTime.year, endDateTime.monthOfYear - 1, endDateTime.dayOfMonth) { _, year, monthOfYear, dayOfMonth ->
            endDateTime = endDateTime.withDate(year, monthOfYear + 1, dayOfMonth)
            if (startDateTime.isAfter(endDateTime)) {
                startDateTime = endDateTime
                binding.dialogGroupScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
            }
            binding.dialogGroupScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))
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

    private fun getLoctionPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(ORIGIN_ACTIVITY_INTENT_KEY, "GroupSchedule")
                if (isPostOrEdit) {
                    if (postGroupSchedule.x != 0.0 && postGroupSchedule.y != 0.0) {
                        intent.putExtra("PREV_PLACE_NAME", postGroupSchedule.locationName)
                        intent.putExtra("PREV_PLACE_X", postGroupSchedule.x)
                        intent.putExtra("PREV_PLACE_Y", postGroupSchedule.y)
                    }
                } else {
                    if (editGroupSchedule.x != 0.0 && editGroupSchedule.y != 0.0) {
                        intent.putExtra("PREV_PLACE_NAME", editGroupSchedule.locationName)
                        intent.putExtra("PREV_PLACE_X", editGroupSchedule.x)
                        intent.putExtra("PREV_PLACE_Y", editGroupSchedule.y)
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
                selectedMembers = result.data?.getSerializableExtra(GROUP_MEMBER_INTENT_KEY) as MoimSchduleMemberList
                setMembers()
            }
        }
    }

    private fun setMembers() {
        selectedIds.clear()
        for (i in selectedMembers.memberList.indices) {
            selectedIds.add(selectedMembers.memberList[i].userId)
        }

        binding.dialogGroupScheduleMemberTv.text =
            selectedMembers.memberList.joinToString(", ") { it.userName }
        Log.d("GROUP_MEMBER", selectedIds.toString())
    }


    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.dialogGroupScheduleTitleEt.windowToken, 0)
    }

    private fun setResultLocation() {
        getLocationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                place_name = result.data?.getStringExtra(PLACE_NAME_INTENT_KEY)!!
                place_x = result.data?.getDoubleExtra(PLACE_X_INTENT_KEY, 0.0)!!
                place_y = result.data?.getDoubleExtra(PLACE_Y_INTENT_KEY, 0.0)!!

                if (isPostOrEdit) {
                    postGroupSchedule.locationName = place_name
                    postGroupSchedule.x = place_x
                    postGroupSchedule.y = place_y
                } else {
                    editGroupSchedule.locationName = place_name
                    editGroupSchedule.x = place_x
                    editGroupSchedule.y = place_y
                }
                if (place_x != 0.0 || place_y != 0.0) {
                    setMapContent()
                }
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

            override fun getPosition(): LatLng {
                return LatLng.from(place_y, place_x)
            }

            override fun getZoomLevel(): Int {
                // 지도 시작 시 확대/축소 줌 레벨 설정
                return MapActivity.ZOOM_LEVEL
            }
        })
    }

    // 모임 위치 표시
    private fun setMapContent() {
        binding.dialogGroupSchedulePlaceNameTv.text = place_name
//        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.VISIBLE
        binding.dialogGroupSchedulePlaceContainer.visibility = ViewGroup.VISIBLE

        // 지도 위치 조정
        val latLng = LatLng.from(place_y, place_x)
        // 카메라를 마커의 위치로 이동
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, MapActivity.ZOOM_LEVEL))

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
        deleteSchedule(editGroupSchedule.moimScheduleId)
    }
}