package com.gradu.presentation.ui.community.moim.schedule

import android.app.Activity
import android.content.Intent
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityFriendInviteBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.gradu.presentation.MainActivity
import com.gradu.presentation.ui.common.ConfirmDialog
import com.gradu.presentation.ui.community.moim.MoimFragment.Companion.MOIM_EDIT_KEY
import com.gradu.presentation.ui.community.moim.schedule.adapter.FriendInvitePreparatoryRVAdapter
import com.gradu.presentation.ui.community.moim.schedule.adapter.FriendInviteRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendInviteActivity : BaseActivity<ActivityFriendInviteBinding>(R.layout.activity_friend_invite),
    ConfirmDialog.ConfirmDialogInterface {

    private val viewModel: FriendInviteViewModel by viewModels()

    private var invitedFriendAdapter = FriendInviteRVAdapter(canEdit = false) // 초대한 친구
    private lateinit var friendToInviteAdapter: FriendInvitePreparatoryRVAdapter // 초대할 친구
    private var allFriendAdapter = FriendInviteRVAdapter(canEdit = true) // 모든 친구

    override fun setup() {
        binding.viewModel = viewModel

        getDataFromScheduleScreen()
        initClickListeners()
        initObserve()
    }

    private fun getDataFromScheduleScreen() {
        // moimScheduleId
        intent.getLongExtra(MOIM_INVITE_KEY, 0L).let { moimScheduleId ->
            viewModel.moimScheduleId = moimScheduleId
        }

        // 이미 초대된 참석자들의 id
        intent.getLongArrayExtra(MOIM_PARTICIPANT_ID_KEY)?.let { memberIds ->
            viewModel.invitedUserIdList = memberIds.toList()
            binding.isInvitedFriendExist = true
        }
    }

    private fun initClickListeners() {
        // 뒤로가기
        binding.friendInviteBackIv.setOnClickListener {
            // 변경사항이 없는 경우
            if (viewModel.friendToInviteList.value.isNullOrEmpty()) {
                finish()
                return@setOnClickListener
            }
            // 변경사항이 있는 경우
            showCustomDialog(
                R.string.dialog_moim_schedule_invite_change_notify_title,
                R.string.dialog_moim_schedule_invite_change_notify_content,
                DIALOG_CHANGE_NOTIFY_ID
            )
        }

        // 전체 선택 취소
        binding.friendInviteResetBtn.setOnClickListener {
            viewModel.resetAllSelectedFriend()
            allFriendAdapter.resetAllSelectedFriend()
        }

        // 초대하기 버튼
        binding.friendInviteBtn.setOnClickListener {
            showCustomDialog(
                R.string.dialog_moim_schedule_invite_complete_title,
                R.string.dialog_moim_schedule_invite_complete_content,
                DIALOG_FRIEND_INVITE_ID
            )
        }
    }

    // 초대한 친구 현황 표시용
    private fun setFriendSelectedNum() {
        val totalCount = viewModel.remainFriendList.value?.size ?: viewModel.allFriendList.value?.size
        // {초대할 친구 수} / {전체 친구 수}
        binding.friendInviteSelectedNumTv.text = Html.fromHtml(
            String.format(resources.getString(R.string.moim_schedule_friend_invite_selected_num),
                viewModel.friendToInviteList.value?.size,
                totalCount)
        )
    }

    // 초대된 친구 어댑터 설정
    private fun setInvitedFriendAdapter() {
        binding.friendInviteInvitedListRv.apply {
            adapter = invitedFriendAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    // 초대할 친구 어댑터 설정
    private fun setFriendInvitePreparatoryAdapter() {
        friendToInviteAdapter = FriendInvitePreparatoryRVAdapter()
        binding.friendInvitePreparatoryRv.apply {
            adapter = friendToInviteAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        friendToInviteAdapter.setItemClickListener(object : FriendInvitePreparatoryRVAdapter.MyItemClickListener {
            override fun onDeleteBtnClick(position: Int) { // 초대할 친구 해제
                val friendToDelete = viewModel.friendToInviteList.value!![position]
                // 초대할 친구 목록에서 삭제
                viewModel.updateSelectedFriend(false, friendToDelete)
                // 모든 친구 목록에서도 체크 해제
                allFriendAdapter.uninvitedFriend(friendToDelete)
            }
        })
    }

    // 모든 친구 어댑터 설정
    private fun setAllFriendAdapter() {
        binding.friendInviteListRv.apply {
            adapter = allFriendAdapter
            layoutManager = LinearLayoutManager(context)
        }

        allFriendAdapter.setItemClickListener(object : FriendInviteRVAdapter.MyItemClickListener {
            override fun onInviteButtonClick(isSelected: Boolean, position: Int) {
                // 초대할 친구 목록에 추가
//                Log.d("FriendInviteACT", "onInviteButtonClick - isSelected: $isSelected, position: $position")
                val friendToInvite = viewModel.remainFriendList.value?.let { it[position] }
                    ?: viewModel.allFriendList.value!![position]
                viewModel.updateSelectedFriend(isSelected, friendToInvite)
            }

            override fun onItemClick(position: Int) {
                //
            }
        })
    }

    private fun initObserve() {
        // 초대할 친구
        viewModel.friendToInviteList.observe(this) {
            Log.d("FriendInviteACT", "friendToInviteList: $it")
            if (it.isNotEmpty()) {
                setFriendInvitePreparatoryAdapter()
                friendToInviteAdapter.addFriend(it)
                setFriendSelectedNum()
            }
        }

        // 모든 친구
        viewModel.allFriendList.observe(this) { friendList ->
            if (friendList.isNotEmpty()) {
                setAllFriendAdapter()
            }
            // 이미 초대된 친구
            viewModel.setInvitedFriend()
            setInvitedFriendAdapter()
            invitedFriendAdapter.addFriend(viewModel.invitedFriendList)
            // 모든 친구
            allFriendAdapter.addFriend(viewModel.remainFriendList.value ?: friendList)
            setFriendSelectedNum()
        }

        // API 호출 성공 여부
        viewModel.friendInviteResult.observe(this) { response ->
            if (!response.isSuccess) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                return@observe
            }

            /* 생성 및 편집 모드에서의 화면 진입 경로 구분 */
            val mainIntent = Intent(this, MainActivity::class.java) // 생성 시
                .putExtra(MOIM_EDIT_KEY, response.isSuccess)
            val scheduleIntent = Intent(this, MoimScheduleActivity::class.java) // 편집 시
                .putExtra(MOIM_EDIT_KEY, response.isSuccess)
            val intents = arrayOf(mainIntent, scheduleIntent)
            // 결과 전달
            intents.forEach {
                setResult(Activity.RESULT_OK, it)
            }
            finish()
        }
    }

    private fun showCustomDialog(title: Int, content: Int, id: Int) {
        val dialog = ConfirmDialog(this@FriendInviteActivity, getString(title), getString(content), null, id)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    override fun onClickYesButton(id: Int) {
        when (id) {
            DIALOG_FRIEND_INVITE_ID -> viewModel.inviteMoimParticipants() // 참석자 초대 진행
            DIALOG_CHANGE_NOTIFY_ID -> finish() // 화면 닫기
        }
    }

    companion object {
        const val MOIM_INVITE_KEY = "moim_invite_key"
        const val MOIM_PARTICIPANT_ID_KEY = "moim_participant_id_key"

        private const val DIALOG_FRIEND_INVITE_ID = 0
        private const val DIALOG_CHANGE_NOTIFY_ID = 1
    }
}