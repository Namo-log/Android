package com.mongmong.namo.presentation.ui.diary


import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityDiaryBinding
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryRVAdapter
import com.mongmong.namo.presentation.utils.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class DiaryActivity : BaseActivity<ActivityDiaryBinding>(R.layout.activity_diary) {
    private val viewModel: DiaryViewModel by viewModels()

    override fun setup() {
        binding.viewModel = viewModel
        getList()
    }

    override fun onResume() {
        super.onResume()
        getList() // 화면이 다시 보일 때 관찰 시작
        initClickListener()
    }

    private fun getList() {
        Log.d("DiaryFragment", "getList")
        setDiaryList()
    }

    private fun initClickListener() {
        binding.diaryFilter.setOnClickListener {
            // 다이얼로그 띄우기
        }
        binding.diaryFilterSearchBtn.setOnClickListener {
            // 키워드 검색
        }
        binding.diaryCalendarBtn.setOnClickListener {
            //startActivity(Intent(this, DiaryCalendarActivity::class.java))
        }
    }

    private fun setDiaryList() {
        val adapter = DiaryRVAdapter(
            personalEditClickListener = ::onPersonalEditClickListener,
            moimEditClickListener = ::onMoimEditClickListener ,
            imageClickListener = {
                startActivity(
                    Intent(this, DiaryImageDetailActivity::class.java).putExtra("imgs", it as ArrayList<DiaryImage>)
                )
            }
        )

        setRecyclerView(adapter)
        setDataFlow(adapter)
    }

    private fun setRecyclerView(adapter: RecyclerView.Adapter<*>) {
        binding.diaryListRv.apply {
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
            Intent(this, PersonalDetailActivity::class.java)
                .putExtra("schedule", item.convertToSchedule())
                .putExtra("paletteId", item.color)
        )
    }

    private fun onMoimEditClickListener(scheduleId: Long, paletteId: Int) {
        Log.d("onDetailClickListener", "$scheduleId")
        startActivity(
            Intent(this, MoimMemoDetailActivity::class.java)
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

