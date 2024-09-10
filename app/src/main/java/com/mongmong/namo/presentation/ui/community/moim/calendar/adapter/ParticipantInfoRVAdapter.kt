package com.mongmong.namo.presentation.ui.community.moim.calendar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemScheduleColorInfoBinding
import com.mongmong.namo.domain.model.CalendarColorInfo

class ParticipantInfoRVAdapter(
    private val participantList : List<CalendarColorInfo>
) : RecyclerView.Adapter<ParticipantInfoRVAdapter.ViewHolder>() {

    private lateinit var context : Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemScheduleColorInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participantList[position])
    }

    override fun getItemCount(): Int = participantList.size


    inner class ViewHolder(val binding : ItemScheduleColorInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(participant : CalendarColorInfo) {
            binding.info = participant
        }
    }
}