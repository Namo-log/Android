package com.mongmong.namo.presentation.ui.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimDiaryBinding
import com.mongmong.namo.databinding.ItemPersonalDiaryBinding
import com.mongmong.namo.domain.model.Diary

class MoimDiaryRVAdapter(
    private val onItemClick: (Long) -> Unit
): RecyclerView.Adapter<MoimDiaryRVAdapter.ViewHolder>() {
    private var diaries: List<Diary> = emptyList()

    fun updateData(newData: List<Diary>) {
        diaries = newData
        notifyDataSetChanged() // 전체 데이터가 변경되었음을 알림
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MoimDiaryRVAdapter.ViewHolder {
        val binding = ItemMoimDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoimDiaryRVAdapter.ViewHolder, position: Int) {
        val diaryItem = diaries[position]
        holder.bind(diaryItem)
    }

    inner class ViewHolder(private val binding: ItemMoimDiaryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(diary: Diary) {
            binding.diary = diary

            binding.itemMoimDiaryContentCl.setOnClickListener { onItemClick(diary.scheduleId) }
        }
    }

    override fun getItemCount() = diaries.size

}