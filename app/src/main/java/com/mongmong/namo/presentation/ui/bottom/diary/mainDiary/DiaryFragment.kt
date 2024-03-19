package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary


import DiaryAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mongmong.namo.R
import com.mongmong.namo.data.datasource.diary.DiaryGroupPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryPersonalPagingSource
import com.mongmong.namo.data.local.entity.diary.DiaryEvent
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.presentation.utils.NetworkManager
import com.mongmong.namo.databinding.FragmentDiaryBinding
import com.mongmong.namo.domain.model.MonthDiary
import com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.adapter.DiaryGroupAdapter
import com.mongmong.namo.presentation.ui.bottom.home.calendar.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class DiaryFragment : Fragment() {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel : DiaryViewModel by viewModels()

    private lateinit var diaryGroupAdapter: DiaryGroupAdapter

    private lateinit var pagingDataFlow: Flow<PagingData<DiaryEvent>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        setDiaryList()
        setTabLayout()
        setMonthSelector()
        initObserve()

        return binding.root
    }


    private fun setDiaryList() {

    }
    private fun setTabLayout() {
        binding.diaryTab.apply {
            addTab(newTab().setText(getString(R.string.diary_personal)))
            addTab(newTab().setText(getString(R.string.diary_group)))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.setIsGroup(tab.position == IS_GROUP)
                    getList()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }
    private fun setMonthSelector() {
        binding.diaryMonth.text = viewModel.getCurrentDate()
        binding.diaryMonthLl.setOnClickListener {
            val currentDateTime = convertYearMonthToMillis(viewModel.getCurrentDate()!!) // 화면에 표시된 텍스트를 밀리초로 받음
            SetMonthDialog(requireContext(), currentDateTime) { selectedYearMonth ->
                viewModel.setCurrentDate(DateTime(selectedYearMonth).toString("yyyy.MM"))
            }.show()
        }
    }

    private fun initObserve() {
        viewModel.isGroup.observe(viewLifecycleOwner) { isGroup ->
            binding.diaryTab.apply {
                if(selectedTabPosition != isGroup) { getTabAt(isGroup)?.select() }
            }
        }
        viewModel.currentDate.observe(viewLifecycleOwner) { date ->
            binding.diaryMonth.text = date
            getList()
        }
    }
    private fun getList() {
        Log.d("DiaryFragment", "getList")
        if (viewModel.getIsGroup() == IS_NOT_GROUP) {
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

        val diaryPersonalAdapter = DiaryAdapter(
            editClickListener = { onEditClickListener(it) }
            , imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })
        binding.diaryPersonalListRv.apply {
            adapter = diaryPersonalAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        paging(viewModel.getCurrentDate(), true, diaryPersonalAdapter, null)

    }

    private fun getGroupList() {

        binding.diaryPersonalListRv.visibility = View.GONE
        binding.diaryGroupListRv.visibility = View.VISIBLE

        diaryGroupAdapter = DiaryGroupAdapter(detailClickListener = { item -> // 리사이클러뷰 어댑터 연결
            onDetailClickListener(item)
        }, imageClickListener = {
            ImageDialog(it).show(parentFragmentManager, "test")
        })

        val yearMonthSplit = viewModel.getCurrentDate().split(".")
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
        val diaryPersonalPagingSource = DiaryPersonalPagingSource(month)
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
        lifecycleScope.launch {
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

        val monthDiary = MonthDiary(
            item.eventId, item.event_title, item.event_start, item.content,
            item.images ?: emptyList(), item.event_category_idx, 0L, item.event_place_name
        )

        requireActivity().startActivity(Intent(context, GroupDetailActivity::class.java)
                .putExtra("groupDiary", monthDiary))

    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    // yyyy.MM 타입을 밀리초로 변경
    private fun convertYearMonthToMillis(
        yearMonthStr: String,
        pattern: String = "yyyy.MM"
    ): Long = DateTimeFormat.forPattern(pattern).parseDateTime(yearMonthStr).toDate().time

    companion object {
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
    }
}

