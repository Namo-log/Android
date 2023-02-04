package com.example.namo.bottom.home.schedule

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.bottom.home.schedule.adapter.DialogCategoryRVAdapter
import com.example.namo.bottom.home.schedule.data.Category
import com.example.namo.bottom.home.schedule.map.MapActivity
import com.example.namo.databinding.FragmentScheduleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Formatter
import kotlin.math.min

class ScheduleDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentScheduleDialogBinding
    //0 -> basic
    //1 -> category
    private var recentView : Int = 0

    private val categoryRVAdapter : DialogCategoryRVAdapter = DialogCategoryRVAdapter()
    private val categoryList : ArrayList<Category> = arrayListOf()
    private var initCategory : Int = 0
    private var selectedCategory : Int = 0

    private var picker : Int = 0
    private lateinit var startDateTime : DateTime
    private lateinit var endDateTime : DateTime
    private var selectedDate = DateTime(System.currentTimeMillis())
    private var selectedHourStr : String = "00"
    private var selectedMinStr : String = "00"
    private var isAmOrPm : String = "AM"
    private var closeOtherTime : Boolean = false

    private lateinit var date: DateTime

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_dialog, container, false)
        setAdapter()
        setScreen()
        initPickerText()
        clickListener()
        Log.d("LIFECYCLE", "OnCreateView")

        return binding.root
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

    private fun clickListener() {
        binding.dialogScheduleBasicContainer.dialogScheduleCategoryLayout.setOnClickListener {
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
            val intent = Intent(requireActivity(), MapActivity::class.java)
            startActivity(intent)
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
                binding.dialogScheduleHeaderTv.text = "새 일정"
                setCategory()

                binding.dialogScheduleCloseBtn.setOnClickListener {
                    //그냥 닫기
                    dialog?.dismiss()
                }

               binding.dialogScheduleSaveBtn.setOnClickListener {
                   //저장하고 닫기
                    dialog?.dismiss()
                }
            }

            //category
            1 -> {
                binding.dialogScheduleHeaderTv.text = "카테고리"

                binding.dialogScheduleCloseBtn.setOnClickListener {
                    recentView = 0
                    binding.dialogScheduleBasicContainer.root.visibility = View.VISIBLE
                    binding.dialogScheduleCategoryContainer.root.visibility = View.GONE
                    selectedCategory = initCategory
                    categoryRVAdapter.setSelectedPos(initCategory)
                    categoryRVAdapter.notifyDataSetChanged()
                    setScreen()
                }

                binding.dialogScheduleSaveBtn.setOnClickListener {
                    recentView = 0
                    binding.dialogScheduleBasicContainer.root.visibility = View.VISIBLE
                    binding.dialogScheduleCategoryContainer.root.visibility = View.GONE
                    setScreen()
                }
            }
            else -> {

            }
        }
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
        Log.d("LIFECYCLE", "Ondestory")

    }

    fun setDate(date : DateTime) {
        this.date = date
    }

    companion object {
        const val TAG = "scheduleDialogFragment"
    }
}