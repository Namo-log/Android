package com.mongmong.namo.presentation.ui.group

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.presentation.ui.MainActivity.Companion.GROUP_MEMBER_INTENT_KEY
import com.mongmong.namo.domain.model.MoimListUserList
import com.mongmong.namo.databinding.ActivityGroupScheduleMemberBinding
import com.mongmong.namo.presentation.ui.group.adapter.GroupScheduleMemberRVAdapter

class GroupScheduleMemberActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGroupScheduleMemberBinding
    private lateinit var members : MoimListUserList
    private lateinit var selectedPosition : ArrayList<Boolean>
    private lateinit var groupScheduleMemberRVAdapter : GroupScheduleMemberRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupScheduleMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        members = intent.getSerializableExtra("members") as MoimListUserList
        val selected = intent.getLongArrayExtra("selectedIds")
        val selectedIdsList = selected?.toMutableList() ?: ArrayList() // Long 배열을 ArrayList<Long>로 변환

        Log.d("PUT_INTENT", "Result : " + members.toString())
        Log.d("PUT_INTENT", "Result : " + selected.toString())

        selectedPosition = ArrayList<Boolean>(members.memberList.size)
        for (i in members.memberList.indices) {
            // 선택된 멤버 목록에 해당 멤버의 ID가 포함되어 있는지 확인
            val isSelected = selectedIdsList.contains(members.memberList[i].userId)
            Log.d("PUT_INTENT", "Member contains : " + (members.memberList[i].userId))
            selectedPosition.add(isSelected)
            Log.d("PUT_INTENT", "Selected Position : " + selectedPosition.toString())
        }

        setAdapter()
        clickListener()
    }

    private fun setAdapter() {
        groupScheduleMemberRVAdapter = GroupScheduleMemberRVAdapter(this, members.memberList)
        groupScheduleMemberRVAdapter.setSelectedMember(selectedPosition)

        binding.groupScheduleMemberRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.groupScheduleMemberRv.adapter = groupScheduleMemberRVAdapter
    }

    private fun clickListener() {
        binding.groupScheduleMemberClose.setOnClickListener {
            finish()
        }

        binding.groupScheduleMemberSave.setOnClickListener {
            val selectedMembers = MoimListUserList(groupScheduleMemberRVAdapter.getSelectedMembers())
            Log.d("GROUP_MEMBER", selectedMembers.toString())
            val intent = Intent(this, GroupScheduleActivity::class.java)
            intent.putExtra(GROUP_MEMBER_INTENT_KEY, selectedMembers)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}