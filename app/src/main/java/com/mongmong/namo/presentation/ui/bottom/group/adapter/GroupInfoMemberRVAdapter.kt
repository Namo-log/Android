package com.mongmong.namo.presentation.ui.bottom.group.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.MoimUser
import com.mongmong.namo.databinding.ItemGroupMemberBinding
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.PaletteType

class GroupInfoMemberRVAdapter(
    private val members : List<MoimUser>
) : RecyclerView.Adapter<GroupInfoMemberRVAdapter.ViewHolder>() {

    private lateinit var context : Context
    private lateinit var categoryColorArray : ArrayList<String>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupInfoMemberRVAdapter.ViewHolder {
        val binding = ItemGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        categoryColorArray = CategoryColor.getAllColors()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupInfoMemberRVAdapter.ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size


    inner class ViewHolder(val binding : ItemGroupMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member : MoimUser) {
            val color = if (member.color < 20) categoryColorArray[member.color - 1]
                        else CategoryColor.DEFAULT_PALETTE_COLOR1.hexColor

            binding.itemGroupMemberColorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
            binding.itemGroupMemberNameTv.text = member.userName
        }
    }

}