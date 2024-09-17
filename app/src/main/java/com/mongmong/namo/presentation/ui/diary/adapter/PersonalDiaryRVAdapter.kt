package com.mongmong.namo.presentation.ui.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemPersonalDiaryBinding
import com.mongmong.namo.domain.model.Diary

class PersonalDiaryRVAdapter(): RecyclerView.Adapter<PersonalDiaryRVAdapter.ViewHolder>() {
    private var diaries: List<Diary> = emptyList()

    fun updateData(newData: List<Diary>) {
        diaries = newData
        notifyDataSetChanged() // 전체 데이터가 변경되었음을 알림
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonalDiaryRVAdapter.ViewHolder {
        val binding = ItemPersonalDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonalDiaryRVAdapter.ViewHolder, position: Int) {
        val diaryItem = diaries[position]
        holder.bind(diaryItem)
    }

    inner class ViewHolder(private val binding: ItemPersonalDiaryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(diary: Diary) {
            binding.diary = diary
        }
    }

    override fun getItemCount() = diaries.size

}