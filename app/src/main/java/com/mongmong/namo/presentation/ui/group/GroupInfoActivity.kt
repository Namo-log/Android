package com.mongmong.namo.presentation.ui.group

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.databinding.ActivityGroupInfoBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.group.adapter.GroupInfoMemberRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupInfoActivity : BaseActivity<ActivityGroupInfoBinding>(R.layout.activity_group_info), ConfirmDialogInterface {
    private val viewModel: GroupViewModel by viewModels()

    override fun setup() {
        binding.apply {
            // 데이터바인딩 뷰모델 초기화
            viewModel = this@GroupInfoActivity.viewModel

            // marquee focus
            groupInfoCodeTv.requestFocus()
            groupInfoCodeTv.isSelected = true
        }

        viewModel.setGroup(intent.getSerializableExtra("group") as Group)

        setAdapter()
        setClickListener()
        setEditTextListener()
        initObserve()
    }

    private fun setAdapter() {
        val groupMembers = viewModel.groupInfo.value?.groupMembers ?: emptyList()
        val groupInfoMemberRVAdapter = GroupInfoMemberRVAdapter(groupMembers)
        binding.groupInfoMemberRv.apply {
            layoutManager = GridLayoutManager(this@GroupInfoActivity, 2)
            adapter = groupInfoMemberRVAdapter
        }
    }

    private fun setClickListener() {
        binding.groupInfoCloseBtn.setOnClickListener { finish() }
        binding.groupInfoSaveBtn.setOnClickListener {
            viewModel.updateGroupName(binding.groupInfoGroupNameContentEt.text.toString())
        }
        binding.groupInfoCodeCopyIv.setOnClickListener {
            copyTextToClipboard(binding.groupInfoCodeTv.text.toString())
            Toast.makeText(this, "그룹 코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
        binding.groupInfoLeaveBtn.setOnClickListener { showLeaveDialog() }
    }

    private fun showLeaveDialog() {
        val title = "정말 그룹에서 탈퇴하시겠어요?"
        val content = "탈퇴하더라도\n이전 모임 일정은 사라지지 않습니다."

        val dialog = ConfirmDialog(this, title, content, "확인", 0)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    private fun setEditTextListener() {
        binding.groupInfoGroupNameContentEt.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.groupInfoGroupNameContentEt.windowToken, 0)
                true
            }
            false
        }
    }

    private fun initObserve() {
        viewModel.updateGroupNameResult.observe(this) {
            if (it.result != 0L) {
                Toast.makeText(this, "그룹 이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().apply {
                    putExtra("groupName", binding.groupInfoGroupNameContentEt.text.toString())
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        viewModel.deleteMemberResult.observe(this) {
            if (it == 200) {
                Toast.makeText(this, "${viewModel.groupInfo.value?.groupName} 그룹에서 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().apply {
                    putExtra("leave", true)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun copyTextToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun onClickYesButton(id: Int) {
        viewModel.deleteGroupMember()
    }
}
