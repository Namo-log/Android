package com.example.namo.ui.bottom.diary

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.ui.bottom.diary.adapter.DiaryListRVAdapter
import org.joda.time.DateTime
import java.lang.Boolean.TRUE
import java.text.ParseException
import java.text.SimpleDateFormat


class DiaryFragment: Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

    private var monthList=listOf<Event>()
    lateinit var diaryAdapter:DiaryListRVAdapter
    private lateinit var yearMonth:String
    private lateinit var day:String
    private lateinit var db: NamoDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        db=NamoDatabase.getInstance(requireContext())

        binding.diaryMonth.text=DateTime(dateTime).toString("yyyy.MM")
        yearMonth=binding.diaryMonth.text.toString()

        getList()

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    fun dateTimeToMillSec(dateTime: String): Long{
        var timeInMilliseconds: Long = 0
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        try {
            val mDate = sdf.parse(dateTime)
            if (mDate != null) {
                timeInMilliseconds = mDate.time
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getList(){
        val r = Runnable {
            try {
                day="$yearMonth.32"

                getDayInMonth()

                val nextMonth=dateTimeToMillSec(day)
                val startMonth=dateTimeToMillSec( "$yearMonth.01")

                Log.d("text","$startMonth,$nextMonth")

                monthList = db.diaryDao.getMonthList(startMonth,nextMonth,TRUE)

                Log.d("monthlist","$monthList")
                diaryAdapter= DiaryListRVAdapter(requireContext(),monthList)
                requireActivity().runOnUiThread {
                    binding.diaryListRv.adapter =  diaryAdapter
                    diaryAdapter.notifyDataSetChanged()
                    binding.diaryListRv.layoutManager = LinearLayoutManager(requireContext())
                    binding.diaryListRv.setHasFixedSize(true)

                    diaryAdapter.setRecordClickListener(object : DiaryListRVAdapter.DiaryEditInterface{
                        override fun onEditClicked(allData: Event) {
                            val bundle=Bundle()
                            bundle.putInt("scheduleIdx",allData.eventId)

                            val diaryFrag=DiaryModifyFragment()
                            diaryFrag.arguments=bundle

                            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_diaryModifyFragment,bundle)
                        }
                    })
                }

            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()

    }

    @SuppressLint("SimpleDateFormat")
    private fun getDayInMonth(){

        val year:String = SimpleDateFormat("yyyy").format(dateTime)

        if(yearMonth=="$year.04" ||yearMonth=="$year.06" ||yearMonth=="$year.09" ||yearMonth=="$year.11")
        { day="$yearMonth.31"}
        if(yearMonth=="$year.02") {
            day = "$yearMonth.29"
            if (year.toInt() % 4 == 0 && year.toInt() % 100 != 0 || year.toInt() % 400 == 0) {
                day = "$yearMonth.30"
            }
        }
    }

    private fun dialogCreate() {

        YearMonthDialog(dateTime){
            yearMonth= DateTime(it).toString("yyyy.MM")
            binding.diaryMonth.text=yearMonth
            getList()
        }.show(parentFragmentManager,"test")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
