package com.example.namo.ui.bottom.home.schedule

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.MainActivity.Companion.PLACE_NAME_INTENT_KEY
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.ui.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.ui.bottom.home.schedule.data.Category
import com.example.namo.ui.bottom.home.schedule.map.MapActivity
import com.example.namo.databinding.FragmentScheduleDialogBinding
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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

    private val categoryRVAdapter : DialogCategoryRVAdapter = DialogCategoryRVAdapter()
    private val categoryList : ArrayList<Category> = arrayListOf()
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

    private lateinit var getResult : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_dialog, container, false)

        db = NamoDatabase.getInstance(requireContext())

        if (isEdit) {
            recentView = 2
        }

        setAdapter()
        setScreen()
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

            binding.dialogScheduleBasicContainer.dialogSchedulePlaceLayout.setOnClickListener {
                getLocationPermission()
            }
        }
        else {
            binding.dialogScheduleBasicContainer.dialogScheduleCategoryLayout.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.setOnClickListener(null)
            binding.dialogScheduleBasicContainer.dialogSchedulePlaceLayout.setOnClickListener(null)
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

                binding.dialogScheduleHeaderTv.text = "새 일정"

                initPickerText()
                setCategory()
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
                   event.categoryColor = categoryList[selectedCategory].color
                   event.categoryName = categoryList[selectedCategory].name
                   event.categoryIdx = selectedCategory
                   event.place = place_name

                   var storeDB : Thread = Thread {
                       db.eventDao.insertEvent(event)
                   }
                   storeDB.start()
                   try {
                       storeDB.join()
                   } catch ( e: InterruptedException) {
                       e.printStackTrace()
                   }

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
            }

            //see
            2 -> {
                binding.deleteBtn.visibility = View.VISIBLE
                binding.deleteBtn.setOnClickListener {
                    //일정 삭제하고 닫기

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

                clickListener(false)
                binding.dialogScheduleHeaderTv.text = "내 일정"

                binding.dialogScheduleCloseBtn.visibility = View.VISIBLE
                binding.dialogScheduleSaveBtn.visibility = View.VISIBLE
                binding.dialogScheduleSaveBtn.text = "편집"

                binding.dialogScheduleCloseBtn.setOnClickListener {
                    okCallback(false)
                    dismiss()
                }

                binding.dialogScheduleSaveBtn.setOnClickListener {
                    recentView = 3
                    setScreen()
                }

                binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.visibility = View.GONE
                binding.dialogScheduleBasicContainer.dialogScheduleTitleTv.visibility = View.VISIBLE
                binding.dialogScheduleBasicContainer.dialogScheduleTitleTv.text = event.title

                binding.dialogScheduleBasicContainer.dialogScheduleCategoryNameTv.text = event.categoryName
                binding.dialogScheduleBasicContainer.dialogScheduleCategoryColorIv.background.setTint(resources.getColor(event.categoryColor))
                selectedCategory = event.categoryIdx


                startDateTime = DateTime(event.startLong)
                endDateTime = DateTime(event.endLong)

                binding.dialogScheduleBasicContainer.dialogScheduleStartDateTv.text = DateTime(event.startLong).toString(getString(R.string.dateFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleEndDateTv.text = DateTime(event.endLong).toString(getString(R.string.dateFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleStartTimeTv.text = DateTime(event.startLong).toString(getString(R.string.timeFormat))
                binding.dialogScheduleBasicContainer.dialogScheduleEndTimeTv.text = DateTime(event.endLong).toString(getString(R.string.timeFormat))

                binding.dialogScheduleBasicContainer.dialogSchedulePlaceNameTv.text = event.place
            }

            //edit
            3 -> {
                binding.deleteBtn.visibility = View.GONE

                clickListener(true)
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
                    event.title = binding.dialogScheduleBasicContainer.dialogScheduleTitleEt.text.toString()
                    event.startLong = startDateTime.millis
                    event.endLong = endDateTime.millis
                    event.dayInterval = getInterval(event.startLong, event.endLong)
                    event.categoryColor = categoryList[selectedCategory].color
                    event.categoryName = categoryList[selectedCategory].name
                    event.categoryIdx = selectedCategory
                    event.place = place_name

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
                categoryRVAdapter.notifyDataSetChanged()

            }
            else -> {

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
        binding.dialogScheduleBasicContainer.dialogScheduleCategoryNameTv.text = categoryList[selectedCategory].name
        binding.dialogScheduleBasicContainer.dialogScheduleCategoryColorIv.background.setTint(resources.getColor(categoryList[selectedCategory].color))

        Log.d("CATEGORY_COLOR", selectedCategory.toString())
    }

    private fun setAdapter() {
        binding.dialogScheduleCategoryContainer.scheduleDialogCategoryRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.dialogScheduleCategoryContainer.scheduleDialogCategoryRv.adapter = categoryRVAdapter
        Log.d("CATEGORY_BEFORE", categoryList.toString())
        getCategoryList()
        Log.d("CATEGORY_AFTER", categoryList.toString())
        categoryRVAdapter.addCategory(categoryList)
        initCategory = 0
        categoryRVAdapter.setSelectedPos(initCategory)
        categoryRVAdapter.notifyDataSetChanged()


        categoryRVAdapter.setMyItemClickListener( object  : DialogCategoryRVAdapter.MyItemClickListener {
            override fun onSendPos(selected: Int) {
                selectedCategory = selected

                recentView = prevView
                binding.dialogScheduleBasicContainer.root.visibility = View.VISIBLE
                binding.dialogScheduleCategoryContainer.root.visibility = View.GONE
                setScreen()
            }
        })
    }

    private fun getCategoryList() {
        categoryList.clear()
        categoryList.apply {
            add(
                Category(
                    "카테고리1",
                    R.color.palette1,
                    false
                )
            )
            add(
                Category(
                    "카테고리2",
                    R.color.palette2,
                    false
                )
            )
            add(
                Category(
                    "카테고리3",
                    R.color.palette3,
                    false
                )
            )
            add(
                Category(
                    "카테고리4",
                    R.color.palette4,
                    false
                )
            )
            add(
                Category(
                    "카테고리5",
                    R.color.palette5,
                    false
                )
            )
        }
    }


    override fun onResume() {
        super.onResume()
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

        Log.d("LIFECYCLE", "Ondestory")

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