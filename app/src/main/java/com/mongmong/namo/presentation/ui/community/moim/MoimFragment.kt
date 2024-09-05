package com.mongmong.namo.presentation.ui.community.moim

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentMoimBinding
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.moim.adapter.MoimRVAdapter
import com.mongmong.namo.presentation.ui.group.GroupCalendarActivity
import com.mongmong.namo.presentation.ui.group.schedule.GroupScheduleActivity

class MoimFragment : BaseFragment<FragmentMoimBinding>(R.layout.fragment_moim) {

    private val viewModel: MoimViewModel by viewModels()

    private lateinit var moimAdapter: MoimRVAdapter

    override fun setup() {
        binding.apply {
            viewModel = this@MoimFragment.viewModel
            lifecycleOwner = this@MoimFragment
        }

        initClickListeners()
        initObserve()
    }

    private fun initClickListeners() {
        binding.moimCreateFloatingBtn.setOnClickListener {
            // 모임 일정 생성 화면으로 이동
            requireActivity().startActivity(Intent(context, GroupScheduleActivity::class.java)
                .putExtra("group", Group())
            )
        }
    }

    private fun setAdapter() {
        moimAdapter = MoimRVAdapter()
        binding.moimRv.apply {
            adapter = moimAdapter
            layoutManager = LinearLayoutManager(context)
        }
        moimAdapter.setItemClickListener(object : MoimRVAdapter.MyItemClickListener {
            override fun onRecordButtonClick(position: Int) {
                //TODO: 모임 기록 화면으로 이동
            }

            override fun onItemClick(position: Int) {
                //TODO: 모임 일정 편집 화면으로 이동
            }
        })
    }

    private fun initObserve() {
        viewModel.moimList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setAdapter()
                moimAdapter.addMoim(it)
            }
        }
    }
}