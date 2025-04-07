package com.gradu.presentation.ui.home.diary

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryArchiveBinding
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.enums.FilterType
import com.gradu.presentation.ui.home.diary.adapter.DiaryRVAdapter
import com.gradu.presentation.ui.community.moim.diary.MoimDiaryDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class DiaryArchiveFragment: BaseFragment<FragmentDiaryArchiveBinding>(R.layout.fragment_diary_archive) {
    private val viewModel: DiaryViewModel by viewModels()

    override fun setup() {
        binding.viewModel = viewModel
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        getDiaries() // 화면이 다시 보일 때 관찰 시작
    }

    private fun initClickListener() {
        binding.diaryArchiveFilter.setOnClickListener {
            DiaryFilterDialog(viewModel.filter.value).apply {
                setOnFilterSelectedListener(object : DiaryFilterDialog.OnFilterSelectedListener {
                    override fun onFilterSelected(filter: FilterType) {
                        viewModel.setFilter(filter)
                        if (filter == FilterType.NONE) handleNullFilter()
                    }
                })
            }.show(parentFragmentManager, "FilterDialog")
        }

        binding.diaryArchiveFilterSearchBtn.setOnClickListener {
            if (viewModel.filter.value == FilterType.NONE) {
                // 필터가 NONE일 경우 토스트 메시지 표시
                Toast.makeText(requireContext(), "필터를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                getDiaries()
            }
        }
    }


    fun handleNullFilter() {
        viewModel.clearKeyword()
        getDiaries()
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
        binding.diaryArchiveRv.apply {
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
}
