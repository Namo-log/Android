package com.example.namo.ui.bottom.diary.mainDiary


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.remote.diary.*
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.ui.bottom.diary.mainDiary.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import org.joda.time.DateTime


class DiaryFragment : Fragment() {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var currentTabPosition: Int = 0
    private var currentYearMonth: String = ""

    private lateinit var yearMonth: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        binding.diaryMonth.text = DateTime(dateTime).toString("yyyy.MM")
        yearMonth = binding.diaryMonth.text.toString()

        getDiaryList(yearMonth, currentTabPosition)

        // 그룹 다이어리 테스트, 확인하고 지우기....
        binding.groupdiarytest1.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_groupDiaryFragment)
        }

        binding.groupdiarytest2.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_groupModifyFragment)
        }
        dialogCreate()

        return binding.root
    }


    private fun dialogCreate() {

        binding.diaryMonth.setOnClickListener {

            val year = yearMonth.split(".")[0]
            val month = yearMonth.split(".")[1]

            YearMonthDialog(year.toInt(), month.toInt()) { selectedYearMonth ->
                yearMonth = DateTime(selectedYearMonth).toString("yyyy.MM")
                binding.diaryMonth.text = yearMonth
                if (yearMonth != currentYearMonth) {
                    currentYearMonth = yearMonth
                    getDiaryList(yearMonth, currentTabPosition)
                }
            }.show(parentFragmentManager, "test")
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun getDiaryList(yearMonth: String, tabPosition: Int) {

        val adapter = ViewPagerAdapter(yearMonth,context as MainActivity)
        binding.viewpager2.adapter = adapter
        binding.viewpager2.isUserInputEnabled = false // 슬라이드 못하게

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    currentTabPosition = it // 선택한 탭의 위치를 저장
                    binding.viewpager2.setCurrentItem(it, false)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 선택한 탭 위치로 이동
        binding.viewpager2.post {
            binding.viewpager2.setCurrentItem(tabPosition, false)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}