package com.mongmong.namo.presentation.ui.bottom.diary.groupDiary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.remote.diary.DiaryResponse
import com.mongmong.namo.databinding.ItemDiaryGroupPayMemberBinding

class GroupPayMemberRVAdapter(  // 그룹 장소별 정산 다이얼로그 멤버
    private var members: List<DiaryResponse.GroupUser>,
    private var memberIsChecked: MutableList<Pair<Long, Boolean>>
) :
    RecyclerView.Adapter<GroupPayMemberRVAdapter.ViewHolder>() {

    /** 체크한 멤버 정산 **/
    interface PeopleItemClickListener {
        fun onItemClick(
            peopleList: ArrayList<String>,
            memberIsChecked: MutableList<Pair<Long, Boolean>>
        )
    }

    private lateinit var mItemClickListener: PeopleItemClickListener
    fun setPeopleItemClickListener(itemClickListener: PeopleItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupPayMemberBinding = ItemDiaryGroupPayMemberBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val memberId = members[position]
        holder.bind(memberId, memberIsChecked[position].second)

        holder.binding.itemGroupPayMemberCheckBox.setOnClickListener {

            memberIsChecked[position] =
                memberId.userId to holder.binding.itemGroupPayMemberCheckBox.isChecked

            mItemClickListener.onItemClick(members as ArrayList<String>, memberIsChecked)
        }
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemDiaryGroupPayMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: DiaryResponse.GroupUser, isChecked: Boolean) {
            binding.itemGroupMemberName.text = member.userName
            binding.itemGroupPayMemberCheckBox.isChecked = isChecked
        }
    }
}
