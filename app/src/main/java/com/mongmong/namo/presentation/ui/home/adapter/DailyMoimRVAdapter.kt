package com.mongmong.namo.presentation.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class DailyMoimRVAdapter : RecyclerView.Adapter<DailyMoimRVAdapter.ViewHolder>() {

    private val schedules = ArrayList<Schedule>()
    private val categoryList = ArrayList<Category>()
    private val moimDiary = ArrayList<MoimDiary>()
    private lateinit var context : Context
    private lateinit var moimScheduleClickListener: MoimScheduleClickListener
    private lateinit var timeConverter: ScheduleTimeConverter

    interface MoimScheduleClickListener {
        fun onContentClicked(schedule: Schedule)
        fun onDiaryIconClicked(schedule: Schedule)
    }

    fun setMoimScheduleClickListener(moimScheduleClickListener: MoimScheduleClickListener) {
        this.moimScheduleClickListener = moimScheduleClickListener
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(DateTime.now())
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(schedules[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            moimScheduleClickListener.onContentClicked(schedules[position])
        }
        // 기록 아이콘 클릭
        holder.binding.itemCalendarScheduleRecord.setOnClickListener {
            if (schedules[position].hasDiary != null) {
                moimScheduleClickListener.onDiaryIconClicked(schedules[position])
            }
        }
    }

    override fun getItemCount(): Int = schedules.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroup(group : ArrayList<Schedule>) {
        this.schedules.clear()
        this.schedules.addAll(group)

        notifyDataSetChanged()
    }

    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupDiary(diaryGroup: ArrayList<MoimDiary>){
        this.moimDiary.addAll(diaryGroup)
        notifyDataSetChanged()
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule : Schedule) {
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == schedule.categoryServerId
                else it.categoryId == schedule.categoryId }

            binding.itemCalendarTitle.text = schedule.title
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarScheduleTime.text = timeConverter.getScheduleTimeText(schedule.startLong, schedule.endLong)
            if (category != null) {
                binding.itemCalendarScheduleColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            }
            // 기록 아이콘 표시
            with(binding.itemCalendarScheduleRecord) {
                if (schedule.hasDiary != null) { // 그룹에서 추가한 모임 기록이 있을 때
                    this.visibility = View.VISIBLE
                    if (schedule.hasDiary == false) this.setColorFilter(ContextCompat.getColor(context,R.color.realGray))
                    else this.setColorFilter(ContextCompat.getColor(context,R.color.MainOrange)) // 개인이 메모를 추가했을 떄
                }
                else this.visibility = View.GONE
            }
        }
    }
}