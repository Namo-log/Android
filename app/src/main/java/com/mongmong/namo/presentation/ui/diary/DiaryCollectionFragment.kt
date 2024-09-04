package com.mongmong.namo.presentation.ui.diary

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCollectionBinding
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.state.FilterType
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class DiaryCollectionFragment: BaseFragment<FragmentDiaryCollectionBinding>(R.layout.fragment_diary_collection) {
    private val viewModel: DiaryViewModel by viewModels()

    private fun initClickListener() {
        binding.diaryCollectionFilter.setOnClickListener {
            DiaryFilterDialog(viewModel.filter.value).apply {
                setOnFilterSelectedListener(object : DiaryFilterDialog.OnFilterSelectedListener {
                    override fun onFilterSelected(filter: FilterType) {
                        viewModel.setFilter(filter)
                    }
                })
            }.show(parentFragmentManager, "FilterDialog")
        }
        binding.diaryCollectionFilterSearchBtn.setOnClickListener {
            // 키워드 검색
        }
    }

    override fun setup() {
        binding.viewModel = viewModel
        getList()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        getList() // 화면이 다시 보일 때 관찰 시작
    }

    private fun getList() {
        Log.d("DiaryFragment", "getList")
        setDiaryList()
    }

    private fun setDiaryList() {
        val adapter = DiaryRVAdapter(
            personalEditClickListener = ::onPersonalEditClickListener,
            moimEditClickListener = ::onMoimEditClickListener ,
            participantClickListener = ::onParticipantClickListener,
            imageClickListener = {
                startActivity(
                    Intent(requireActivity(), DiaryImageDetailActivity::class.java).putExtra("imgs", it as ArrayList<DiaryImage>)
                )
            }
        )

        setRecyclerView(adapter)
        setDataFlow(adapter)
    }

    private fun setRecyclerView(adapter: RecyclerView.Adapter<*>) {
        binding.diaryCollectionRv.apply {
            visibility = View.VISIBLE
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setDataFlow(adapter: PagingDataAdapter<Diary, RecyclerView.ViewHolder>) {
        lifecycleScope.launch {
            val pagingDataFlow = viewModel.getDiaryPaging()

            pagingDataFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                viewModel.setIsListEmpty(adapter.itemCount == 0)
            }
        }

        adapter.addLoadStateListener { loadState ->
            when {
                loadState.refresh is LoadState.Error ->
                    viewModel.setEmptyView(
                        messageResId = R.string.diary_network_failure,
                        imageResId = R.drawable.ic_network_disconnect,
                    )

                loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0 ->
                    viewModel.setEmptyView(
                        messageResId = R.string.diary_empty,
                        imageResId = R.drawable.img_diary_empty,
                    )
                loadState.refresh is LoadState.NotLoading && adapter.itemCount > 0 -> {
                    viewModel.setIsListEmpty(false)
                }
            }
        }
    }

    private fun onPersonalEditClickListener(item: Diary) {
        startActivity(
            Intent(requireContext(), PersonalDetailActivity::class.java)
        )
    }

    private fun onMoimEditClickListener(scheduleId: Long) {
        Log.d("onDetailClickListener", "$scheduleId")
        startActivity(
            Intent(requireContext(), MoimMemoDetailActivity::class.java)
                .putExtra("moimScheduleId", scheduleId)
        )
    }
    private fun onParticipantClickListener() {
        DiaryParticipantDialog().show(parentFragmentManager, "ParticipantDialog")
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


