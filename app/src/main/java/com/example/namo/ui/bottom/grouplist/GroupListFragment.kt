package com.example.namo.ui.bottom.grouplist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.group.Group
import com.example.namo.databinding.FragmentGroupListBinding


class GroupListFragment : Fragment() {

    lateinit var binding: FragmentGroupListBinding

    private var groupList = listOf<Group>()
    lateinit var groupAdapter: GroupListRVAdapter

    private lateinit var db: NamoDatabase
    lateinit var mContext : Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentGroupListBinding.inflate(inflater, container, false)

        db = NamoDatabase.getInstance(requireContext())
        groupAdapter = GroupListRVAdapter(groupList)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        initRecyclerView()
        onClickMenu()
    }

    private fun initRecyclerView() {
        val r = Runnable {
            try {
                groupList = db.groupDao.getGroupList()
                groupAdapter = GroupListRVAdapter(groupList)
                requireActivity().runOnUiThread {
                    // 어댑터와 데이터리스트 연결
                    binding.groupListRv.adapter = groupAdapter
                    groupAdapter.notifyDataSetChanged()
                    binding.groupListRv.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    binding.groupListRv.setHasFixedSize(true)
                }
            } catch (e: Exception) {
                Log.d("Group", "Error - $e")
            }
        }
        val thread = Thread(r)
        thread.start()

        groupAdapter.setMyItemClickListener(object : GroupListRVAdapter.ItemClickListener {
            override fun onItemClick(group: Group) { // 그룹 캘린더로 이동
                Log.d("CLICK", "click item")
//                (context as MainActivity).supportFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, GroupCalendarWrapperFragment(group))
//                    .commitAllowingStateLoss()
            }
        })
    }

    //메뉴 클릭시
    private fun onClickMenu(){
        binding.groupMoreIv.setOnClickListener {
            val popupMenu = PopupMenu(mContext, it)
            popupMenu.menuInflater.inflate(R.menu.group_option_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId === R.id.menu_item_create_group) {
//                    Toast.makeText(activity, "그룹 생성", Toast.LENGTH_SHORT).show()
                    showCreateGroupDialog()
                } else if (menuItem.itemId === R.id.menu_item_input_group_code) {
//                    Toast.makeText(activity, "그룹 코드 입력", Toast.LENGTH_SHORT).show()
                    showGroupCodeDialog()
                }
                false
            }
            popupMenu.show()
        }
    }

    // 그룹 생성
    private fun showCreateGroupDialog() {
        view?.findNavController()?.navigate(R.id.action_grouplistFragment_to_groupAddFragment)
        //startActivity(Intent(activity, CreateGroupFragment::class.java))
    }

    // 그룹 코드
    private fun showGroupCodeDialog() {
        GroupCodeDialog(requireContext()) {
            //viewModel.setName(it)
        }.show()
    }
}