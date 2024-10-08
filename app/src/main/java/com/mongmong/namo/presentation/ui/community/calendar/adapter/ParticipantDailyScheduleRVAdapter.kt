package com.mongmong.namo.presentation.ui.community.calendar.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemSchedulePreviewMoimBinding
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

class ParticipantDailyScheduleRVAdapter : RecyclerView.Adapter<ParticipantDailyScheduleRVAdapter.ViewHolder>() {

    private val personal = ArrayList<MoimCalendarSchedule>()
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
    fun addPersonal(personal : ArrayList<MoimCalendarSchedule>) {
        this.personal.clear()
        this.personal.addAll(personal)
        notifyDataSetChanged()
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(DateTime.now())
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewMoimBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(personalSchedule: MoimCalendarSchedule) {
            binding.schedule = personalSchedule

            binding.itemCalendarEventTime.text = timeConverter.getScheduleTimeText(
                SchedulePeriod(LocalDateTime.now(), LocalDateTime.now()) //TODO: 추후 변경 필요
            )
        }
    }
}