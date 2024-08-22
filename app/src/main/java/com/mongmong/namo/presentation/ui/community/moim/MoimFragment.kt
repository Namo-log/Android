package com.mongmong.namo.presentation.ui.community.moim

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentMoimBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.moim.adapter.MoimRVAdapter

class MoimFragment : BaseFragment<FragmentMoimBinding>(R.layout.fragment_moim) {

    private val viewModel: MoimViewModel by viewModels()

    private lateinit var moimAdapter: MoimRVAdapter

    override fun setup() {
        binding.apply {
            viewModel = this@MoimFragment.viewModel
            lifecycleOwner = this@MoimFragment
        }

        initObserve()
    }

    private fun setAdapter() {
        moimAdapter = MoimRVAdapter()
        binding.groupListRv.apply {
            adapter = moimAdapter
            layoutManager = LinearLayoutManager(context)
        }
        moimAdapter.setItemClickListener(object : MoimRVAdapter.MyItemClickListener {
            override fun onRecordButtonClick(position: Int) {
                //TODO: 모임 기록 화면으로 이동
            }

            override fun onItemClick(position: Int) {
                //TODO: 모임 일정 화면으로 이동
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