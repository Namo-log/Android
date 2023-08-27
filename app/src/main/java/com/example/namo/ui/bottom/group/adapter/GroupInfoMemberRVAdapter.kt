package com.example.namo.ui.bottom.group.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.remote.moim.MoimListUser
import com.example.namo.databinding.ItemGroupMemberBinding

class GroupInfoMemberRVAdapter(
    private val members : List<MoimListUser>
) : RecyclerView.Adapter<GroupInfoMemberRVAdapter.ViewHolder>() {

    private lateinit var context : Context
    private lateinit var categoryColorArray : IntArray

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupInfoMemberRVAdapter.ViewHolder {
        val binding = ItemGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        categoryColorArray = context.resources.getIntArray(R.array.categoryColorArr)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupInfoMemberRVAdapter.ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size


    inner class ViewHolder(val binding : ItemGroupMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member : MoimListUser) {
            Log.d("TEST_MEMBER", member.color.toString())
            Log.d("TEST_COLOR", R.color.palette1.toString())
            Log.d("TEST_COLOR", R.color.palette2.toString())
            Log.d("TEST_COLOR", R.color.palette3.toString())
            val color = if (member.color < 20) categoryColorArray[member.color - 1]
                        else context.resources.getColor(member.color)

            binding.itemGroupMemberColorView.background.setTint(color)
            binding.itemGroupMemberNameTv.text = member.userName
        }
    }

}