package com.mongmong.namo.presentation.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
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
import org.joda.time.DateTime

class DailyGroupRVAdapter : RecyclerView.Adapter<DailyGroupRVAdapter.ViewHolder>() {

    private val schedules = ArrayList<Schedule>()
    private val categoryList = ArrayList<Category>()
    private val moimDiary = ArrayList<MoimDiary>()
    private lateinit var context : Context

    interface MoimScheduleClickListener {
        fun onContentClicked(schedule: Schedule)
        fun onDiaryIconClicked(monthDiary: MoimDiary?)
    }

    fun setMoimScheduleClickListener(moimScheduleClickListener: MoimScheduleClickListener) {
        this.moimScheduleClickListener = moimScheduleClickListener
    }

    private lateinit var moimScheduleClickListener: MoimScheduleClickListener


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
            val newCategoryId = schedules[position].categoryServerId

            val diary = moimDiary.find {
                it.scheduleId == schedules[position].serverId
            }

            if (diary != null) {
                val updatedDiary = diary.copy(categoryId = newCategoryId)
                moimScheduleClickListener.onDiaryIconClicked(updatedDiary)
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

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule : Schedule) {
            val time = DateTime(schedule.startLong * 1000L).toString("HH:mm") + " - " + DateTime(schedule.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == schedule.categoryServerId
                else it.categoryId == schedule.categoryId }

            binding.itemCalendarTitle.text = schedule.title
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarScheduleTime.text = time
            if (category != null) {
                binding.itemCalendarScheduleColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            }
            binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            val diary = moimDiary.find {
                it.scheduleId == schedule.serverId
            }
            if (schedule.hasDiary != 0) {
                binding.itemCalendarScheduleRecord.visibility = View.VISIBLE
                if (diary?.content.isNullOrEmpty()) binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))
                else binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.MainOrange))
            }
            else binding.itemCalendarScheduleRecord.visibility = View.GONE

        }
    }
}