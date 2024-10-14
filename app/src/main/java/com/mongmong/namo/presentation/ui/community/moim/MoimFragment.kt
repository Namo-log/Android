package com.mongmong.namo.presentation.ui.community.moim

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentMoimBinding
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.moim.adapter.MoimRVAdapter
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoimFragment : BaseFragment<FragmentMoimBinding>(R.layout.fragment_moim) {

    private val viewModel: MoimViewModel by viewModels()

    private var moimAdapter = MoimRVAdapter()

    private val getResultText = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val edited = result.data?.getBooleanExtra(MOIM_EDIT_KEY, false)
            if (edited == true) viewModel.getMoim() // 변경 사항이 있다면 업데이트
        }
    }

    override fun setup() {
        binding.viewModel = this@MoimFragment.viewModel
        setAdapter()
        initClickListeners()
        initObserve()
    }

    private fun initClickListeners() {
        // + 버튼
        binding.moimCreateFloatingBtn.setOnClickListener {
            // 모임 일정 생성 화면으로 이동
            requireActivity().startActivity(Intent(context, MoimScheduleActivity::class.java)
                .putExtra("moim", Moim())
            )
        }
    }

    private fun setAdapter() {
        binding.moimRv.apply {
            adapter = moimAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        moimAdapter.setItemClickListener(object : MoimRVAdapter.MyItemClickListener {
            override fun onRecordButtonClick(position: Int) {
                //TODO: 모임 기록 화면으로 이동
            }

            override fun onItemClick(position: Int) {
                // 모임 일정 편집 화면으로 이동
                val intent = Intent(context, MoimScheduleActivity::class.java)
                    .putExtra("moimScheduleId", viewModel.moimPreviewList.value!![position].moimId)
                getResultText.launch(intent)
            }
        })
    }

    private fun initObserve() {
        viewModel.moimPreviewList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Log.d("MoimFragment", "moimPreviewListObserve\n$it")
                moimAdapter.addMoim(it)
            }
        }
    }

    companion object {
        const val MOIM_EDIT_KEY = "moim_edit_key"
    }
}