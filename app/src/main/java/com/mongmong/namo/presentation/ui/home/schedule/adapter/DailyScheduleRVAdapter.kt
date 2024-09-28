package com.mongmong.namo.presentation.ui.home.schedule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.data.dto.GetMonthScheduleResult
import com.mongmong.namo.domain.model.Schedule
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class DailyScheduleRVAdapter : RecyclerView.Adapter<DailyScheduleRVAdapter.ViewHolder>() {
    private val scheduleList = ArrayList<Schedule>()
    private val categoryList = ArrayList<Category>()

    private lateinit var scheduleClickListener : PersonalScheduleClickListener
    private lateinit var timeConverter: ScheduleTimeConverter

    interface PersonalScheduleClickListener {
        fun onContentClicked(schedule: Schedule)
        fun onDiaryIconClicked(schedule: Schedule, paletteId: Int)
    }

    fun setDailyScheduleClickListener(scheduleClickListener : PersonalScheduleClickListener) {
        this.scheduleClickListener = scheduleClickListener
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(DateTime.now())
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(scheduleList[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            scheduleClickListener.onContentClicked(scheduleList[position])
        }
    }

    override fun getItemCount(): Int = scheduleList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addSchedules(personal : ArrayList<Schedule>) {
        this.scheduleList.clear()
        this.scheduleList.addAll(personal)

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCategoryList(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)

        notifyDataSetChanged()
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule : Schedule) {
            binding.schedule = schedule

            binding.itemSchedulePreviewTimeTv.text = timeConverter.getScheduleTimeText(schedule.startLong, schedule.endLong)

            // 기록 아이콘 클릭
            binding.itemSchedulePreviewDiaryIv.setOnClickListener {
                scheduleClickListener.onDiaryIconClicked(schedule, schedule.categoryInfo.colorId)
            }
        }
    }
}