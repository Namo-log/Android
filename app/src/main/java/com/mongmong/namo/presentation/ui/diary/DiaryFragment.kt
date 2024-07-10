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
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.FragmentDiaryBinding
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryAdapter
import com.mongmong.namo.presentation.ui.diary.adapter.MoimDiaryAdapter
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
    private val viewModel: DiaryViewModel by viewModels()

    private var isInitialLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        // 데이터 바인딩
        binding.apply {
            viewModel = this@DiaryFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        setTabLayout()
        setMonthSelector()
        getList()

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
        viewModel.isMoim.observe(viewLifecycleOwner, isMoimObserver)
        viewModel.currentDate.observe(viewLifecycleOwner, currentDateObserver)
    }

    private fun removeObserve() {
        // LiveData 관찰 중지
        viewModel.isMoim.removeObserver(isMoimObserver)
        viewModel.currentDate.removeObserver(currentDateObserver)
    }

    // 관찰자를 클래스 변수로 선언하여 재사용
    private val isMoimObserver = Observer<Int> { isMoim ->
        binding.diaryTab.apply {
            if(selectedTabPosition != isMoim) { getTabAt(isMoim)?.select() }
            if(!isInitialLoad) getList()
        }
    }

    private val currentDateObserver = Observer<String> { date ->
        binding.diaryMonth.text = date
        if(!isInitialLoad) getList()
    }
    private fun setTabLayout() {
        binding.diaryTab.apply {
            addTab(newTab().setText(getString(R.string.diary_personal)))
            addTab(newTab().setText(getString(R.string.diary_group)))
        }
    }

    private fun setMonthSelector() {
        binding.diaryMonthLl.setOnClickListener {
            val currentDateTime =
                convertYearMonthToMillis(viewModel.currentDate.value!!) // 화면에 표시된 텍스트를 밀리초로 받음
            SetMonthDialog(requireContext(), currentDateTime) { selectedYearMonth ->
                viewModel.setCurrentDate(DateTime(selectedYearMonth).toString("yyyy.MM"))
            }.show()
        }
    }

    private fun getList() {
        Log.d("DiaryFragment", "getList")
        isInitialLoad = false
        setDiaryList(viewModel.getIsMoim() == IS_MOIM)
    }

    private fun setDiaryList(isMoim: Boolean) {
        val adapter = if (!isMoim)
            DiaryAdapter(::onPersonalEditClickListener,
                imageClickListener = { ImageDialog(it).show(parentFragmentManager, "test") })
        else
            MoimDiaryAdapter(::onMoimEditClickListener)
            { ImageDialog(it).show(parentFragmentManager, "test") }

        setRecyclerView(isMoim, adapter)
        setDataFlow(isMoim, adapter)
    }

    private fun setRecyclerView(isMoim: Boolean, adapter: RecyclerView.Adapter<*>) {
        val targetRecyclerView = if (!isMoim) binding.diaryPersonalListRv else binding.diaryGroupListRv
        val hiddenRecyclerView = if (isMoim) binding.diaryPersonalListRv else binding.diaryGroupListRv

        targetRecyclerView.apply {
            visibility = View.VISIBLE
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        hiddenRecyclerView.visibility = View.GONE
    }

    private fun setDataFlow(isMoim: Boolean, adapter: PagingDataAdapter<DiarySchedule, RecyclerView.ViewHolder>) {
        // 데이터 로딩 및 어댑터 데이터 설정
        viewLifecycleOwner.lifecycleScope.launch {
            val pagingDataFlow = if (!isMoim) viewModel.getPersonalPaging(viewModel.getFormattedDate())
            else viewModel.getMoimPaging(viewModel.getFormattedDate())

            pagingDataFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                viewModel.setIsListEmpty(adapter.itemCount == 0)
            }
        }

        // 어댑터의 로드 상태 리스너 설정
        adapter.addLoadStateListener { loadState ->
            when {
                loadState.refresh is LoadState.Error && isMoim ->
                    viewModel.setEmptyView(
                        messageResId = R.string.diary_network_failure,
                        imageResId = R.drawable.ic_network_disconnect,
                    )

                loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0 ->
                    viewModel.setEmptyView(
                        messageResId = R.string.diary_empty,
                        imageResId = R.drawable.ic_diary_empty,
                    )
                loadState.refresh is LoadState.NotLoading && adapter.itemCount > 0 -> {
                    viewModel.setIsListEmpty(false)  // 데이터가 있을 경우 isListEmpty를 false로 설정
                }
            }
        }
    }

    private fun onPersonalEditClickListener(item: DiarySchedule) {  // 개인 기록 수정 클릭리스너
        startActivity(
            Intent(context, PersonalDetailActivity::class.java)
                .putExtra("schedule", item.convertToSchedule())
                .putExtra("paletteId", item.color)
        )

    }

    private fun onMoimEditClickListener(scheduleId: Long, paletteId: Int) {  // 모임 메모 수정 클릭리스너
        Log.d("onDetailClickListener", "$scheduleId")
        startActivity(
            Intent(context, MoimMemoDetailActivity::class.java)
                .putExtra("moimScheduleId", scheduleId)
                .putExtra("paletteId", paletteId)
        )
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

