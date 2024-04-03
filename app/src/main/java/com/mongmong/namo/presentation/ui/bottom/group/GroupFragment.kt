package com.mongmong.namo.presentation.ui.bottom.group

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.Group
import com.mongmong.namo.databinding.FragmentGroupListBinding
import com.mongmong.namo.presentation.ui.bottom.group.adapter.GroupListRVAdapter
import com.mongmong.namo.presentation.utils.NetworkCheckerImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : Fragment(), CreateGroupDialog.GroupCreationListener {
    lateinit var binding: FragmentGroupListBinding
    private val viewModel : GroupViewModel by viewModels()
    private val groupAdapter = GroupListRVAdapter(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentGroupListBinding.inflate(inflater, container, false)

        initObserve()
        onClickMenu()
        setRecyclerView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getGroups()
    }

    private fun initObserve() {
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            setEmptyView(groups.isEmpty())
            groupAdapter.updateGroups(groups)
        }
    }
    private fun setEmptyView(isEmpty: Boolean) {
        if(isEmpty) {
            binding.groupListEmptyTv.apply {
                text = getText(
                    if(NetworkCheckerImpl(requireContext()).isOnline())
                        R.string.add_group_msg
                    else R.string.network_group_msg
                )
                visibility = View.VISIBLE
            }
        } else {
            binding.groupListEmptyTv.visibility = View.GONE
            binding.groupListRv.visibility = View.VISIBLE
        }
    }
    private fun setRecyclerView() {
        groupAdapter.setMyItemClickListener(object : GroupListRVAdapter.ItemClickListener {
            override fun onItemClick(moim: Group) { // 그룹 캘린더로 이동
                Log.d("GroupListFrag", "Click Moim Item")
                startActivity(Intent(context, GroupCalendarActivity::class.java).putExtra("moim", moim))
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
        viewModel.getGroups()
    }

    // 그룹 코드
    private fun showGroupCodeDialog() {
        val dialog = GroupCodeDialog()
        // 알림창이 띄워져있는 동안 배경 클릭 허용
        dialog.isCancelable = true
        dialog.show(parentFragmentManager, "GroupCodeDialog")
    }
}