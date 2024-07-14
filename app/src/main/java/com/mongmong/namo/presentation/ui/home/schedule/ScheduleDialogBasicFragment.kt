package com.mongmong.namo.presentation.ui.home.schedule

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
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
import com.google.android.material.chip.Chip
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

    private var isAlarm : Boolean = false

    private var prevClicked : TextView? = null
    private var selectedDate = DateTime(System.currentTimeMillis())

    private var kakaoMap: KakaoMap? = null
    private lateinit var mapView: MapView

    private var date = DateTime(System.currentTimeMillis())

    private lateinit var getResult : ActivityResultLauncher<Intent>

    private var prevChecked : MutableList<Int> = mutableListOf()
    private var alarmList : MutableList<Int> = mutableListOf()
    private var prevAlarmList : List<Int>? = null
    private var alarmText : String = ""

    private val viewModel : PersonalScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_dialog_basic, container, false)

        if (args.nowDay != 0L) {
            date = DateTime(args.nowDay)
        }

        binding.apply {
            viewModel = this@ScheduleDialogBasicFragment.viewModel
            lifecycleOwner = this@ScheduleDialogBasicFragment
        }

        initObservers()
        initMapView()
        setInit()
        initClickListeners()

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
            // 텍스트 변경
            if (args.schedule?.scheduleId != 0L) {
                binding.dialogScheduleHeaderTv.text = "일정 편집"
            }
            if (args.schedule?.moimSchedule == true) {
                binding.dialogScheduleHeaderTv.text = "모임 일정 편집"
                inactivateMoimScheduleEdit() // 비활성화 처리
            }
        } else {
            binding.dialogScheduleHeaderTv.text = "새 일정"
            viewModel.setSchedule(
                Schedule(
                    startLong = PickerConverter.getDefaultDate(DateTime(args.nowDay), true),
                    endLong = PickerConverter.getDefaultDate(DateTime(args.nowDay), false)
                )
            )
        }
