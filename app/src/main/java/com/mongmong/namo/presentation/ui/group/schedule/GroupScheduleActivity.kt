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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
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
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.joda.time.DateTime
import java.lang.NullPointerException

@AndroidEntryPoint
class GroupScheduleActivity : AppCompatActivity(), ConfirmDialogInterface {

    private lateinit var binding : ActivityGroupScheduleBinding

    private var isPostOrEdit : Boolean = true

    private lateinit var getLocationResult : ActivityResultLauncher<Intent>
    lateinit var mapView: MapView
    var mapViewContainer: RelativeLayout? = null
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

        setInit()
        setResultLocation()
        setResultMember()
        clickListener()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        initMapView()
    }

    override fun onPause() {
        super.onPause()
        mapViewContainer?.removeView(mapView)
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
        } else { // 모임 일정 생성
            binding.scheduleDeleteBtn.visibility = View.GONE
            binding.dialogGroupScheduleHeaderTv.text = "새 일정"
            if (nowDay != 0L) {
                date = DateTime(nowDay)
            }
            setDateTime(DateTime(date.year, date.monthOfYear, date.dayOfMonth, 8, 0, 0, 0), DateTime(date.year, date.monthOfYear, date.dayOfMonth, 9, 0, 0, 0))

            selectedMembers = originalMembers
            selectedIds.clear()
            for (i in selectedMembers.memberList) {
                selectedIds.add(i.userId)
            }
            setMembers()
        }

        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.GONE
        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.GONE
        mapViewContainer?.visibility = View.GONE

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

    private fun clickListener() {
        // 참여자 클릭
        binding.dialogGroupScheduleMemberTv.setOnClickListener {
            val intent = Intent(this, GroupScheduleMemberActivity::class.java)
            intent.putExtra("members", originalMembers)
            intent.putExtra("selectedIds", selectedIds.toLongArray())
//            Log.d("PUT_INTENT", originalMembers.toString())
//            Log.d("PUT_INTENT", selectedIds.toString())
            getMemberResult.launch(intent)
        }

        // time & date 클릭
        binding.dialogGroupScheduleStartDateTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleStartDateTv)
        }
        binding.dialogGroupScheduleEndDateTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleEndDateTv)
        }
        binding.dialogGroupScheduleStartTimeTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleStartTimeTv)
        }
        binding.dialogGroupScheduleEndTimeTv.setOnClickListener {
            setPicker(binding.dialogGroupScheduleEndTimeTv)
        }

        // 장소 클릭
        binding.dialogGroupSchedulePlaceLayout.setOnClickListener {
            hideKeyboard()
            getLoctionPermission()
        }

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
            postGroupSchedule.moimId = group.groupId
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
        if (prevClicked != clicked) {
            prevClicked?.setTextColor(resources.getColor(R.color.textGray))
            clicked.setTextColor(resources.getColor(R.color.MainOrange))
            togglePicker(prevClicked, false)
            togglePicker(clicked, true)
            prevClicked = clicked // prevClicked 값을 현재 clicked로 업데이트
        } else {
            clicked.setTextColor(resources.getColor(R.color.textGray))
            togglePicker(clicked, false)
            prevClicked = null
        }
    }


    private fun setContent() {
        binding.dialogGroupScheduleTitleEt.setText(editGroupSchedule.name)

        //참여자 넣어야됨
        binding.dialogGroupScheduleMemberTv.text = group.groupMembers.filter { it.userId in editGroupSchedule.users }.map { it.userName }.joinToString(", ")

        //시작일, 종료일, 시작시간, 종료시간
        setDateTime(DateTime(editGroupSchedule.startLong * 1000L), DateTime(editGroupSchedule.endLong * 1000L))

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
        binding.dialogGroupScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        binding.dialogGroupScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))

        binding.dialogGroupScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        binding.dialogGroupScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))

        binding.dialogGroupScheduleStartTimeTp.hour = startDateTime.hourOfDay
        binding.dialogGroupScheduleStartTimeTp.minute = startDateTime.minuteOfHour

        binding.dialogGroupScheduleEndTimeTp.hour = endDateTime.hourOfDay
        binding.dialogGroupScheduleEndTimeTp.minute = endDateTime.minuteOfHour

        // picker 리스너
        binding.dialogGroupScheduleStartTimeTp.setOnTimeChangedListener { _, hourOfDay, minute ->
            startDateTime = startDateTime.withTime(hourOfDay, minute, 0, 0)
            if (startDateTime.millis > endDateTime.millis) {
                endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
                binding.dialogGroupScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
            }
            binding.dialogGroupScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        }
        binding.dialogGroupScheduleEndTimeTp.setOnTimeChangedListener { _, hourOfDay, minute ->
            endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
            if (endDateTime.millis < startDateTime.millis) {
                startDateTime = startDateTime.withTime(hourOfDay, minute, 0, 0)
                binding.dialogGroupScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
            }
            binding.dialogGroupScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
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
        mapView = MapView(this).also {
            mapViewContainer = RelativeLayout(this)
            mapViewContainer?.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            binding.dialogGroupSchedulePlaceContainer.addView(mapViewContainer)
            mapViewContainer?.addView(it)
        }
        Log.d("InitMapView", "InitMapView 실행")


        if (place_x != 0.0 || place_y != 0.0) {
            setMapContent()
        }
    }

    private fun setMapContent() {
        binding.dialogGroupSchedulePlaceNameTv.text = place_name
        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.VISIBLE
        binding.dialogGroupSchedulePlaceContainer.visibility = ViewGroup.VISIBLE
        mapViewContainer?.visibility = View.VISIBLE

        var mapPoint = MapPoint.mapPointWithGeoCoord(place_y, place_x)
        mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)

        var marker = MapPOIItem()
        marker.itemName = place_name
        marker.tag = 0
        marker.mapPoint = mapPoint
        marker.markerType = MapPOIItem.MarkerType.BluePin
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin

        mapView.addPOIItem(marker)
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