package com.mongmong.namo.presentation.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class DailyMoimRVAdapter : RecyclerView.Adapter<DailyMoimRVAdapter.ViewHolder>() {

    private val schedules = ArrayList<GetMonthScheduleResult>()
    private val categoryList = ArrayList<Category>()

    private lateinit var moimScheduleClickListener: MoimScheduleClickListener
    private lateinit var timeConverter: ScheduleTimeConverter

    interface MoimScheduleClickListener {
        fun onContentClicked(schedule: GetMonthScheduleResult)
        fun onDiaryIconClicked(scheduleId: Long, paletteId: Int)
    }

    fun setMoimScheduleClickListener(moimScheduleClickListener: MoimScheduleClickListener) {
        this.moimScheduleClickListener = moimScheduleClickListener
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(DateTime.now())
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(schedules[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            moimScheduleClickListener.onContentClicked(schedules[position])
        }
    }

    override fun getItemCount(): Int = schedules.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroup(group : ArrayList<GetMonthScheduleResult>) {
        this.schedules.clear()
        this.schedules.addAll(group)

        notifyDataSetChanged()
    }

    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule : GetMonthScheduleResult) {
            binding.scheule = schedule

            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == schedule.categoryId
                else it.categoryId == schedule.categoryId
            } ?: categoryList.first()

            binding.itemSchedulePreviewTimeTv.text = timeConverter.getScheduleTimeText(schedule.startDate, schedule.endDate)
            binding.itemSchedulePreviewColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)

            // 기록 아이콘 클릭
            binding.itemSchedulePreviewDiaryIv.setOnClickListener {
                moimScheduleClickListener.onDiaryIconClicked(schedule.scheduleId, category.paletteId)
            }
        }
    }
}