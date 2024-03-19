package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary


import DiaryAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.DiaryEvent
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.data.remote.diary.DiaryRepository
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.presentation.utils.NetworkManager
import com.mongmong.namo.databinding.FragmentDiaryBinding
import com.mongmong.namo.domain.model.MonthDiary
import com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.adapter.DiaryGroupAdapter
import com.mongmong.namo.presentation.ui.bottom.home.calendar.SetMonthDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


class DiaryFragment : Fragment() {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis // 현재 연월 밀리초
    private var currentYearMonth: String = ""

    private lateinit var sf: SharedPreferences
    private lateinit var repo: DiaryRepository
    private lateinit var diaryGroupAdapter: DiaryGroupAdapter

    private var yearMonthTextView: String = ""
    private var isMoim = false
    private lateinit var pagingDataFlow: Flow<PagingData<DiaryEvent>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        repo = DiaryRepository(requireContext())

        yearMonthTextView = DateTime(dateTime).toString("yyyy.MM")
        sf = requireContext().getSharedPreferences("sf", Context.MODE_PRIVATE)

        val savedYearMonth = sf.getString("yearMonth", yearMonthTextView)  // 다른 화면으로 넘어가도 텍스트 유지
        if (savedYearMonth != null) {
            yearMonthTextView = savedYearMonth
        }
        val savedChecked = sf.getBoolean("checked", false)
        isMoim = savedChecked

        getList()

        binding.diaryMonth.text = yearMonthTextView

        dialogCreate()

        initSelectorTabView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        getList()
    }

    override fun onStop() {
        super.onStop()

        val editor = sf.edit()
        editor.putBoolean("checked", isMoim)
        editor.apply()
    }


    private fun dialogCreate() {

        binding.diaryMonthLl.setOnClickListener {

            dateTime = convertYearMonthToMillis(yearMonthTextView) // 화면에 표시된 텍스트를 밀리초로 받음
            SetMonthDialog(requireContext(), dateTime) { selectedYearMonth ->
                yearMonthTextView = DateTime(selectedYearMonth).toString("yyyy.MM")
                binding.diaryMonth.text = yearMonthTextView

                if (yearMonthTextView != currentYearMonth) {
                    currentYearMonth = yearMonthTextView
                }
                val editor = sf.edit()
                editor.putString("yearMonth", yearMonthTextView)
                editor.apply()

                getList()
            }.show()
        }
    }

    private fun convertYearMonthToMillis(
        yearMonthStr: String,
        pattern: String = "yyyy.MM"
    ): Long {  // yyyy.MM 타입을 밀리초로 변경

        val formatter = DateTimeFormat.forPattern(pattern)
        val dateTime = formatter.parseDateTime(yearMonthStr)
        return dateTime.toDate().time

    }

    private fun initSelectorTabView() {
        val tabLayout = binding.diaryTab
        val tabTitleList = listOf(R.string.diary_personal, R.string.diary_group)

        for (position in tabTitleList.indices) {
            val tab = tabLayout.newTab()
            tab.text = resources.getString(tabTitleList[position])
            tabLayout.addTab(tab)
        }

        // 탭 선택 리스너를 설정하십시오 (필요에 따라).
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // 개인 탭이 선택된 경우
                        isMoim = false
                        getList()
                    }
                    1 -> {
                        // 모임 탭이 선택된 경우
                        isMoim = true
                        getList()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // 선택 해제된 탭에 대한 동작을 정의
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 이미 선택된 탭을 다시 선택한 경우의 동작을 정의
            }
        })
    }

    private fun getList() {
        // 기록 리스트 가져오기
        if (!isMoim) {
            // 개인 기록 가져오기
            getPersonalList()
        } else {
            // 모임 기록 가져오기
            getGroupList()
        }
    }

    private fun getPersonalList() {

        binding.diaryPersonalListRv.visibility = View.VISIBLE
        binding.diaryGroupListRv.visibility = View.GONE

        val diaryPersonalAdapter = DiaryAdapter(editClickListener = {
            onEditClickListener(it)
        }, imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })
        binding.diaryPersonalListRv.apply {
            adapter = diaryPersonalAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        paging(yearMonthTextView, true, diaryPersonalAdapter, null)

    }

    private fun getGroupList() {

        binding.diaryPersonalListRv.visibility = View.GONE
        binding.diaryGroupListRv.visibility = View.VISIBLE

        diaryGroupAdapter = DiaryGroupAdapter(detailClickListener = { item -> // 리사이클러뷰 어댑터 연결
            onDetailClickListener(item)
        }, imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })

        val yearMonthSplit = yearMonthTextView.split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"

        paging(formatYearMonth, false, null, diaryGroupAdapter)

        binding.diaryGroupListRv.apply {
            adapter = diaryGroupAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        if (!NetworkManager.checkNetworkState(requireContext())) {
            //인터넷 연결 안 됨
            binding.diaryListEmptyTv.visibility = View.VISIBLE
            binding.diaryListEmptyTv.text = resources.getString(R.string.diary_network_failure)
            binding.diaryGroupListRv.visibility = View.GONE

            return
        }
    }

    private fun paging(
        month: String,
        isPersonal: Boolean,
        personalAdapter: DiaryAdapter?,
        groupAdapter: DiaryGroupAdapter?
    ) {
        val diaryPersonalPagingSource = DiaryPersonalPagingSource(month, requireContext(),binding.diaryPersonalListRv,binding.diaryListEmptyTv)
        val diaryGroupPagingSource = DiaryGroupPagingSource(month,binding.diaryGroupListRv,binding.diaryListEmptyTv)

        val diaryPagingSource = if (isPersonal) {
            diaryPersonalPagingSource
        } else {
            diaryGroupPagingSource
        }

        val adapterToSubmit = if (isPersonal) {
            personalAdapter
        } else {
            groupAdapter
        }

        val pagingConfig = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false // placeholders 사용 여부
        )

        // Pager를 통해 페이징 데이터 생성
        pagingDataFlow = Pager(
            config = pagingConfig,
            pagingSourceFactory = { diaryPagingSource }
        ).flow

        // 페이징 데이터 플로우를 수집하여 데이터를 어댑터에 제출
        viewLifecycleOwner.lifecycleScope.launch {
            pagingDataFlow.collectLatest { pagingData ->
                adapterToSubmit?.submitData(pagingData)
            }
        }
    }

    private fun onEditClickListener(item: DiaryEvent) {  // 개인 기록 수정 클릭리스너

        val event = Event(
            item.eventId,
            item.event_title,
            item.event_start, 0L, 0,
            item.event_category_idx, item.event_place_name,
            0.0, 0.0, 0, null, 1,
            R.string.event_current_default.toString(),
            item.event_server_idx,
            item.event_category_server_idx,
            1
        )

        startActivity(Intent(context, PersonalDetailActivity::class.java)
            .putExtra("event", event))

    }

    private fun onDetailClickListener(item: DiaryEvent) {  // 그룹 기록 수정 클릭리스너

        val monthDiary = item.images?.let {
            MonthDiary(
                item.eventId, item.event_title, item.event_start, item.content,
                it, item.event_category_idx, 0L, item.event_place_name
            )
        }

        val intent = Intent(context, GroupDetailActivity::class.java)
        intent.putExtra("groupDiary", monthDiary)
        requireActivity().startActivity(intent)

    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}

