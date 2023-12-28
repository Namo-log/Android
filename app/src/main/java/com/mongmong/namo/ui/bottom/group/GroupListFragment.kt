package com.mongmong.namo.ui.bottom.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.NamoDatabase
import com.mongmong.namo.data.remote.moim.GetMoimListResponse
import com.mongmong.namo.data.remote.moim.GetMoimListView
import com.mongmong.namo.data.remote.moim.Moim
import com.mongmong.namo.data.remote.moim.MoimService
import com.mongmong.namo.databinding.FragmentGroupListBinding
import com.mongmong.namo.ui.bottom.group.adapter.GroupListRVAdapter


class GroupListFragment : Fragment(), GetMoimListView {

    lateinit var binding: FragmentGroupListBinding

    private var groupList = listOf<Moim>()
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

        onClickMenu()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val moimService = MoimService()
        moimService.setGetMoimListView(this)
        moimService.getMoimList()
    }

    override fun onStart() {
        super.onStart()

//        initRecyclerView()
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
        val dialog = CreateGroupDialog()
        // 알림창이 띄워져있는 동안 배경 클릭 허용
        dialog.isCancelable = true
        dialog.show(this.requireFragmentManager(), "CreateGroupDialog")
//        view?.findNavController()?.navigate(R.id.action_grouplistFragment_to_groupAddFragment)
        //startActivity(Intent(activity, CreateGroupFragment::class.java))
    }

    // 그룹 코드
    private fun showGroupCodeDialog() {
        GroupCodeDialog(requireContext()) {
            //viewModel.setName(it)
        }.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onGetMoimListSuccess(response: GetMoimListResponse) {
        Log.d("GroupListFrag", "onGetMoimListSuccess")
        Log.d("GroupListFrag", response.result.toString())

        if (response.result.isEmpty()) {
            binding.groupListRv.visibility = View.GONE
            binding.groupListEmptyTv.visibility = View.VISIBLE
        } else {
            binding.groupListNetworkTv.visibility = View.GONE
            binding.groupListRv.visibility = View.VISIBLE
        }

        groupList = response.result
        groupAdapter = GroupListRVAdapter(groupList)
        binding.groupListRv.adapter = groupAdapter
        groupAdapter.notifyDataSetChanged()
        binding.groupListRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupListRv.setHasFixedSize(true)

        groupAdapter.setMyItemClickListener(object : GroupListRVAdapter.ItemClickListener {
            override fun onItemClick(moim: Moim) { // 그룹 캘린더로 이동
                Log.d("GroupListFrag", "Click Moim Item")

//                val action = GroupListFragmentDirections.actionGroupListFragmentToGroupCalendarFragment(moim)
//                view?.findNavController()?.navigate(action)

                val intent = Intent(context, GroupCalendarActivity::class.java)
                intent.putExtra("moim", moim)
                startActivity(intent)

            }
        })
    }

    override fun onGetMoimListFailure(message: String) {
        Log.d("GroupListFrag", "onGetMoimListFailure")

        binding.groupListNetworkTv.visibility = View.VISIBLE
        binding.groupListRv.visibility = View.GONE
    }
}