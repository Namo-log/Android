package com.mongmong.namo.presentation.ui.diary

import android.content.Intent
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCollectionBinding
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.state.FilterType
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryRVAdapter
import com.mongmong.namo.presentation.ui.community.diary.MoimDiaryDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import java.util.ArrayList

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
            getDiaries()
        }
    }

    override fun setup() {
        binding.viewModel = viewModel
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        getDiaries() // 화면이 다시 보일 때 관찰 시작
    }


    private fun getDiaries() {
        val adapter = DiaryRVAdapter(
            detailClickListener = ::onDetailClickListener,
            participantClickListener = ::onParticipantClickListener,
            imageClickListener = { images ->
                startActivity(
                    Intent(requireActivity(), DiaryImageDetailActivity::class.java)
                        .putStringArrayListExtra(
                            "imgs",
                            images.map { it.imageUrl } as ArrayList<String>
                        )
                )
            }
        )

        setRecyclerView(adapter)
        setDataFlow(adapter)
    }

    private fun setRecyclerView(adapter: RecyclerView.Adapter<*>) {
        binding.diaryCollectionRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
        }
    }

    private fun setDataFlow(adapter: PagingDataAdapter<Diary, RecyclerView.ViewHolder>) {
        lifecycleScope.launch {
            viewModel.getDiaryPaging().collectLatest { pagingData ->
                adapter.submitData(pagingData)
                viewModel.setIsListEmpty(adapter.itemCount == 0)
            }
        }

        adapter.addLoadStateListener { loadState ->
            if (loadState.append is LoadState.Loading) {
                Log.d("Paging", "Loading next page")
            }

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

    private fun onDetailClickListener(item: Diary) {
        Log.d("onPersonalEditClickListener", "${item.scheduleType}")

        startActivity(
            Intent(requireContext(),
                if(item.scheduleType == 0) PersonalDiaryDetailActivity::class.java else MoimDiaryDetailActivity::class.java)
                .putExtra("scheduleId", item.scheduleId)
        )
    }

    private fun onParticipantClickListener(participantsCount: Int, participantNames: String) {
        DiaryParticipantDialog(participantsCount, participantNames).show(parentFragmentManager, "ParticipantDialog")
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


