package com.example.namo.ui.bottom.diary.mainDiary

import DiaryAdapter
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
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryBinding
import org.joda.time.DateTime
import java.lang.Boolean.TRUE
import java.text.ParseException
import java.text.SimpleDateFormat

class DiaryFragment : Fragment(), GetMonthDiaryView {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var dateList = listOf<Long>()
    private lateinit var repo: DiaryRepository

    private lateinit var diaryDateAdapter: DiaryAdapter
    private lateinit var yearMonth: String
    private lateinit var day: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        binding.diaryMonth.text = DateTime(dateTime).toString("yyyy.MM")
        yearMonth = binding.diaryMonth.text.toString()

        val diaryDao = NamoDatabase.getInstance(requireContext()).diaryDao
        val categoryDao = NamoDatabase.getInstance(requireContext()).categoryDao
        val diaryService = DiaryService()

        repo = DiaryRepository(diaryDao, categoryDao, diaryService,requireContext())

        getList()

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        // 그룹 다이어리 테스트, 확인하고 지우기....
        binding.groupdiarytest.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_groupDiaryFragment)
        }


        repo.getMonthDiaryRetrofit(yearMonth)
        diaryService.getMonthDiaryView(this)

        return binding.root
    }

    override fun onGetMonthDiarySuccess(
        code: Int,
        message: String,
        result: List<DiaryResponse.MonthDiaryDto>
    ) {
        Log.d("getMonthDiary",result.toString())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getList() {
        val r = Runnable {
            try {
                getDayInMonth()
                day = "$yearMonth.32"

                val nextMonth = dateTimeToMillSec(day)
                val startMonth = dateTimeToMillSec("$yearMonth.01")

                dateList = repo.getDateList(startMonth, nextMonth, TRUE)
                val diaryItems = repo.getMonthDiaryLocal(startMonth, nextMonth, TRUE)

                diaryDateAdapter = DiaryAdapter(parentFragmentManager)
                diaryDateAdapter.submitList(diaryItems)

                // 수정 버튼 클릭리스너
                diaryDateAdapter.setRecordClickListener(object : DiaryAdapter.DiaryEditInterface {
                    override fun onEditClicked(allData: DiaryItem.Content) {
                        val bundle = Bundle()
                        bundle.putInt("scheduleIdx", allData.eventId.toInt())
                        bundle.putInt("categoryIdx", allData.categoryIdx)

                        val editFrag = DiaryModifyFragment()
                        editFrag.arguments = bundle
                        view?.findNavController()
                            ?.navigate(R.id.action_diaryFragment_to_diaryModifyFragment, bundle)
                    }
                })

                requireActivity().runOnUiThread {

                    // 달 별 메모 없으면 없다고 띄우기
                    if (dateList.isNotEmpty()) {
                        binding.diaryListRv.visibility = View.VISIBLE
                    } else {
                        binding.diaryListRv.visibility = View.GONE
                        binding.diaryListEmptyTv.visibility = View.VISIBLE
                    }

                    binding.diaryListRv.apply {
                        adapter = diaryDateAdapter
                        layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        setHasFixedSize(TRUE)
                        (adapter as DiaryAdapter).notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }
        val thread = Thread(r)
        thread.start()

    }


    /** string 형식 날짜를 long 타입으로 변환 **/
    @SuppressLint("SimpleDateFormat")
    fun dateTimeToMillSec(dateTime: String): Long {
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


    /** 월별 일수 계산 **/
    @SuppressLint("SimpleDateFormat")
    private fun getDayInMonth() {

        val year: String = SimpleDateFormat("yyyy").format(dateTime)

        if (yearMonth == "$year.04" || yearMonth == "$year.06" || yearMonth == "$year.09" || yearMonth == "$year.11") {
            day = "$yearMonth.31"
        }
        if (yearMonth == "$year.02") {
            day = "$yearMonth.29"
            if (year.toInt() % 4 == 0 && year.toInt() % 100 != 0 || year.toInt() % 400 == 0) {
                day = "$yearMonth.30"
            }
        }
    }

    /** 다이얼로그 띄우기 **/
    private fun dialogCreate() {

        YearMonthDialog(dateTime) {
            yearMonth = DateTime(it).toString("yyyy.MM")
            binding.diaryMonth.text = yearMonth
            getList()
        }.show(parentFragmentManager, "test")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}