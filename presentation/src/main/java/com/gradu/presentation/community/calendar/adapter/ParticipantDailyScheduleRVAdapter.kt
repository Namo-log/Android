package com.gradu.presentation.community.calendar.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.core.utils.converter.ScheduleTimeConverter
import com.gradu.domain.model.CommunityCommonSchedule
import com.gradu.domain.model.SchedulePeriod
import com.gradu.presentation.databinding.ItemSchedulePreviewMoimBinding
import org.threeten.bp.LocalDateTime

class ParticipantDailyScheduleRVAdapter : RecyclerView.Adapter<ParticipantDailyScheduleRVAdapter.ViewHolder>() {

    private val personal = ArrayList<CommunityCommonSchedule>()
    private lateinit var timeConverter: ScheduleTimeConverter

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewMoimBinding = ItemSchedulePreviewMoimBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(personal[position])
    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<CommunityCommonSchedule>) {
        this.personal.clear()
        this.personal.addAll(personal)
        notifyDataSetChanged()
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(LocalDateTime.now())
    }

    fun setClickedDate(date: LocalDateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewMoimBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(personalSchedule: CommunityCommonSchedule) {
            binding.schedule = personalSchedule
            binding.itemCalendarEventTime.text = timeConverter.getScheduleTimeText(
                SchedulePeriod(LocalDateTime.now(), LocalDateTime.now())
            )
        }
    }
}