package com.example.namo.ui.bottom.home.schedule

import android.Manifest
import android.app.*
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.data.entity.home.Category
import com.example.namo.ui.bottom.home.schedule.map.MapActivity
import com.example.namo.databinding.FragmentScheduleDialogBinding
import com.example.namo.ui.bottom.home.category.CategoryActivity
import com.example.namo.ui.bottom.home.notify.PushNotificationReceiver
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import org.joda.time.DateTime

class ScheduleDialogFragment (
    private val okCallback : (Boolean) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentScheduleDialogBinding
    //0 -> basic
    //1 -> category
    //2 -> look
    private var prevView : Int = 0
    private var recentView : Int = 0

    var isEdit : Boolean = false
    var isAlarm : Boolean = false

    private lateinit var categoryRVAdapter : DialogCategoryRVAdapter
    private var categoryList : List<Category> = arrayListOf()
    private var initCategory : Int = 0
    private var selectedCategory : Int = 0

    private var picker : Int = 0
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())
    private var selectedDate = DateTime(System.currentTimeMillis())
    private var selectedHourStr : String = "00"
    private var selectedMinStr : String = "00"
    private var isAmOrPm : String = "AM"
    private var closeOtherTime : Boolean = false

    private var place_name : String = ""

    private var date = DateTime(System.currentTimeMillis())

    private var event : Event = Event()
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
    private var alarmText : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_dialog, container, false)
        db = NamoDatabase.getInstance(requireContext())


        if (isEdit) {
            recentView = 3
        }

        categoryRVAdapter = DialogCategoryRVAdapter(requireContext(), categoryList)

        Log.d("LIFECYCLE", "OnCreateView")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK) {
                place_name = it.data?.getStringExtra(PLACE_NAME_INTENT_KEY)!!
                binding.dialogScheduleBasicContainer.dialogSchedulePlaceNameTv.text = place_name
                Log.d("PLACE_INTENT", place_name)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        Log.d("LIFECYCLE", "OnCreateDialog")
        dialog.setOnShowListener{ dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        layoutParams.height = getBottomSheetDialogHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBottomSheetDialogHeight() : Int {
        return getWindowHeight() * 90 / 100
    }

    private fun getWindowHeight() : Int {
        val displayMetrix : DisplayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrix)
        return displayMetrix.heightPixels
    }

    private fun clickListener( setListener : Boolean) {
        if (setListener) {
            binding.dialogScheduleBasicContainer.dialogScheduleCategoryLayout.setOnClickListener {
                prevView = recentView
                recentView = 1
                binding.dialogScheduleBasicContainer.root.visibility = View.GONE
                binding.dialogScheduleCategoryContainer.root.visibility = View.VISIBLE
                setScreen()
            }

            binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.setOnClickListener {
                setPicker(1)
            }
            binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.setOnClickListener {
                setPicker(2)
            }

            binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.setOnClickListener {
                setPicker(3)
            }
            binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.setOnClickListener {
                setPicker(4)
            }

            binding.dialogScheduleBasicContainer.dialogScheduleAlarmLayout.setOnClickListener {
                if (!isAlarm) binding.dialogScheduleBasicContainer.dialogScheduleAlarmContentLayout.visibility = View.VISIBLE
                else binding.dialogScheduleBasicContainer.dialogScheduleAlarmContentLayout.visibility = View.GONE
                isAlarm = !isAlarm
            }

            binding.dialogScheduleBasicContainer.dialogSchedulePlaceLayout.setOnClickListener {
                getLocationPermission()
            }

            binding.dialogScheduleBasicContainer.alarmGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                Log.d("CHIP_GROUP", "Now : $checkedIds")
                Log.d("CHIP_GROUP", "Prev : $prevChecked")
                if (checkedIds.size == 0) {
                    val child : Chip = group.getChildAt(0) as Chip
                    child.isChecked = true
                    child.isCheckable = false
                    alarmText = "없음, "
                    prevChecked.clear()
                    prevChecked.add(child.id)
                } else if (checkedIds.size > 1 && prevChecked.size == 1 && prevChecked[0] == binding.dialogScheduleBasicContainer.alarmNone.id) {
                    val child : Chip = group.getChildAt(0) as Chip
                    child.isCheckable = true
                    child.isChecked = false
                    Log.d("CHIP_GROUP", "none out")
                    prevChecked.remove(child.id)
                    alarmText = getChipText(checkedIds[1])
                } else if (checkedIds.size > 0 && checkedIds[0] == binding.dialogScheduleBasicContainer.alarmNone.id) {
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
                binding.dialogScheduleBasicContainer.dialogScheduleAlarmTv.text = alarmText
            }
//
//            binding.dialogScheduleBasicContainer.alarmGroup.setOnCheckedChangeListener { group, checkedId ->
//                Log.d("CHIP_GROUP", checkedId.toString())
//                if (checkedId == binding.dialogScheduleBasicContainer.alarmNone.id) {
//                    for (i in 0 until group.childCount) {
//                        val child : View = group.getChildAt(i)
//                        if (child is Chip && child.id != checkedId) {
//                            child.isChecked = false
//                        }
//                    }
//                }
//            }

        }
        else {
            binding.dialogScheduleBasicContainer.dialogScheduleCategoryLayout.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleAlarmLayout.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogSchedulePlaceLayout.setOnClickListener(null)
        }
    }

    private fun getChipText(id : Int) : String {
        return when (id) {
            binding.dialogScheduleBasicContainer.alarmMin60.id -> "1시간 전, "
            binding.dialogScheduleBasicContainer.alarmMin30.id -> "30분 전, "
            binding.dialogScheduleBasicContainer.alarmMin10.id -> "10분 전, "
            binding.dialogScheduleBasicContainer.alarmMin5.id -> "5분 전, "
            binding.dialogScheduleBasicContainer.alarmMin0.id -> "정시, "
            binding.dialogScheduleBasicContainer.alarmNone.id -> "없음, "
            else -> ""
        }
    }

    private fun setAlarmList() {
        val checkedAlarm = binding.dialogScheduleBasicContainer.alarmGroup.checkedChipIds
        alarmList.clear()
        for (i in checkedAlarm) {
            when (i) {
                binding.dialogScheduleBasicContainer.alarmNone.id -> {
                    Log.d("ALARM", "None selected")
                }
                binding.dialogScheduleBasicContainer.alarmMin60.id -> {
                    alarmList.add(60)
                    Log.d("ALARM", "60 min selected")
                }
                binding.dialogScheduleBasicContainer.alarmMin30.id -> {
                    alarmList.add(30)
                    Log.d("ALARM", "30 min selected")
                }
                binding.dialogScheduleBasicContainer.alarmMin10.id -> {
                    alarmList.add(10)
                    Log.d("ALARM", "10 min selected")
                }
                binding.dialogScheduleBasicContainer.alarmMin5.id -> {
                    alarmList.add(5)
                    Log.d("ALARM", "5 min selected")
                }
                binding.dialogScheduleBasicContainer.alarmMin0.id -> {
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

    private fun initPickerText(){
        startDateTime = DateTime(date.year, date.monthOfYear, date.dayOfMonth, 8, 0, 0, 0)
        endDateTime = DateTime(date.year, date.monthOfYear, date.dayOfMonth, 9, 0, 0, 0)

        binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))
        binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))

        Log.d("INIT_PICKER_TEXT", startDateTime.toString())
    }

    private fun setPicker(selected : Int) {
        //아무것도 안 열려 있을 때
        if (picker == 0) {
            setPickerColor(true, selected)
            openPicker(true, selected)
        }
        //이전에 클릭했던 것과 똑같은 걸 클릭했을 때
        else if (picker == selected) {
            setPickerColor(false, selected)
            openPicker(false, selected)
        }
        else {
            setPickerColor(true, selected)
            closeOtherTime = (picker == 3 && selected == 4) || (picker == 4 && selected == 3)
            openPicker(true, selected)
        }
    }

    private fun openPicker(nowOpen : Boolean, selected: Int) {
        if (nowOpen) {
            if (selected == 1 || selected == 2) {
                if (picker == 3) {
                    binding.dialogScheduleBasicContainer.dialogScheduleStartTimeLayout.transitionToStart()
                }
                else if (picker == 4) {
                    binding.dialogScheduleBasicContainer.dialogScheduleEndTimeLayout.transitionToStart()
                }
                binding.dialogScheduleBasicContainer.dialogScheduleDateLayout.transitionToEnd()
//                binding.dialogScheduleBasicContainer.dialogScheduleDateDp.visibility = View.VISIBLE
//                binding.dialogScheduleBasicContainer.dialogScheduleTimeTp.visibility = View.GONE

                if (selected == 1) {
                    Log.d("OPEN_PICKER_START", startDateTime.toString())
                    binding.dialogScheduleBasicContainer.dialogScheduleDateDp.init(startDateTime.year, startDateTime.monthOfYear - 1, startDateTime.dayOfMonth) {
                            view, year, monthOfYear, dayOfMonth ->
                        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, startDateTime.hourOfDay, startDateTime.minuteOfHour)
                        binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
                        startDateTime = selectedDate
                    }
                }
                else {
                    binding.dialogScheduleBasicContainer.dialogScheduleDateDp.init(endDateTime.year, endDateTime.monthOfYear - 1, endDateTime.dayOfMonth) {
                            view, year, monthOfYear, dayOfMonth ->
                        selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, endDateTime.hourOfDay, endDateTime.minuteOfHour)
                        binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.text = selectedDate.toString(getString(R.string.dateFormat))
                        endDateTime = selectedDate
                    }
                }
            }
            else {
//                binding.dialogScheduleBasicContainer.dialogScheduleDateDp.visibility = View.GONE
//                binding.dialogScheduleBasicContainer.dialogScheduleTimeTp.visibility = View.VISIBLE
                if (selected == 3) {
                    if (picker == 4) {
                        binding.dialogScheduleBasicContainer.dialogScheduleEndTimeLayout.transitionToStart()
                    }
                    else {
                        binding.dialogScheduleBasicContainer.dialogScheduleDateLayout.transitionToStart()
                    }
                    binding.dialogScheduleBasicContainer.dialogScheduleStartTimeLayout.transitionToEnd()

                    binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTp.currentHour = startDateTime.hourOfDay
                    binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTp.currentMinute = startDateTime.minuteOfHour
                    binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
                        startDateTime = startDateTime.withTime(hourOfDay, minute, 0,0)
                        binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
                    }
                }
                else {
                    if (picker == 3) {
                        binding.dialogScheduleBasicContainer.dialogScheduleStartTimeLayout.transitionToStart()
                    }
                    else {
                        binding.dialogScheduleBasicContainer.dialogScheduleDateLayout.transitionToStart()
                    }
                    binding.dialogScheduleBasicContainer.dialogScheduleEndTimeLayout.transitionToEnd()

                    binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTp.currentHour = endDateTime.hourOfDay
                    binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTp.currentMinute = endDateTime.minuteOfHour
                    binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTp.setOnTimeChangedListener { view, hourOfDay, minute ->
                        endDateTime = endDateTime.withTime(hourOfDay, minute, 0, 0)
                        binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
                    }
                }
            }
            picker = selected
        }
        else {
            if (selected == 1 || selected == 2) {
//                binding.dialogScheduleBasicContainer.dialogScheduleDateDp.visibility = View.GONE
                binding.dialogScheduleBasicContainer.dialogScheduleDateLayout.transitionToStart()

            }
            else {
                if (selected == 3) binding.dialogScheduleBasicContainer.dialogScheduleStartTimeLayout.transitionToStart()
                else binding.dialogScheduleBasicContainer.dialogScheduleEndTimeLayout.transitionToStart()
//                binding.dialogScheduleBasicContainer.dialogScheduleTimeTp.visibility = View.GONE
            }
            picker = 0
        }
    }

    private fun setPickerColor(nowColor : Boolean, selected: Int) {
        var pickerView : TextView  = when(selected) {
            1 -> binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv
            2 -> binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv
            3 -> binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv
            4 -> binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv
            else -> binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv
        }

        if (nowColor) {
            pickerView.setTextColor(resources.getColor(R.color.MainOrange))
            var prevView : TextView = when(picker) {
                1 -> binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv
                2 -> binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv
                3 -> binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv
                4 -> binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv
                else -> return
            }
            prevView.setTextColor(resources.getColor(R.color.textGray))
        }
        else {
            pickerView.setTextColor(resources.getColor(R.color.textGray))
        }
    }

    private fun setScreen() {
        when(recentView) {
            //basic
            0-> {
                binding.deleteBtn.visibility = View.GONE

                binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.visibility = View.VISIBLE
                binding.dialogScheduleBasicContainer.dialogScheduleTitleTv.visibility = View.GONE

                binding.dialogScheduleCloseBtn.visibility = View.VISIBLE
                binding.dialogScheduleSaveBtn.visibility = View.VISIBLE

                if (selectedCategory == 0) {
                    selectedCategory = categoryList[0].categoryIdx
                }
                setCategory()

                binding.dialogScheduleHeaderTv.text = "새 일정"

                initPickerText()
//                setCategory()
                clickListener(true)

                binding.dialogScheduleCloseBtn.setOnClickListener {
                    //그냥 닫기
                    okCallback(false)
                    dismiss()
//                    dialog?.dismiss()
                }

                binding.dialogScheduleSaveBtn.setOnClickListener {
                    //저장하고 닫기
                    event.title = binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.text.toString()
                    event.startLong = startDateTime.millis
                    event.endLong = endDateTime.millis
                    event.dayInterval = getInterval(event.startLong, event.endLong)
                    event.place = place_name

                    setAlarmList()
                    event.alarmList = alarmList

                    var storeDB : Thread = Thread {
                        scheduelIdx = db.eventDao.insertEvent(event).toInt()
                    }
                    storeDB.start()
                    try {
                        storeDB.join()
                    } catch ( e: InterruptedException) {
                        e.printStackTrace()
                    }

                    setAlarm(event.startLong)


                    okCallback(true)
                    dismiss()
//                    dialog?.dismiss()
                }
            }

            //category
            1 -> {
                storeTemp()

                binding.dialogScheduleHeaderTv.text = "카테고리"

                binding.dialogScheduleCloseBtn.visibility = View.GONE
                binding.dialogScheduleSaveBtn.visibility = View.GONE

                // 카테고리 편집 화면 클릭
                binding.dialogScheduleCategoryContainer.scheduleDialogCategoryEditCv.setOnClickListener {
                    startActivity(Intent(activity, CategoryActivity::class.java))
                }
            }
            //edit
            3 -> {
                binding.deleteBtn.visibility = View.VISIBLE
                binding.deleteBtn.setOnClickListener {
                    //일정 삭제하고 닫기
                    alarmList = event.alarmList!!.toMutableList()
                    for (i in alarmList) {
                        deleteNotification(event.eventId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt())
                    }

                    var deleteDB : Thread = Thread {
                        db.eventDao.deleteEvent(event)
                    }
                    deleteDB.start()
                    try {
                        deleteDB.join()
                    } catch ( e: InterruptedException) {
                        e.printStackTrace()
                    }

                    Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                    okCallback(true)
                    dismiss()
                }

                setAlarmClicked(event.alarmList!!)
                val checkedIds = binding.dialogScheduleBasicContainer.alarmGroup.checkedChipIds
                prevChecked = checkedIds
                for (i in checkedIds) {
                    alarmText += getChipText(i)
                }
                alarmText = alarmText.substring(0, alarmText.length - 2)
                binding.dialogScheduleBasicContainer.dialogScheduleAlarmTv.text = alarmText

                clickListener(true)
                binding.dialogScheduleHeaderTv.text = "내 일정"
                selectedCategory = event.categoryIdx
                setCategory()

                binding.dialogScheduleCloseBtn.visibility = View.VISIBLE
                binding.dialogScheduleSaveBtn.visibility = View.VISIBLE

                binding.dialogScheduleHeaderTv.text = "일정 편집"
                binding.dialogScheduleSaveBtn.text = "저장"

                binding.dialogScheduleCloseBtn.setOnClickListener {
                    okCallback(false)
                    dismiss()
                }

                binding.dialogScheduleSaveBtn.setOnClickListener {
                    //일정 업데이트하고 닫기
                    //저장하고 닫기
                    scheduelIdx = event.eventId.toInt()

                    event.title = binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.text.toString()
                    event.startLong = startDateTime.millis
                    event.endLong = endDateTime.millis
                    event.dayInterval = getInterval(event.startLong, event.endLong)
                    // 카테고리는 카테고리 선택할 때 바로 event에 집어넣음
                    event.place = place_name
                    Log.d("CategoryEvent", "event = $event")

                    val prevAlarmList = event.alarmList
                    setAlarmList()
                    if (prevAlarmList != alarmList) {
                        event.alarmList = alarmList
                        for (i in prevAlarmList!!) {
                            deleteNotification(event.eventId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt())
                        }
                        setAlarm(event.startLong)
                    }

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

                    okCallback(true)
                    dismiss()
                }

                binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.visibility = View.VISIBLE
                binding.dialogScheduleBasicContainer.dialogScheduleTitleTv.visibility = View.GONE
                binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.setText(event.title)

                categoryRVAdapter.setSelectedPos(selectedCategory)
//                categoryRVAdapter.notifyDataSetChanged()

                selectedCategory = event.categoryIdx
                setCategory()

                startDateTime = DateTime(event.startLong)
                endDateTime = DateTime(event.endLong)

                binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.text = DateTime(event.startLong).toString(getString(R.string.dateFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.text = DateTime(event.endLong).toString(getString(R.string.dateFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.text = DateTime(event.startLong).toString(getString(R.string.timeFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.text = DateTime(event.endLong).toString(getString(R.string.timeFormat))

                binding.dialogScheduleBasicContainer.dialogSchedulePlaceNameTv.text = event.place

            }
            else -> {

            }
        }
    }

    private fun setAlarmClicked(alarmaList : List<Int>) {
        if (alarmaList.isEmpty()) {
            binding.dialogScheduleBasicContainer.alarmNone.isChecked = true
        } else {
            for (i in alarmaList) {
                when (i) {
                    60 -> {
                        binding.dialogScheduleBasicContainer.alarmMin60.isChecked = true
                    }
                    30 -> {
                        binding.dialogScheduleBasicContainer.alarmMin30.isChecked = true
                    }
                    10 -> {
                        binding.dialogScheduleBasicContainer.alarmMin10.isChecked = true
                    }
                    5 -> {
                        binding.dialogScheduleBasicContainer.alarmMin5.isChecked = true
                    }
                    0 -> {
                        binding.dialogScheduleBasicContainer.alarmMin0.isChecked = true
                    }
                }
            }
        }
    }

    private fun storeTemp() {
        event.title = binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.text.toString()
        event.startLong = startDateTime.millis
        event.endLong = endDateTime.millis
        event.place = place_name
    }

    private fun setCategory() {
        val thread = Thread {
            val category = db.categoryDao.getCategoryContent(selectedCategory)
            Log.d("SET_CATEGORY", category.toString())
            event.categoryIdx = selectedCategory
            event.categoryName = category.name
            event.categoryColor = category.color

            requireActivity().runOnUiThread {
                binding.dialogScheduleBasicContainer.dialogScheduleCategoryNameTv.text = category.name
                binding.dialogScheduleBasicContainer.dialogScheduleCategoryColorIv.background.setTint(resources.getColor(category.color))
            }

            Log.d("CATEGORY_COLOR", "idx : ${selectedCategory}, name : ${category.name}")
            categoryRVAdapter.setSelectedPos(getSelectedCategoryPos())
        }
        thread.start()
        try {
            thread.join()
        }catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun getCategoryList() {
        // 카테고리가 아무것도 없으면 기본 카테고리 2개 생성 (일정, 모임)
        setInitialCategory()

        val rv = binding.dialogScheduleCategoryContainer.scheduleDialogCategoryRv

        val r = Runnable {
            try {

                // 활성화 상태의 카테고리만 보여줌
                categoryList = db.categoryDao.getActiveCategoryList(true)
                categoryRVAdapter = DialogCategoryRVAdapter(requireContext(), categoryList)
                categoryRVAdapter.setMyItemClickListener(object: DialogCategoryRVAdapter.MyItemClickListener {
                    // 아이템 클릭
                    override fun onSendPos(selected: Int, category: Category) {
                        // 카테고리 세팅
                        selectedCategory = category.categoryIdx
                        Log.d("CATEGORY_CLICK", selectedCategory.toString())
                        setCategory()

                        recentView = prevView
                        binding.dialogScheduleBasicContainer.root.visibility = View.VISIBLE
                        binding.dialogScheduleCategoryContainer.root.visibility = View.GONE
                        setScreen()
                    }
                })
                requireActivity().runOnUiThread {
                    rv.adapter = categoryRVAdapter
                    rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                }
//                Log.d("ScheduleDialogFrag", "categoryDao: ${db.categoryDao.getActiveCategoryList(true)}")
            } catch (e: Exception) {
                Log.d("schedule category", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
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

    private fun getSelectedCategoryPos(): Int {
        var selectedItemPos = 0

        for (i: Int in 0 until categoryList.size) {
            if (categoryList[i].categoryIdx == selectedCategory) {
                selectedItemPos = i
                //Log.e("selectedItemPos", selectedItemPos.toString())
            }
        }
        return selectedItemPos
    }


    override fun onResume() {
        super.onResume()
        getCategoryList()
        setScreen()

        Log.d("LIFECYCLE","OnResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE", "OnStop")

    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFECYCLE", "OnPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedCategory = initCategory
        isEdit = false

        Log.d("LIFECYCLE", "OnDestroy")

    }

    fun setDate(date : DateTime) {
        this.date = date
    }

    fun setEvent(event : Event) {
        this.event = event
    }

    companion object {
        const val TAG = "scheduleDialogFragment"
    }
}