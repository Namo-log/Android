package com.mongmong.namo.presentation.ui.group

import android.content.Intent
import android.util.Log
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.databinding.FragmentGroupListBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity
import com.mongmong.namo.presentation.ui.group.adapter.GroupListRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment
    : BaseFragment<FragmentGroupListBinding>(R.layout.fragment_group_list),
    CreateGroupDialog.GroupCreationListener,
    GroupCodeDialog.GroupCodeListener {

    private val viewModel : GroupViewModel by viewModels()
    private val groupAdapter = GroupListRVAdapter(emptyList())

    override fun setup() {
        binding.viewModel = viewModel

        initObserve()
        onClickMenu()
        setRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getGroupList()
    }

    private fun initObserve() {
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            groups?.let { groupAdapter.updateGroups(groups) }
        }
    }

    private fun setRecyclerView() {
        groupAdapter.setMyItemClickListener(object : GroupListRVAdapter.ItemClickListener {
            override fun onItemClick(moim: Group) { // 그룹 캘린더로 이동
                Log.d("GroupListFrag", "Click Moim Item")
                startActivity(Intent(context, CommunityCalendarActivity::class.java).putExtra("moim", moim))
            }
        })

        binding.groupListRv.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }
    //메뉴 클릭시
    private fun onClickMenu(){
        binding.groupMoreIv.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.group_option_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId === R.id.menu_item_create_group) {
                    showCreateGroupDialog()
                } else if (menuItem.itemId === R.id.menu_item_input_group_code) {
                    showGroupCodeDialog()
                }
                false
            }
            popupMenu.show()
        }
    }

    // 그룹 생성
    private fun showCreateGroupDialog() {
        val dialog = CreateGroupDialog().apply {
            setGroupCreationListener(this@GroupFragment)
        }
        dialog.isCancelable = true
        dialog.show(parentFragmentManager, "CreateGroupDialog")
    }
    override fun onGroupCreated() {
        viewModel.getGroupList()
    }

    // 그룹 코드
    private fun showGroupCodeDialog() {
        val dialog = GroupCodeDialog().apply {
            setGroupCodeListener(this@GroupFragment)
        }
        // 알림창이 띄워져있는 동안 배경 클릭 허용
        dialog.isCancelable = true
        dialog.show(parentFragmentManager, "GroupCodeDialog")
    }

    override fun onGroupParticipate() {
        viewModel.getGroupList()
    }
}