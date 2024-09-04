package com.mongmong.namo.presentation.ui.community.alert

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentMoimAlertBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.alert.adapter.MoimAlertRVAdapter

class MoimAlertFragment : BaseFragment<FragmentMoimAlertBinding>(R.layout.fragment_moim_alert) {

    private val viewModel: AlertViewModel by activityViewModels()

    private lateinit var moimAdapter: MoimAlertRVAdapter

    override fun setup() {
        binding.apply {
            viewModel = this@MoimAlertFragment.viewModel
            lifecycleOwner = this@MoimAlertFragment
        }

        initObserve()
    }

    private fun setAdapter() {
        moimAdapter = MoimAlertRVAdapter()
        binding.moimAlertListRv.apply {
            adapter = moimAdapter
            layoutManager = LinearLayoutManager(context)
        }
        moimAdapter.setItemClickListener(object : MoimAlertRVAdapter.MyItemClickListener {
            override fun onMoimInfoClick(position: Int) {
                //
            }

            override fun onAcceptBtnClick(position: Int) {
                //TODO: 요청 수락 진행
            }

            override fun onDenyBtnClick(position: Int) {
                //TODO: 요청 거절 진행
            }
        })
    }

    private fun initObserve() {
        viewModel.moimRequestList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setAdapter()
                moimAdapter.addRequest(it)
            }
        }
    }
}