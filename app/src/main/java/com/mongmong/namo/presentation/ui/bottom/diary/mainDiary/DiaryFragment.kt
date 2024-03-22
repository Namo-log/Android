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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.mongmong.namo.R
import com.mongmong.namo.data.datasource.diary.DiaryGroupPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryPersonalPagingSource
import com.mongmong.namo.data.local.entity.diary.DiaryEvent
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.presentation.utils.NetworkManager
import com.mongmong.namo.databinding.FragmentDiaryBinding
import com.mongmong.namo.domain.model.MonthDiary
import com.mongmong.namo.presentation.config.RoomState
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

        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        initObserve() // 화면이 다시 보일 때 관찰 시작
        getList() // onResume에서 getList 호출
    }

    override fun onPause() {
        super.onPause()
        removeObserve() // 화면이 사라질 때 관찰 중지
    }

    private fun initObserve() {
        viewModel.isGroup.observe(viewLifecycleOwner, isGroupObserver)
        viewModel.currentDate.observe(viewLifecycleOwner, currentDateObserver)
    }

    private fun removeObserve() {
        // LiveData 관찰 중지
        viewModel.isGroup.removeObserver(isGroupObserver)
        viewModel.currentDate.removeObserver(currentDateObserver)
    }

    // 관찰자를 클래스 변수로 선언하여 재사용
    private val isGroupObserver = Observer<Int> { isGroup ->
        binding.diaryTab.apply {
            if(selectedTabPosition != isGroup) { getTabAt(isGroup)?.select() }
        }
    }

    private val currentDateObserver = Observer<String> { date ->
        binding.diaryMonth.text = date
        getList()
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
        // 리사이클러뷰 어댑터 연결
        val diaryPersonalAdapter = DiaryAdapter(
            editClickListener = { onEditClickListener(it) }
            , imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })

        binding.diaryPersonalListRv.visibility = View.VISIBLE
        binding.diaryGroupListRv.visibility = View.GONE

        binding.diaryPersonalListRv.apply {
            adapter = diaryPersonalAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DiaryItemDecoration(context))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPersonalPaging(viewModel.getCurrentDate()).collectLatest { pagingData ->
                diaryPersonalAdapter?.submitData(pagingData)
            }
        }

        diaryPersonalAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.NotLoading && diaryPersonalAdapter.itemCount == 0) {
                // 첫 페이지 로드가 완료되었으나 아이템이 없을 경우
                binding.diaryPersonalListRv.visibility = View.GONE
                binding.diaryListEmptyTv.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.diary_empty)
                }
            } else {
                // 데이터가 있는 경우나 로딩 중인 경우
                binding.diaryPersonalListRv.visibility = View.VISIBLE
            }
        }
    }

    private fun getGroupList() {

        binding.diaryPersonalListRv.visibility = View.GONE
        binding.diaryGroupListRv.visibility = View.VISIBLE

        if (!NetworkManager.checkNetworkState(requireContext())) {
            //인터넷 연결 안 됨
            binding.diaryListEmptyTv.visibility = View.VISIBLE
            binding.diaryListEmptyTv.text = resources.getString(R.string.diary_network_failure)
            binding.diaryGroupListRv.visibility = View.GONE

            return
        }

        // 리사이클러뷰 어댑터 연결
        val diaryGroupAdapter = DiaryGroupAdapter(
            detailClickListener = { onDetailClickListener(it) }
            , imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })

        val yearMonthSplit = viewModel.getCurrentDate().split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"

        binding.diaryGroupListRv.apply {
            adapter = diaryGroupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DiaryItemDecoration(context))
        }

        groupPaging(formatYearMonth, diaryGroupAdapter)
    }

    private fun groupPaging(
        date: String,
        adapter: PagingDataAdapter<DiaryEvent, RecyclerView.ViewHolder>?
    ) {
        // Pager를 통해 페이징 데이터 생성
        pagingDataFlow = Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { DiaryGroupPagingSource(date,binding.diaryGroupListRv,binding.diaryListEmptyTv) }
        ).flow

        // 페이징 데이터 플로우를 수집하여 데이터를 어댑터에 제출
        lifecycleScope.launch {
            pagingDataFlow.collectLatest { pagingData ->
                adapter?.submitData(pagingData)
            }
        }
    }

    private fun onEditClickListener(item: DiaryEvent) {  // 개인 기록 수정 클릭리스너

        val event = Event(
            item.eventId,
            item.event_title,
            item.event_start, 0L, 0,
            item.event_category_idx, item.event_place_name,
            0.0, 0.0, 0, null, IS_UPLOAD,
            RoomState.DEFAULT.state,
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
        const val IS_UPLOAD = true
        const val IS_NOT_UPLOAD = false

    }
}

