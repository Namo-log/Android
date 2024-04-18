package com.mongmong.namo.presentation.ui.diary


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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.presentation.utils.NetworkManager
import com.mongmong.namo.databinding.FragmentDiaryBinding
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import com.mongmong.namo.presentation.ui.group.diary.MoimMemoDetailActivity
import com.mongmong.namo.presentation.ui.diary.personalDiary.ImageDialog
import com.mongmong.namo.presentation.ui.diary.personalDiary.PersonalDetailActivity
import com.mongmong.namo.presentation.utils.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class DiaryFragment : Fragment() {  // 다이어리 리스트 화면(bottomNavi)

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel : DiaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        setTabLayout()
        setMonthSelector()

        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        initObserve() // 화면이 다시 보일 때 관찰 시작
    }

    override fun onPause() {
        super.onPause()
        removeObserve() // 화면이 사라질 때 관찰 중지
    }

    private fun initObserve() {
        viewModel.isMoim.observe(viewLifecycleOwner, isGroupObserver)
        viewModel.currentDate.observe(viewLifecycleOwner, currentDateObserver)
    }

    private fun removeObserve() {
        // LiveData 관찰 중지
        viewModel.isMoim.removeObserver(isGroupObserver)
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

    private fun setTabLayout() {
        binding.diaryTab.apply {
            addTab(newTab().setText(getString(R.string.diary_personal)))
            addTab(newTab().setText(getString(R.string.diary_group)))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.setIsGroup(tab.position == IS_MOIM)
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
        if (viewModel.getIsGroup() == IS_NOT_MOIM) {
            // 개인 기록 가져오기
            setDiaryList(isMoim = false)
        } else {
            // 모임 기록 가져오기
            setDiaryList(isMoim = true)
        }
    }
    private fun setDiaryList(isMoim: Boolean) {
        if(isMoim && !NetworkManager.checkNetworkState(requireContext())) {
            with(binding) {
                diaryListEmptyTv.visibility = View.VISIBLE
                diaryListEmptyTv.text = getString(R.string.diary_network_failure)
                diaryGroupListRv.visibility = View.GONE
            }
            return
        }

        val adapter = if (!isMoim)
            DiaryAdapter(::onEditClickListener,
                imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })
        else
            DiaryGroupAdapter(::onDetailClickListener,
                imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })

        setupRecyclerView(isMoim, adapter)

        // 데이터 로딩 및 어댑터 데이터 설정
        viewLifecycleOwner.lifecycleScope.launch {
            val pagingDataFlow = if (!isMoim) viewModel.getPersonalPaging(viewModel.getCurrentDate())
            else viewModel.getMoimPaging(viewModel.getCurrentDate().split(".").let { "${it[0]},${it[1].removePrefix("0")}" })

            pagingDataFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // 어댑터의 로드 상태 리스너 설정
        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0) {
                binding.diaryPersonalListRv.visibility = View.GONE
                binding.diaryGroupListRv.visibility = View.GONE
                binding.diaryListEmptyTv.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.diary_empty)
                }
            }
        }
    }

    private fun setupRecyclerView(isMoim: Boolean, adapter: RecyclerView.Adapter<*>) {
        val targetRecyclerView = if (!isMoim) binding.diaryPersonalListRv else binding.diaryGroupListRv
        val hiddenRecyclerView = if (isMoim) binding.diaryPersonalListRv else binding.diaryGroupListRv

        targetRecyclerView.apply {
            visibility = View.VISIBLE
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        hiddenRecyclerView.visibility = View.GONE
    }

    private fun onEditClickListener(item: DiarySchedule) {  // 개인 기록 수정 클릭리스너
        val schedule = Schedule(
            item.scheduleId,
            item.title,
            item.startDate, 0L, 0,
            item.categoryId, item.place,
            0.0, 0.0, 0, null, UploadState.IS_UPLOAD.state,
            RoomState.DEFAULT.state,
            item.serverId,
            item.categoryServerId,
            1
        )

        startActivity(Intent(context, PersonalDetailActivity::class.java)
            .putExtra("schedule", schedule))

    }

    private fun onDetailClickListener(item: DiarySchedule) {  // 그룹 기록 수정 클릭리스너

        val monthDiary = MoimDiary(
            item.scheduleId, item.title, item.startDate, item.content,
            item.images ?: emptyList(), item.categoryId, 0L, item.place
        )

        requireActivity().startActivity(Intent(context, MoimMemoDetailActivity::class.java)
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
        const val IS_MOIM = 1
        const val IS_NOT_MOIM = 0
    }
}

