package com.example.namo.ui.bottom.group

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.namo.config.BaseResponse
import com.example.namo.data.remote.moim.AddMoimResponse
import com.example.namo.data.remote.moim.DeleteMoimMemberView
import com.example.namo.data.remote.moim.Moim
import com.example.namo.data.remote.moim.MoimService
import com.example.namo.data.remote.moim.UpdateMoimNameBody
import com.example.namo.databinding.ActivityGroupInfoBinding
import com.example.namo.ui.bottom.group.adapter.GroupInfoMemberRVAdapter
import java.security.acl.Group

class GroupInfoActivity : AppCompatActivity(), DeleteMoimMemberView {

    private lateinit var binding : ActivityGroupInfoBinding
    private lateinit var group : Moim

    private lateinit var groupInfoMemberRVAdapter : GroupInfoMemberRVAdapter

    private var groupName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        group = intent.getSerializableExtra("group") as Moim
        setAdapter()
        clickListener()

        setGroupInfo()
        setListenerToEditText()
    }

    private fun setAdapter() {
//        addUsers()

        binding.groupInfoMemberRv.layoutManager = GridLayoutManager(this, 2)
        groupInfoMemberRVAdapter = GroupInfoMemberRVAdapter(group.moimUsers)
        binding.groupInfoMemberRv.adapter = groupInfoMemberRVAdapter
    }

//    private fun addUsers() {
//        val userList : ArrayList<MoimUser> = arrayListOf()
//        userList.apply {
//            add(
//                MoimUser(
//                    0,
//                    "강어진"
//                )
//            )
//            add(
//                MoimUser(
//                    1,
//                    "김나현"
//                )
//            )
//            add(
//                MoimUser(
//                    2,
//                    "박수빈"
//                )
//            )
//            add(
//                MoimUser(
//                    3,
//                    "서은수"
//                )
//            )
//            add(
//                MoimUser(
//                    5,
//                    "김현재"
//                )
//            )
//        }
//
//        group.moimUsers = userList
//    }

    private fun clickListener() {
        binding.groupInfoCloseBtn.setOnClickListener {
            finish()
        }

        binding.groupInfoSaveBtn.setOnClickListener {
            val editGroupName = binding.groupInfoGroupNameContentEt.text.toString()
            // 그룹명이 변경되었으면
            if (groupName != editGroupName) {
                val moimService = MoimService()
                moimService.setDeleteMoimMemberView(this)

                moimService.updateMoimName(UpdateMoimNameBody(group.groupId, editGroupName))
            }
        }

        binding.groupInfoCodeCopyIv.setOnClickListener {
            copyTextToClipboard(binding.groupInfoCodeTv.text.toString())
//            Toast.makeText(this, "그룹 코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.groupInfoLeaveBtn.setOnClickListener {
            val moimService = MoimService()
            moimService.setDeleteMoimMemberView(this)

            moimService.deleteMoimMember(group.groupId)
        }
    }

    private fun setGroupInfo() {
        groupName = group.groupName
        binding.groupInfoGroupNameContentEt.setText(group.groupName)
        binding.groupInfoMemberHeaderContentTv.text = group.moimUsers.size.toString()

        binding.groupInfoCodeTv.isSelected = true
        binding.groupInfoCodeTv.text = group.groupCode


    }

    private fun setListenerToEditText() {
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

    private fun copyTextToClipboard(text : String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)

        clipboardManager.setPrimaryClip(clipData)
    }

    override fun onUpdateMoimNameSuccess(response: AddMoimResponse) {
        Log.d("GroupInfoAct", "onUpdateMoimNameSuccess")
        Toast.makeText(this, "모임 이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onUpdateMoimNameFailure(message: String) {
        Log.d("GroupInfoAct", "onUpdateMoimNameFailure")
        finish()
    }

    override fun onDeleteMoimMemberSuccess(response: BaseResponse) {
        Log.d("GroupInfoAct", "onDeleteMoimMemberSuccess")
        Toast.makeText(this, "${group.groupName} 모임에서 탈퇴하였습니다.", Toast.LENGTH_SHORT).show()

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onDeleteMoimMemberFailure(message: String) {
        Log.d("GroupInfoAct", "onDeleteMoimMemberFailure")
    }
}