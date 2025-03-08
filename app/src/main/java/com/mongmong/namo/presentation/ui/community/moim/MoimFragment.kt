package com.mongmong.namo.presentation.ui.community.moim

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentMoimBinding
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.domain.model.MoimCreateInfo
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.common.ConfirmDialog
import com.mongmong.namo.presentation.ui.common.ConfirmDialog.ConfirmDialogInterface
import com.mongmong.namo.presentation.ui.community.moim.adapter.MoimRVAdapter
import com.mongmong.namo.presentation.ui.community.moim.diary.MoimDiaryDetailActivity
import com.mongmong.namo.presentation.ui.community.moim.schedule.FriendInviteActivity
import com.mongmong.namo.presentation.ui.community.moim.schedule.FriendInviteActivity.Companion.MOIM_INVITE_KEY
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleActivity
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleActivity.Companion.MOIM_ID_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoimFragment : BaseFragment<FragmentMoimBinding>(R.layout.fragment_moim),
    ConfirmDialogInterface {

    private val viewModel: MoimViewModel by viewModels()

    private var moimAdapter = MoimRVAdapter()

    private lateinit var getMoimResultData: ActivityResultLauncher<Intent>

    override fun setup() {
        binding.viewModel = this@MoimFragment.viewModel
        setAdapter()
        initClickListeners()
        initObserve()
        initMoimResultData()
    }

    private fun initClickListeners() {
        // + 버튼
        binding.moimCreateFloatingBtn.setOnClickListener {
            // 모임 일정 생성 화면으로 이동
            val intent = Intent(context, MoimScheduleActivity::class.java)
                .putExtra("moim", Moim())
            getMoimResultData.launch(intent)
        }
    }

    private fun initMoimResultData() {
        // 모임 생성/수정/삭제 후 업데이트 결과 받아오기
        getMoimResultData = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            // 변경사항 확인
            val isEdited = result.data?.getBooleanExtra(MOIM_EDIT_KEY, false)
            if (isEdited == true) viewModel.getMoim() // 변경 사항이 있다면 업데이트

            // 생성 모드인지 확인
            try {
                val createdMoimInfo = result.data?.getSerializableExtra(MOIM_CREATE_KEY) as MoimCreateInfo
                Log.d("MoimFragment", "createdMoimInfo: $createdMoimInfo")
                viewModel.createdMoimId = createdMoimInfo.moimId
                showFriendInviteDialog(createdMoimInfo) // 친구 초대 다이얼로그 띄우기
            } catch (e: Exception) {
                Log.e("MoimFragment", "Error processing MoimCreateInfo: ${e.message}")
            }
        }
    }

    private fun setAdapter() {
        binding.moimRv.apply {
            adapter = moimAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        moimAdapter.setItemClickListener(object : MoimRVAdapter.MyItemClickListener {
            override fun onRecordButtonClick(position: Int) {
                startActivity(
                    Intent(context, MoimDiaryDetailActivity::class.java)
                        .putExtra("scheduleId", viewModel.moimPreviewList.value!![position].moimId)
                )
            }

            override fun onItemClick(position: Int) {
                // 모임 일정 편집 화면으로 이동
                val intent = Intent(context, MoimScheduleActivity::class.java)
                    .putExtra(MOIM_ID_KEY, viewModel.moimPreviewList.value!![position].moimId)
                getMoimResultData.launch(intent)
            }
        })
    }

    private fun showFriendInviteDialog(createdMoimInfo: MoimCreateInfo) {
        val dialog = ConfirmDialog(
            this,
            getString(R.string.dialog_moim_friend_invite_title, createdMoimInfo.title),
            getString(R.string.dialog_moim_friend_invite_content),
            getString(R.string.continuously),
            DIALOG_MOIM_CREATE_ID
        )
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }

    private fun initObserve() {
        viewModel.moimPreviewList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Log.d("MoimFragment", "moimPreviewListObserve\n$it")
                moimAdapter.addMoim(it)
            }
        }
    }

    override fun onClickYesButton(id: Int) {
        // 친구 초대 화면으로 이동
        val intent = Intent(requireActivity(), FriendInviteActivity::class.java)
            .putExtra(MOIM_INVITE_KEY, viewModel.createdMoimId)
        getMoimResultData.launch(intent)
    }

    companion object {
        const val MOIM_EDIT_KEY = "moim_edit_key"
        const val MOIM_CREATE_KEY = "moim_create_key"

        private const val DIALOG_MOIM_CREATE_ID = 0
    }
}