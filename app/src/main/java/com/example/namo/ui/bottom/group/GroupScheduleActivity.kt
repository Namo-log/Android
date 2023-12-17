package com.example.namo.ui.bottom.group

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import com.example.namo.MainActivity
import com.example.namo.MainActivity.Companion.GROUP_MEMBER_INTENT_KEY
import com.example.namo.MainActivity.Companion.ORIGIN_ACTIVITY_INTENT_KEY
import com.example.namo.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.example.namo.MainActivity.Companion.PLACE_X_INTENT_KEY
import com.example.namo.MainActivity.Companion.PLACE_Y_INTENT_KEY
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.group.AddMoimSchedule
import com.example.namo.data.entity.group.EditMoimSchedule
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.moim.AddMoimScheduleResponse
import com.example.namo.data.remote.moim.Moim
import com.example.namo.data.remote.moim.MoimListUserList
import com.example.namo.data.remote.moim.MoimSchedule
import com.example.namo.data.remote.moim.MoimScheduleView
import com.example.namo.data.remote.moim.MoimService
import com.example.namo.databinding.ActivityGroupScheduleBinding
import com.example.namo.ui.bottom.home.schedule.map.MapActivity
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.example.namo.utils.ConfirmDialog
import com.example.namo.utils.ConfirmDialogInterface
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.joda.time.DateTime
import java.lang.NullPointerException

class GroupScheduleActivity : AppCompatActivity(), ConfirmDialogInterface, MoimScheduleView {

    private lateinit var binding : ActivityGroupScheduleBinding
    private lateinit var db : NamoDatabase
    private var isPostOrEdit : Boolean = true

    private lateinit var getLocationResult : ActivityResultLauncher<Intent>
    lateinit var mapView: MapView
    var mapViewContainer: RelativeLayout? = null
    private var place_name : String = "없음"
    private var place_x : Double = 0.0
    private var place_y : Double = 0.0

    private lateinit var getMemberResult : ActivityResultLauncher<Intent>
    private var originalMembers : MoimListUserList = MoimListUserList(listOf())
    private var selectedMembers : MoimListUserList = MoimListUserList(listOf())
    private var selectedIds : ArrayList<Long> = arrayListOf()
    private lateinit var group : Moim
    private var date = DateTime(System.currentTimeMillis())
    private var postGroupSchedule : AddMoimSchedule = AddMoimSchedule()
    private var editGroupSchedule : EditMoimSchedule = EditMoimSchedule()

    private var prevClicked : TextView? = null
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupScheduleBinding.inflate(layoutInflater)
        db = NamoDatabase.getInstance(this)
        setContentView(binding.root)

        group = intent.getSerializableExtra("group") as Moim
        originalMembers.memberList = group.moimUsers

        val nowDay = intent.getLongExtra("nowDay", 0L)
        val moimSchedule = intent.getSerializableExtra("moimSchedule") as? MoimSchedule
        if (moimSchedule != null) {
            isPostOrEdit = false
            setEditSchedule(moimSchedule)
            binding.scheduleDeleteBtn.visibility = View.VISIBLE
            binding.dialogGroupScheduleHeaderTv.text = "모임 일정 편집"
//            selectedMembers = moimSchedule.users
            setContent()
        } else {
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

//        if (this.event.eventId != 0L) {
//            binding.dialogGroupScheduleHeaderTv.text = "일정 편집"
//            scheduleIdx = this.event.eventId
//        } else {
//            binding.dialogGroupScheduleHeaderTv.text = "새 일정"
//        }

        setResultLocation()
        setResultMember()
        clickListener()
    }

    override fun onResume() {
        super.onResume()
        initMapView()
    }

    override fun onPause() {
        super.onPause()
        mapViewContainer?.removeView(mapView)
    }

    private fun setEditSchedule(moimSchedule: MoimSchedule) {
        editGroupSchedule.moimScheduleId = moimSchedule.moimScheduleId
        editGroupSchedule.name = moimSchedule.name
        editGroupSchedule.startLong = moimSchedule.startDate
        editGroupSchedule.endLong = moimSchedule.endDate
        editGroupSchedule.interval = moimSchedule.interval
        editGroupSchedule.x = moimSchedule.x
        editGroupSchedule.y = moimSchedule.y
        editGroupSchedule.locationName = moimSchedule.locationName

        place_name = editGroupSchedule.name
        place_x = editGroupSchedule.x
        place_y = editGroupSchedule.y

        editGroupSchedule.users = moimSchedule.users.map { user -> user.userId }
        selectedIds = moimSchedule.users.map { user -> user.userId } as ArrayList<Long>
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

            val moimService = MoimService()
            moimService.setMoimScheduleView(this)
            if (isPostOrEdit) {
                moimService.postMoimSchedule(postGroupSchedule)
            } else {
                moimService.editMoimSchedule(editGroupSchedule)
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
        binding.dialogGroupScheduleMemberTv.text = group.moimUsers.filter { it.userId in editGroupSchedule.users }.map { it.userName }.joinToString(", ")

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
                selectedMembers = result.data?.getSerializableExtra(GROUP_MEMBER_INTENT_KEY) as MoimListUserList
                setMembers()
            }
        }
    }

    private fun setMembers() {
        var text = ""
        selectedIds.clear()
        for (i in selectedMembers.memberList.indices) {
            if (i == 0) text = selectedMembers.memberList[i].userName
            else text = text + ", " + selectedMembers.memberList[i].userName

            selectedIds.add(selectedMembers.memberList[i].userId)
        }

        binding.dialogGroupScheduleMemberTv.text = text
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

    override fun onAddMoimScheduleSuccess(response: AddMoimScheduleResponse) {
        Log.d("GroupScheduleActivity", "onAddMoimScheduleSuccess : ${response.result}")
        Toast.makeText(this, "모임 일정이 등록되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onAddMoimScheduleFailure(message: String) {
        Log.d("GroupScheduleActivity", "onAddMoimScheduleFailure")
        return
    }

    override fun onEditMoimScheduleSuccess(message: String) {
        Log.d("GroupScheduleActivity", "onEditMoimScheduleSuccess : ${message}")
        Toast.makeText(this, "모임 일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onEditMoimScheduleFailure(message: String) {
        Log.d("GroupScheduleActivity", "onEditMoimScheduleFailure")
        return
    }

    override fun onDeleteMoimScheduleSuccess(message: String) {
        Log.d("GroupScheduleActivity", "onDeleteMoimScheduleSuccess : ${message}")
        Toast.makeText(this, "모임 일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDeleteMoimScheduleFailure(message: String) {
        Log.d("GroupScheduleActivity", "onDeleteMoimScheduleFailure")
        return
    }

    override fun onClickYesButton(id: Int) {
        // 일정 삭제 진행
        val moimService = MoimService()
        moimService.setMoimScheduleView(this)
        moimService.deleteMoimSchedule(editGroupSchedule.moimScheduleId)
    }
}