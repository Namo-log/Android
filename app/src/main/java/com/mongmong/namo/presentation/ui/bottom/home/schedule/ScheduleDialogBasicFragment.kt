package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.presentation.ui.MainActivity.Companion.setCategoryList
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.remote.group.EditMoimScheduleView
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.data.remote.group.MoimService
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.databinding.FragmentScheduleDialogBasicBinding
import com.mongmong.namo.presentation.ui.bottom.home.notify.PushNotificationReceiver
import com.mongmong.namo.presentation.ui.bottom.home.schedule.map.MapActivity
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.chip.Chip
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.joda.time.DateTime

@AndroidEntryPoint
class ScheduleDialogBasicFragment : Fragment(), EditMoimScheduleView {

    private lateinit var binding : FragmentScheduleDialogBasicBinding
    private val args : ScheduleDialogBasicFragmentArgs by navArgs()

    private var event : Schedule = Schedule()

    private var isAlarm : Boolean = false

    private var categoryList : List<Category> = arrayListOf()
    private lateinit var selectedCategory : Category

    private var prevClicked : TextView? = null
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())
    private var selectedDate = DateTime(System.currentTimeMillis())

    lateinit var mapView: MapView
    var mapViewContainer: RelativeLayout? = null

    private var place_name : String = "없음"
    private var place_x : Double = 0.0
    private var place_y : Double = 0.0

    private var date = DateTime(System.currentTimeMillis())

    lateinit var db : NamoDatabase

    private lateinit var getResult : ActivityResultLauncher<Intent>

    private var scheduleId : Long = 0

    private var prevChecked : MutableList<Int> = mutableListOf()
    private var alarmList : MutableList<Int> = mutableListOf()
    private var prevAlarmList : List<Int>? = null
    private var alarmText : String = ""

    private val failList = ArrayList<Schedule>()

    private var isMoimScheduleCategorySaved = false
    private var isMoimScheduleAlarmSaved = false
    private var isMoimSchedulePrevAlarm = false

    private val viewModel : ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleDialogBasicBinding.inflate(inflater, container, false)
        db = NamoDatabase.getInstance(requireContext())
        categoryList = setCategoryList(db)

        binding.dialogSchedulePlaceKakaoBtn.visibility = View.GONE
        binding.dialogSchedulePlaceContainer.visibility = View.GONE
        mapViewContainer?.visibility = View.GONE

        initObservers()

        if (args.event != null) {
            event = args.event!!
            prevAlarmList = event.alarmList
            setContent()
        } else {
            binding.dialogScheduleHeaderTv.text = "새 일정"
            val nowDay = args.nowDay
            if (nowDay != 0L) {
                date = DateTime(args.nowDay)
            }
            initPickerText()
            initCategory()
        }

        if (event.scheduleId != 0L) {
            binding.dialogScheduleHeaderTv.text = "일정 편집"
            scheduleId = event.scheduleId
        } else {
            binding.dialogScheduleHeaderTv.text = "새 일정"
        }

        clickListener()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == Activity.RESULT_OK) {
                place_name = it.data?.getStringExtra(MainActivity.PLACE_NAME_INTENT_KEY)!!
                place_x = it.data?.getDoubleExtra(MainActivity.PLACE_X_INTENT_KEY, 0.0)!!
                place_y = it.data?.getDoubleExtra(MainActivity.PLACE_Y_INTENT_KEY, 0.0)!!
                Log.d("PLACE_INFO", "name : $place_name , x : $place_x , y : $place_y")

                event.placeName = place_name
                event.placeX = place_x
                event.placeY = place_y

                initMapView()
                if (place_x != 0.0 || place_y != 0.0) {
                    setMapContent()
                }
                Log.d("PLACE_INTENT", place_name)
            }
        }
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

    override fun onPause() {
        super.onPause()
        mapViewContainer?.removeView(mapView)
        Log.d("OnPause", "Map remove")
    }

    private fun clickListener() {
        //카테고리 클릭
        binding.dialogScheduleCategoryLayout.setOnClickListener {
            hidekeyboard()
            storeContent()

            val action = ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentToScheduleDialogCategoryFragment(event)
            findNavController().navigate(action)
        }

        // picker 클릭
        binding.dialogScheduleStartTimeTp.currentHour = startDateTime.hourOfDay
        binding.dialogScheduleStartTimeTp.currentMinute = startDateTime.minuteOfHour
        binding.dialogScheduleStartTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
            startDateTime = startDateTime.withTime(hourOfDay, minute, 0,0)
            if (startDateTime.millis > endDateTime.millis) {
                endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
                binding.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
            }
            binding.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        }

        binding.dialogScheduleEndTimeTp.currentHour = endDateTime.hourOfDay
        binding.dialogScheduleEndTimeTp.currentMinute = endDateTime.minuteOfHour
        binding.dialogScheduleEndTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
            endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
            if (endDateTime.millis < startDateTime.millis) {
                startDateTime = startDateTime.withTime(hourOfDay, minute, 0, 0)
                binding.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
            }
            binding.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
        }

        binding.dialogScheduleStartDateTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleStartDateTv, binding.dialogScheduleDateLayout)
        }

        binding.dialogScheduleEndDateTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleEndDateTv, binding.dialogScheduleDateLayout)
        }

        binding.dialogScheduleStartTimeTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleStartTimeTv, binding.dialogScheduleStartTimeLayout)
        }

        binding.dialogScheduleEndTimeTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleEndTimeTv, binding.dialogScheduleEndTimeLayout)
        }

        //알람 클릭
        binding.dialogScheduleAlarmLayout.setOnClickListener {
            hidekeyboard()
            if (!isAlarm) binding.dialogScheduleAlarmContentLayout.visibility = View.VISIBLE
            else binding.dialogScheduleAlarmContentLayout.visibility = View.GONE
            isAlarm = !isAlarm
        }

        binding.alarmGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("CHIP_GROUP", "Now : $checkedIds")
            Log.d("CHIP_GROUP", "Prev : $prevChecked")
            if (checkedIds.size == 0) {
                val child : Chip = group.getChildAt(0) as Chip
                child.isChecked = true
                child.isCheckable = false
                alarmText = "없음, "
                prevChecked.clear()
                prevChecked.add(child.id)
            } else if (checkedIds.size > 1 && prevChecked.size == 1 && prevChecked[0] == binding.alarmNone.id) {
                val child : Chip = group.getChildAt(0) as Chip
                child.isCheckable = true
                child.isChecked = false
                Log.d("CHIP_GROUP", "none out")
                prevChecked.remove(child.id)
                alarmText = getChipText(checkedIds[1])
            } else if (checkedIds.size > 0 && checkedIds[0] == binding.alarmNone.id) {
                prevChecked = checkedIds
                for (i in 1 until group.childCount) {
                    val child : Chip = group.getChildAt(i) as Chip
                    child.isChecked = false
                    prevChecked.remove(child.id)
                }
                val none : Chip = group.getChildAt(0) as Chip
                none.isChecked = true
                none.isCheckable = false
                alarmText = "없음, "
                prevChecked.clear()
                prevChecked.add(none.id)
                Log.d("CHIP_GROUP", "others out")
            } else {
                prevChecked = checkedIds
                alarmText = ""
                for (i in checkedIds) {
                    alarmText += getChipText(i)
                }
            }

            if (alarmText.length > 2) {
                alarmText =  alarmText.substring(0, alarmText.length - 2)
            }
            binding.dialogScheduleAlarmTv.text = alarmText
        }

        // 장소 클릭
        binding.dialogSchedulePlaceLayout.setOnClickListener {
            hidekeyboard()
            getLocationPermission()
        }

        binding.dialogSchedulePlaceKakaoBtn.setOnClickListener {
            hidekeyboard()

            val url = "kakaomap://route?sp=&ep=${place_y},${place_x}&by=PUBLICTRANSIT"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        if (event.moimSchedule) {
            isMoimSchedulePrevAlarm = !event.alarmList.isNullOrEmpty()
            binding.dialogScheduleTitleEt.inputType = InputType.TYPE_NULL
            binding.dialogScheduleStartDateTv.setOnClickListener { null }
            binding.dialogScheduleEndDateTv.setOnClickListener { null }
            binding.dialogScheduleStartTimeTv.setOnClickListener { null }
            binding.dialogScheduleEndTimeTv.setOnClickListener { null }
            binding.dialogSchedulePlaceBtn.visibility = View.INVISIBLE
            binding.dialogSchedulePlaceLayout.setOnClickListener { null }
        }

        // 닫기 클릭
        binding.dialogScheduleCloseBtn.setOnClickListener {
            requireActivity().finish()
        }

        // 저장 클릭
        binding.dialogScheduleSaveBtn.setOnClickListener {
            if (binding.dialogScheduleTitleEt.text.toString().isEmpty()) {
                Toast.makeText(context, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            storeContent()

            // 모임 일정일 경우
            if (event.moimSchedule) {
                // 카테고리와 알람 추가/수정
                val moimService = MoimService()
                moimService.setEditMoimScheduleView(this)
                moimService.patchMoimScheduleCategory(
                    PatchMoimScheduleCategoryBody(
                        event.serverId,
                        event.categoryServerId
                    )
                )
                if (isMoimSchedulePrevAlarm) {
                    moimService.patchMoimScheduleAlarm(
                        MoimScheduleAlarmBody(
                            event.serverId,
                            event.alarmList!!
                        )
                    )
                } else {
                    moimService.postMoimScheduleAlarm(
                        MoimScheduleAlarmBody(
                            event.serverId,
                            event.alarmList!!
                        )
                    )
                }
            }
            // 개인일정일 경우
            else {
                if (event.scheduleId == 0L) {
                    lifecycleScope.launch {
                        insertData()
                    }
                } else {
                    // 일정 수정
                    lifecycleScope.launch {
                        updateData()
                    }
                }
            }
        }
    }

    /** 일정 추가 **/
    private fun insertData() {
        // 현재 일정의 상태가 추가 상태임을 나타냄
        event.state = RoomState.ADDED.state
        event.isUpload = UploadState.IS_NOT_UPLOAD.state
        event.serverId = 0

        // 새 일정 등록
        viewModel.addSchedule(event)
    }

    /** 일정 수정 **/
    private fun updateData() {
        // 현재 일정의 상태가 수정 상태임을 나타냄
        event.state = RoomState.EDITED.state
        event.isUpload = UploadState.IS_NOT_UPLOAD.state

        // 이전 알람 삭제 후 변경 알람 저장
        for (i in prevAlarmList!!) {
            deleteNotification(
                event.scheduleId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt()
            )
        }
        setAlarm(event.startLong)

        // 일정 편집
        viewModel.editSchedule(event)

        Toast.makeText(requireContext(), "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        // 뒤로가기
        requireActivity().finish()
    }

    private fun hidekeyboard() {
        val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.dialogScheduleTitleEt.windowToken, 0)
    }


    // Alarm Zone
    private fun setAlarmList() {
        val checkedAlarm = binding.alarmGroup.checkedChipIds
        alarmList.clear()
        for (i in checkedAlarm) {
            when (i) {
                binding.alarmNone.id -> {
                    Log.d("ALARM", "None selected")
                }
                binding.alarmMin60.id -> {
                    alarmList.add(60)
                    Log.d("ALARM", "60 min selected")
                }
                binding.alarmMin30.id -> {
                    alarmList.add(30)
                    Log.d("ALARM", "30 min selected")
                }
                binding.alarmMin10.id -> {
                    alarmList.add(10)
                    Log.d("ALARM", "10 min selected")
                }
                binding.alarmMin5.id -> {
                    alarmList.add(5)
                    Log.d("ALARM", "5 min selected")
                }
                binding.alarmMin0.id -> {
                    alarmList.add(0)
                    Log.d("ALARM", "0 min selected")
                }
            }
        }
    }

    private fun setAlarm(desiredTime: Long) {
        var early = false
        for (i in alarmList) {
            val time = DateTime(desiredTime).minusMinutes(i).millis
            if (time <= System.currentTimeMillis()) {
                early = true
                continue
            }
            val id = scheduleId.toInt() + time.toInt()
            checkNotificationPermission(requireActivity(), time, id)
        }
        if (early) {
            Toast.makeText(context, "현재 시간보다 이른 알림을 제외하고 알림을 등록하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNotificationPermission(activity: Activity, desiredTime: Long, id : Int) {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            Toast.makeText(requireContext(), "설정에서 알림 권한을 허용 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        } else {
            schedulePushNotification(desiredTime, id)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun schedulePushNotification(desiredTimestamp : Long, id : Int) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PushNotificationReceiver::class.java)
        intent.putExtra("notification_id", id)
        intent.putExtra("notification_title", event.title)
        intent.putExtra("notification_content", startDateTime.toString("MM-dd") + " ~ " + endDateTime.toString("MM-dd"))

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("ALARM","setExactAndAllowWhileIdle")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                desiredTimestamp,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, desiredTimestamp, pendingIntent)
            Log.d("ALARM","setExact")
        }
        Log.d("ALARM", "Set puth notification : $id, $desiredTimestamp")
    }

    private fun setAlarmClicked(alarmList : List<Int>) {
        if (alarmList.isEmpty()) {
            binding.alarmNone.isChecked = true
        } else {
            for (i in alarmList) {
                when (i) {
                    60 -> {
                        binding.alarmMin60.isChecked = true
                    }
                    30 -> {
                        binding.alarmMin30.isChecked = true
                    }
                    10 -> {
                        binding.alarmMin10.isChecked = true
                    }
                    5 -> {
                        binding.alarmMin5.isChecked = true
                    }
                    0 -> {
                        binding.alarmMin0.isChecked = true
                    }
                }
            }
        }
    }

    private fun getChipText(id : Int) : String {
        return when (id) {
            binding.alarmMin60.id -> "1시간 전, "
            binding.alarmMin30.id -> "30분 전, "
            binding.alarmMin10.id -> "10분 전, "
            binding.alarmMin5.id -> "5분 전, "
            binding.alarmMin0.id -> "정시, "
            binding.alarmNone.id -> "없음, "
            else -> ""
        }
    }

    private fun deleteNotification(id : Int) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PushNotificationReceiver::class.java)
        intent.putExtra("notification_id", id)
        intent.putExtra("notification_title", event.title)
        intent.putExtra("notification_content", startDateTime.toString("MM-dd") + " ~ " + endDateTime.toString("MM-dd"))

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        alarmManager.cancel(pendingIntent)
    }


    //Location Map Zone
    private fun initMapView() {
        mapView = MapView(context as ScheduleActivity).also {
            mapViewContainer = RelativeLayout(context as ScheduleActivity)
            mapViewContainer?.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            binding.dialogSchedulePlaceContainer.addView(mapViewContainer)
            mapViewContainer?.addView(it)
        }
    }

    private fun setMapContent() {
        binding.dialogSchedulePlaceNameTv.text = place_name
        binding.dialogSchedulePlaceKakaoBtn.visibility = View.VISIBLE
        binding.dialogSchedulePlaceContainer.visibility = ViewGroup.VISIBLE
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
                if (event.placeX != 0.0 && event.placeY != 0.0) {
                    intent.putExtra("PREV_PLACE_NAME", event.placeName)
                    intent.putExtra("PREV_PLACE_X", event.placeX)
                    intent.putExtra("PREV_PLACE_Y", event.placeY)
                }
                getResult.launch(intent)
//                startActivity(intent)

            } catch (e : NullPointerException) {
                Log.e("LOCATION_ERROR", e.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.finishAffinity(requireActivity())
                } else {
                    ActivityCompat.finishAffinity(requireActivity())
                }
            }

        } else {
            Toast.makeText(context, "위치 권한 허용 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        }
    }


    // Content Zone
    fun setContent() {
        //제목
        binding.dialogScheduleTitleEt.setText(event.title)

        //카테고리
        Log.d("TEST_CATEGORY", categoryList.toString())
        Log.d("TEST_CATEGORY", event.toString())
        val category = categoryList.find {
            if (it.serverId != 0L) it.serverId == event.categoryServerId
            else it.categoryId == event.categoryId
        }
        if (category != null) {
            selectedCategory = category
        }
        event.categoryId = selectedCategory.categoryId
        event.categoryServerId = selectedCategory.serverId
        setCategory()

        //시작일, 종료일
        startDateTime = DateTime(event.startLong * 1000L)
        endDateTime = DateTime(event.endLong * 1000L)
        binding.dialogScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        binding.dialogScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))

        //시작 시간, 종료 시간
        binding.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        binding.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))

        //알람
        setAlarmClicked(event.alarmList!!)
        val checkedIds = binding.alarmGroup.checkedChipIds
        prevChecked = checkedIds
        for (i in checkedIds) {
            alarmText += getChipText(i)
        }
        alarmText = alarmText.substring(0, alarmText.length - 2)
        binding.dialogScheduleAlarmTv.text = alarmText

        //장소
        place_name = event.placeName
        place_x = event.placeX
        place_y = event.placeY

        if (event.placeX != 0.0 || event.placeY != 0.0) {
            initMapView()
            setMapContent()
        }
    }

    private fun storeContent() {
        event.title = binding.dialogScheduleTitleEt.text.toString()
        event.startLong = startDateTime.millis / 1000
        event.endLong = endDateTime.millis / 1000
        event.endDate = getInterval(event.startLong, event.endLong)

        setAlarmList()
        event.alarmList = alarmList

        Log.d("STORE_CONTENT", event.toString())
    }


    // Picker Zone
    private val startDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, startDateTime.hourOfDay, startDateTime.minuteOfHour)
        if (selectedDate.isAfter(endDateTime)) {
            endDateTime = selectedDate
            binding.dialogScheduleEndDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
        }
        binding.dialogScheduleStartDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
        startDateTime = selectedDate
    }
    private val endDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, endDateTime.hourOfDay, endDateTime.minuteOfHour)
        if (startDateTime.isAfter(selectedDate)) {
            startDateTime = selectedDate
            binding.dialogScheduleStartDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
        }
        binding.dialogScheduleEndDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
        endDateTime = selectedDate
    }
    private fun initPickerText(){
        startDateTime = DateTime(date.year, date.monthOfYear, date.dayOfMonth, 8, 0, 0, 0)
        endDateTime = DateTime(date.year, date.monthOfYear, date.dayOfMonth, 9, 0, 0, 0)

        binding.dialogScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        binding.dialogScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))
        binding.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        binding.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))

        Log.d("INIT_PICKER_TEXT", startDateTime.toString())
    }

    private fun showPicker(clicked : TextView, pickerLayout : MotionLayout) {
        togglePicker(binding.dialogScheduleStartTimeLayout, false)
        togglePicker(binding.dialogScheduleEndTimeLayout, false)

        binding.dialogScheduleStartDateTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleEndDateTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleStartTimeTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleEndTimeTv.setTextColor(resources.getColor(R.color.textGray))

        if (prevClicked != clicked) {
            togglePicker(pickerLayout, true)
            prevClicked = clicked

            when (clicked) {
                binding.dialogScheduleStartDateTv -> {
                    togglePicker(binding.dialogScheduleDateLayout, true)
                    binding.dialogScheduleDateDp.init(
                        startDateTime.year,
                        startDateTime.monthOfYear - 1,
                        startDateTime.dayOfMonth,
                        startDatePickerListener
                    )
                }
                binding.dialogScheduleEndDateTv -> {
                    togglePicker(binding.dialogScheduleDateLayout, true)
                    binding.dialogScheduleDateDp.init(
                        endDateTime.year,
                        endDateTime.monthOfYear - 1,
                        endDateTime.dayOfMonth,
                        endDatePickerListener
                    )
                }
                else -> {
                    togglePicker(binding.dialogScheduleDateLayout, false)
                }
            }

            clicked.setTextColor(resources.getColor(R.color.MainOrange))

        } else {
            togglePicker(binding.dialogScheduleDateLayout, false)
            prevClicked = null
        }
    }

    private fun togglePicker(pickerLayout: MotionLayout, open : Boolean) {
        val isClosed = pickerLayout.currentState == pickerLayout.startState

        if (isClosed && open) {
            pickerLayout.transitionToEnd()
        } else if (!isClosed && !open) {
            pickerLayout.transitionToStart()
        } else {
            return
        }
    }

    private fun initObservers() {
        viewModel.schedule.observe(requireActivity()) { schedule ->
            binding.dialogScheduleTitleEt.setText(schedule.title)
        }
        viewModel.isPostComplete.observe(requireActivity()) { isComplete ->
            // 추가 작업이 완료된 후 뒤로가기
            if (isComplete) {
                requireActivity().finish()
            }
        }
    }

    // Category Zone
    private fun initCategory() {

        categoryList = setCategoryList(db)
        selectedCategory = categoryList[0]
        event.categoryId = selectedCategory.categoryId
        event.categoryServerId = selectedCategory.serverId

        setCategory()
    }

    private fun setCategory() {
        event.categoryServerId = selectedCategory.serverId
        binding.dialogScheduleCategoryNameTv.text = selectedCategory.name
        binding.dialogScheduleCategoryColorIv.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(selectedCategory.paletteId)
    }

    private fun printNotUploaded() {
        val thread = Thread {
            failList.clear()
            failList.addAll(db.scheduleDao.getNotUploadedSchedule() as ArrayList<Schedule>)
        }
        thread.start()
        try {
            thread.join()
        } catch ( e : InterruptedException) {
            e.printStackTrace()
        }

        Log.d("ScheduleBasic", "Not uploaded Schedule : ${failList}")

        // 화면 이동
        requireActivity().finish()
    }

    override fun onPatchMoimScheduleCategorySuccess(message: String) {
        isMoimScheduleCategorySaved = true
        isMoimScheduleSaved()
    }

    override fun onPatchMoimScheduleCategoryFailure(message: String) {
        isMoimScheduleCategorySaved = false
        Log.d("UPDATE_MOIM_SCHEDULE", message)
        Toast.makeText(context, "카테고리 업데이트에 실패하였습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onPostMoimScheduleAlarmSuccess(message: String) {
        isMoimScheduleAlarmSaved = true
        isMoimScheduleSaved()
    }

    override fun onPostMoimScheduleAlarmFailure(message: String) {
        isMoimScheduleAlarmSaved = false
        Log.d("UPDATE_MOIM_SCHEDULE", message)
        Toast.makeText(context, "알람리스트 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onPatchMoimScheduleAlarmSuccess(message: String) {
        isMoimScheduleAlarmSaved = true
        isMoimScheduleSaved()
    }

    override fun onPatchMoimScheduleAlarmFailure(message: String) {
        isMoimScheduleAlarmSaved = false
        Log.d("UPDATE_MOIM_SCHEDULE", message)
        Toast.makeText(context, "알람리스트 업데이트에 실패하였습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun isMoimScheduleSaved() {
        if (isMoimScheduleCategorySaved && isMoimScheduleAlarmSaved) {
            val thread = Thread {
                db.scheduleDao.updateSchedule(event)
//                Log.d("UPDATE_MOIM_SCHEDULE", db.eventDao.getScheduleById(event.scheduleId).toString())
            }

            thread.start()
            try {
                thread.join()
            } catch (e : InterruptedException) {
                e.printStackTrace()
            }

            requireActivity().finish()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 777
    }
}