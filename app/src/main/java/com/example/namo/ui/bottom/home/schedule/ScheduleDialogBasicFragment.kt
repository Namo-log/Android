package com.example.namo.ui.bottom.home.schedule

import android.Manifest
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentScheduleDialogBasicBinding
import com.example.namo.ui.bottom.home.notify.PushNotificationReceiver
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.ui.bottom.home.schedule.map.MapActivity
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.chip.Chip
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.joda.time.DateTime

class ScheduleDialogBasicFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogBasicBinding
    private val args : ScheduleDialogBasicFragmentArgs by navArgs()

    var isEdit : Boolean = false
    private var event : Event = Event()
    private var category : Category = Category()

    private var prevView : Int = 0
    private var recentView : Int = 0

    var isAlarm : Boolean = false

    private lateinit var categoryRVAdapter : DialogCategoryRVAdapter
    private var categoryList : List<Category> = arrayListOf()
    private var selectedCategory : Int = 0

    private var prevPicker : MotionLayout? = null
    private var prevClicked : TextView? = null
    private var picker : Int = 0
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())
    private var selectedDate = DateTime(System.currentTimeMillis())
    private var selectedHourStr : String = "00"
    private var selectedMinStr : String = "00"
    private var isAmOrPm : String = "AM"
    private var closeOtherTime : Boolean = false

    lateinit var mapView: MapView
    var mapViewContainer: RelativeLayout? = null

    private var place_name : String = "없음"
    private var place_x : Double = 0.0
    private var place_y : Double = 0.0
    private var place_id : String = ""

    private var date = DateTime(System.currentTimeMillis())

    lateinit var db : NamoDatabase

    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val REQUIRED_PERMISSIONS_PUSH = arrayOf(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 777

    private lateinit var getResult : ActivityResultLauncher<Intent>

    private var selectedAlarm : ArrayList<Int> = arrayListOf()
    private var scheduelIdx : Int = 0

    private var prevChecked : MutableList<Int> = mutableListOf()
    private var alarmList : MutableList<Int> = mutableListOf()
    private var prevAlarmList : List<Int>? = null
    private var alarmText : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleDialogBasicBinding.inflate(inflater, container, false)
        db = NamoDatabase.getInstance(requireContext())

        binding.dialogSchedulePlaceKakaoBtn.visibility = View.GONE
        binding.dialogSchedulePlaceContainer.visibility = View.GONE
        mapViewContainer?.visibility = View.GONE

        if (args.event != null) {
            event = args.event!!
            prevAlarmList = event.alarmList
            setContent()
        } else {
            binding.dialogScheduleHeaderTv.text = "새 일정"
            initPickerText()
            initCategory()
        }

        if (event.eventId != 0L) {
            binding.dialogScheduleHeaderTv.text = "일정 편집"
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
                place_id = it.data?.getStringExtra(MainActivity.PLACE_ID_INTENT_KEY)!!
                Log.d("PLACE_INFO", "name : $place_name , x : $place_x , y : $place_y , id : $place_id")

                event.placeName = place_name
                event.placeX = place_x
                event.placeY = place_y
                event.placeId = place_id

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
            storeContent()

            val action = ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentToScheduleDialogCategoryFragment(event)
            findNavController().navigate(action)
        }

        // picker 클릭
        binding.dialogScheduleStartTimeTp.currentHour = startDateTime.hourOfDay
        binding.dialogScheduleStartTimeTp.currentMinute = startDateTime.minuteOfHour
        binding.dialogScheduleStartTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
            startDateTime = startDateTime.withTime(hourOfDay, minute, 0,0)
            binding.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        }

        binding.dialogScheduleEndTimeTp.currentHour = endDateTime.hourOfDay
        binding.dialogScheduleEndTimeTp.currentMinute = endDateTime.minuteOfHour
        binding.dialogScheduleEndTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
            endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
            binding.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
        }

        binding.dialogScheduleStartDateTv.setOnClickListener {
            showPicker(binding.dialogScheduleStartDateTv, binding.dialogScheduleDateLayout)
        }

        binding.dialogScheduleEndDateTv.setOnClickListener {
            showPicker(binding.dialogScheduleEndDateTv, binding.dialogScheduleDateLayout)
        }

        binding.dialogScheduleStartTimeTv.setOnClickListener {
            showPicker(binding.dialogScheduleStartTimeTv, binding.dialogScheduleStartTimeLayout)
        }

        binding.dialogScheduleEndTimeTv.setOnClickListener {
            showPicker(binding.dialogScheduleEndTimeTv, binding.dialogScheduleEndTimeLayout)
        }

        //알람 클릭
        binding.dialogScheduleAlarmLayout.setOnClickListener {
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
            getLocationPermission()
        }

        binding.dialogSchedulePlaceKakaoBtn.setOnClickListener {
            val url = "kakaomap://route?sp=&ep=${place_y},${place_x}&by=PUBLICTRANSIT"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // 닫기 클릭
        binding.dialogScheduleCloseBtn.setOnClickListener {
            requireActivity().finish()
        }

        // 저장 클릭
        binding.dialogScheduleSaveBtn.setOnClickListener {
            storeContent()

            if (event.eventId == 0L) {
                // 새 일정 등록
                var storeDB = Thread {
                    scheduelIdx = db.eventDao.insertEvent(event).toInt()
                }
                storeDB.start()
                try {
                    storeDB.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }

                setAlarm(event.startLong)
            } else {
                // 일정 편집 저장
                var updateDB : Thread = Thread {
                    db.eventDao.updateEvent(event)
                }
                updateDB.start()
                try {
                    updateDB.join()
                } catch ( e: InterruptedException) {
                    e.printStackTrace()
                }
                Toast.makeText(requireContext(), "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()

                // 이전 알람 삭제 후 변경 알람 저장
                for (i in prevAlarmList!!) {
                    deleteNotification(event.eventId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt())
                }
                setAlarm(event.startLong)
            }

            requireActivity().finish()
        }
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
        for (i in alarmList) {
            val time = DateTime(desiredTime).minusMinutes(i).millis
            val id = scheduelIdx + time.toInt()
            checkNotificationPermission(requireActivity(), time, id)
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
        selectedCategory = event.categoryIdx
        binding.dialogScheduleCategoryNameTv.text = event.categoryName
        binding.dialogScheduleCategoryColorIv.background.setTint(resources.getColor(event.categoryColor))

        //시작일, 종료일
        startDateTime = DateTime(event.startLong)
        endDateTime = DateTime(event.endLong)
        binding.dialogScheduleStartDateTv.text = DateTime(event.startLong).toString(getString(R.string.dateFormat))
        binding.dialogScheduleEndDateTv.text = DateTime(event.endLong).toString(getString(R.string.dateFormat))

        //시작 시간, 종료 시간
        binding.dialogScheduleStartTimeTv.text = DateTime(event.startLong).toString(getString(R.string.timeFormat))
        binding.dialogScheduleEndTimeTv.text = DateTime(event.endLong).toString(getString(R.string.timeFormat))

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
        place_id = event.placeId

        if (event.placeX != 0.0 || event.placeY != 0.0) {
            initMapView()
            setMapContent()
        }
    }

    private fun storeContent() {
        event.title = binding.dialogScheduleTitleEt.text.toString()
        event.startLong = startDateTime.millis
        event.endLong = endDateTime.millis
        event.dayInterval = getInterval(event.startLong, event.endLong)

        setAlarmList()
        event.alarmList = alarmList
    }


    // Picker Zone
    private val startDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, startDateTime.hourOfDay, startDateTime.minuteOfHour)
        binding.dialogScheduleStartDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
        startDateTime = selectedDate
    }
    private val endDatePickerListener = DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, endDateTime.hourOfDay, endDateTime.minuteOfHour)
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


    // Category Zone
    private fun initCategory() {
        // 카테고리가 아무것도 없으면 기본 카테고리 2개 생성 (일정, 모임)
        setInitialCategory()

        val r = Runnable {
            try {
                categoryList = db.categoryDao.getCategoryList()
                category = categoryList[0]
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        val thread = Thread(r)
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
        event.categoryIdx = category.categoryIdx
        event.categoryName = category.name
        event.categoryColor = category.color

        setCategory()
    }

    private fun setInitialCategory() {
        // 리스트에 아무런 카테고리가 없으면 기본 카테고리 설정
        val thread = Thread {
            if (db.categoryDao.getCategoryList().isEmpty()) {
                db.categoryDao.insertCategory(Category(0, "일정", R.color.schedule, true))
                db.categoryDao.insertCategory(Category(0, "그룹", R.color.schedule_group, true))
            }
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setCategory() {
        binding.dialogScheduleCategoryNameTv.text = event.categoryName
        binding.dialogScheduleCategoryColorIv.background.setTint(resources.getColor(event.categoryColor))
    }
}