//        viewModel.findCategoryById()
        Log.e("ScheduleDialogFrag", "schedule: ${viewModel.schedule.value}")
    }

    private fun initClickListeners() {
        //카테고리 클릭
        binding.dialogScheduleCategoryLayout.setOnClickListener {
            hidekeyboard()
            storeContent()

            val action = viewModel.schedule.value?.let { schedule ->
                ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentToScheduleDialogCategoryFragment(
                    schedule
                )
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }

        // 알림 클릭
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
                alarmText = alarmText.substring(0, alarmText.length - 2)
            }
            binding.dialogScheduleAlarmTv.text = alarmText
        }

        // 장소 클릭
        binding.dialogSchedulePlaceLayout.setOnClickListener {
            hidekeyboard()
            storeContent()
            getLocationPermission()
        }

        // 길찾기 버튼
        binding.dialogSchedulePlaceKakaoBtn.setOnClickListener {
            hidekeyboard()

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
            storeContent()

            // 모임 일정일 경우
            if (viewModel.isMoimSchedule()) {
                // 카테고리 수정
                editMoimScheduleCategory()
                // 알람 수정
                editMoimScheduleAlert()
                return@setOnClickListener
            }
            // 개인 일정일 경우
            if (viewModel.isCreateMode()) {
                // 일정 생성
                Log.e("ScheduleDialogFrag", "생성 모드")
                insertData()
            } else {
                // 일정 수정
                Log.e("ScheduleDialogFrag", "수정 모드")
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
            hidekeyboard()
            showPicker(binding.dialogScheduleStartDateTv, binding.dialogScheduleDateLayout)
        }
        // 종료일 - 날짜
        binding.dialogScheduleEndDateTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleEndDateTv, binding.dialogScheduleDateLayout)
        }
        // 시작일 - 시간
        binding.dialogScheduleStartTimeTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleStartTimeTv, binding.dialogScheduleStartTimeLayout)
        }
        // 종료일 - 시간
        binding.dialogScheduleEndTimeTv.setOnClickListener {
            hidekeyboard()
            showPicker(binding.dialogScheduleEndTimeTv, binding.dialogScheduleEndTimeLayout)
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

    private fun setTextViewsInactive(vararg textViews: TextView) {
        textViews.forEach {
            // 클릭 비활성화
            it.inputType = InputType.TYPE_NULL
            it.setOnClickListener(null)
            // 색상 비활성화
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.disableTextGray))
        }
    }

    private fun inactivateMoimScheduleEdit() {
        binding.apply {
            setTextViewsInactive(
                dialogScheduleTitleEt,
                dialogScheduleStartDateTv,
                dialogScheduleEndDateTv,
                dialogScheduleStartTimeTv,
                dialogScheduleEndTimeTv,
                dialogSchedulePlaceNameTv
            )
            dialogSchedulePlaceLayout.setOnClickListener { null }
            dialogSchedulePlaceBtn.visibility = View.GONE
        }
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

        // 이전 알람 삭제 후 변경 알람 저장
//        for (i in prevAlarmList!!) {
//            deleteNotification(
//                schedule.scheduleId.toInt() + DateTime(schedule.startLong).minusMinutes(i).millis.toInt()
//            )
//        }
//        setAlarm(schedule.startLong)

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

    /** 모임 일정 알림 수정 */
    private fun editMoimScheduleAlert() {
//        Log.d("alertList", "${schedule.alarmList}")
//        viewModel.editMoimScheduleAlert(schedule.serverId, schedule.alarmList!!)
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
//        var early = false
//        for (i in alarmList) {
//            val time = DateTime(desiredTime).minusMinutes(i).millis
//            if (time <= System.currentTimeMillis()) {
//                early = true
//                continue
//            }
//            val id = scheduleId.toInt() + time.toInt()
//            checkNotificationPermission(requireActivity(), time, id)
//        }
//        if (early) {
//            Toast.makeText(context, "현재 시간보다 이른 알림을 제외하고 알림을 등록하였습니다.", Toast.LENGTH_SHORT).show()
//        }
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
//        val context = requireContext()
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val intent = Intent(context, PushNotificationReceiver::class.java)
//        intent.putExtra("notification_id", id)
////        intent.putExtra("notification_title", schedule.title)
//        intent.putExtra("notification_content", startDateTime.toString("MM-dd") + " ~ " + endDateTime.toString("MM-dd"))
//
//        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
//        } else {
//            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        }
//
//        Log.d("ALARM","setExactAndAllowWhileIdle")
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            desiredTimestamp,
//            pendingIntent
//        )
//        Log.d("ALARM", "Set puth notification : $id, $desiredTimestamp")
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
//        val context = requireContext()
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val intent = Intent(context, PushNotificationReceiver::class.java)
//        intent.putExtra("notification_id", id)
////        intent.putExtra("notification_title", schedule.title)
//        intent.putExtra("notification_content", startDateTime.toString("MM-dd") + " ~ " + endDateTime.toString("MM-dd"))
//
//        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
//        } else {
//            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        }
//
//        alarmManager.cancel(pendingIntent)
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

        // 알람
//        setAlarmClicked(schedule.alarmList!!)
//        val checkedIds = binding.alarmGroup.checkedChipIds
//        prevChecked = checkedIds
//        for (i in checkedIds) {
//            alarmText += getChipText(i)
//        }
//        alarmText = alarmText.substring(0, alarmText.length - 2)
//        binding.dialogScheduleAlarmTv.text = alarmText

        // 장소
        setMapContent()
    }

    private fun storeContent() {
        viewModel.updateTitle(binding.dialogScheduleTitleEt.text.toString())
//        setAlarmList()
//        schedule.alarmList = alarmList
    }


    // Picker Zone
    private val startDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = setSelectedDate(year, monthOfYear, dayOfMonth, viewModel.getDateTime()?.first!!)
        Log.d("ScheduleDialogFrag", "selectedDate(start): $selectedDate")
        viewModel.updateTime(selectedDate, null)
    }
    private val endDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = setSelectedDate(year, monthOfYear, dayOfMonth, viewModel.getDateTime()?.second!!)
        Log.d("ScheduleDialogFrag", "selectedDate(end): $selectedDate")
        viewModel.updateTime(null, selectedDate)
    }

    private fun initPicker(){
        val dateTimePair = viewModel.getDateTime() ?: return
        Log.d("INIT_PICKER_TEXT", "start: ${dateTimePair.first}\nend: ${dateTimePair.second}")
        initTimePicker(dateTimePair.first, dateTimePair.second)
    }

    private fun initTimePicker(startDateTime: DateTime, endDateTime: DateTime) {
        binding.dialogScheduleStartTimeTp.apply { // 시작 시간
            hour = startDateTime.hourOfDay
            minute = startDateTime.minuteOfHour
        }
        binding.dialogScheduleEndTimeTp.apply { // 종료 시간
            hour = endDateTime.hourOfDay
            minute = endDateTime.minuteOfHour
        }
    }

    private fun showPicker(clicked : TextView, pickerLayout : MotionLayout) {
        togglePicker(binding.dialogScheduleStartTimeLayout, false)
        togglePicker(binding.dialogScheduleEndTimeLayout, false)

        binding.dialogScheduleStartDateTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleEndDateTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleStartTimeTv.setTextColor(resources.getColor(R.color.textGray))
        binding.dialogScheduleEndTimeTv.setTextColor(resources.getColor(R.color.textGray))

        val dateTimePair = viewModel.getDateTime() ?: return

        if (prevClicked != clicked) {
            togglePicker(pickerLayout, true)
            prevClicked = clicked

            when (clicked) {
                binding.dialogScheduleStartDateTv -> { // 시작일
                    togglePicker(binding.dialogScheduleDateLayout, true)
                    binding.dialogScheduleDateDp.init(
                        dateTimePair.first.year,
                        dateTimePair.first.monthOfYear - 1,
                        dateTimePair.first.dayOfMonth,
                        startDatePickerListener
                    )
                }
                binding.dialogScheduleEndDateTv -> { // 종료일
                    togglePicker(binding.dialogScheduleDateLayout, true)
                    binding.dialogScheduleDateDp.init(
                        dateTimePair.second.year,
                        dateTimePair.second.monthOfYear - 1,
                        dateTimePair.second.dayOfMonth,
                        endDatePickerListener
                    )
                }
                else -> {
                    togglePicker(binding.dialogScheduleDateLayout, false)
                }
            }

            clicked.setTextColor(resources.getColor(R.color.mainOrange))

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