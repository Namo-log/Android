package com.mongmong.namo.presentation.ui.diary

import android.content.Intent
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.databinding.FragmentDiaryCollectBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.config.FilterState
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryRVAdapter
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class DiaryCollectFragment: BaseFragment<FragmentDiaryCollectBinding>(R.layout.fragment_diary_collect) {
    private val viewModel: DiaryViewModel by viewModels()

    private fun initClickListener() {
        binding.diaryCollectFilter.setOnClickListener {
            FilterDialog(viewModel.filter.value).apply {
                setOnFilterSelectedListener(object : FilterDialog.OnFilterSelectedListener {
                    override fun onFilterSelected(filter: FilterState) {
                        viewModel.setFilter(filter)
                    }
                })
            }.show(parentFragmentManager, "FilterDialog")
        }
        binding.diaryCollectFilterSearchBtn.setOnClickListener {
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
        binding.diaryCollectRv.apply {
            visibility = View.VISIBLE
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setDataFlow(adapter: PagingDataAdapter<DiarySchedule, RecyclerView.ViewHolder>) {
        lifecycleScope.launch {
            val pagingDataFlow = viewModel.getMoimPaging("1900.01.01")

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

    private fun onPersonalEditClickListener(item: DiarySchedule) {
        startActivity(
            Intent(requireContext(), PersonalDetailActivity::class.java)
                .putExtra("schedule", item.convertToSchedule())
                .putExtra("paletteId", item.color)
        )
    }

    private fun onMoimEditClickListener(scheduleId: Long, paletteId: Int) {
        Log.d("onDetailClickListener", "$scheduleId")
        startActivity(
            Intent(requireContext(), MoimMemoDetailActivity::class.java)
                .putExtra("moimScheduleId", scheduleId)
                .putExtra("paletteId", paletteId)
        )
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


