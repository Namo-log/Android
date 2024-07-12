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
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.databinding.ActivityGroupInfoBinding
import com.mongmong.namo.presentation.ui.group.adapter.GroupInfoMemberRVAdapter
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupInfoActivity : AppCompatActivity(), ConfirmDialogInterface {

    private lateinit var binding : ActivityGroupInfoBinding

    private lateinit var groupInfoMemberRVAdapter : GroupInfoMemberRVAdapter

    private val viewModel : GroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setGroup(intent.getSerializableExtra("group") as Group)
        setAdapter()
        setClickListener()
        setGroupInfo()
        setEditTextListener()

        initObserve()
    }

    private fun setAdapter() {
        binding.groupInfoMemberRv.layoutManager = GridLayoutManager(this, 2)
        groupInfoMemberRVAdapter = GroupInfoMemberRVAdapter(viewModel.getGroup().groupMembers)
        binding.groupInfoMemberRv.adapter = groupInfoMemberRVAdapter
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
        // 탈퇴 확인 다이얼로그
        val title = "정말 모임에서 탈퇴하시겠어요?"
        val content = "탈퇴하더라도\n이전 모임 일정은 사라지지 않습니다."

        val dialog = ConfirmDialog(this@GroupInfoActivity, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    private fun setGroupInfo() {
        val group = viewModel.getGroup()
        with(binding) {
            groupInfoGroupNameContentEt.setText(group?.groupName)
            groupInfoMemberHeaderContentTv.text = group?.groupMembers?.size.toString()
            groupInfoCodeTv.isSelected = true
            groupInfoCodeTv.text = group?.groupCode
        }

    }

    private fun setEditTextListener() {
        binding.groupInfoGroupNameContentEt.setOnKeyListener { view, keyCode, event ->
            // Enter Key Action
            if (event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                // 키보드 내리기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.groupInfoGroupNameContentEt.windowToken, 0)
                true
            }

            false
        }
    }

    private fun initObserve() {
        viewModel.updateGroupNameResult.observe(this) {
            if(it.result != 0L) {
                Toast.makeText(this, "모임 이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent(this, GroupCalendarActivity::class.java).apply {
                    putExtra("groupName", binding.groupInfoGroupNameContentEt.text.toString()) // 사용자가 이름을 바꿨음을 알려줌
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
        viewModel.deleteMemberResult.observe(this) {
            if(it == 200) {
                Toast.makeText(this, "${viewModel.getGroup().groupName} 모임에서 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent(this, GroupCalendarActivity::class.java).apply {
                    putExtra("leave", true) // 사용자가 탈퇴했음을 알려줌
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }
    private fun copyTextToClipboard(text : String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)

        clipboardManager.setPrimaryClip(clipData)
    }


    override fun onClickYesButton(id: Int) {
        // 탈퇴 진행
        viewModel.deleteGroupMember()
    }
}