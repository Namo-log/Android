package com.mongmong.namo.presentation.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class DailyPersonalRVAdapter : RecyclerView.Adapter<DailyPersonalRVAdapter.ViewHolder>() {
    private val personal = ArrayList<GetMonthScheduleResult>()
    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context
    private lateinit var personalScheduleClickListener : PersonalScheduleClickListener
    private lateinit var timeConverter: ScheduleTimeConverter

    interface PersonalScheduleClickListener {
        fun onContentClicked(schedule: GetMonthScheduleResult)
        fun onDiaryIconClicked(schedule: GetMonthScheduleResult, paletteId: Int)
    }

    fun setPersonalScheduleClickListener(personalScheduleClickListener : PersonalScheduleClickListener) {
        this.personalScheduleClickListener = personalScheduleClickListener
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
        holder.bind(personal[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            personalScheduleClickListener.onContentClicked(personal[position])
        }
    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<GetMonthScheduleResult>) {
        this.personal.clear()
        this.personal.addAll(personal)

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)

        notifyDataSetChanged()
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(schedule : GetMonthScheduleResult) {
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == schedule.categoryId
                else it.categoryId == schedule.categoryId }!!

            binding.itemCalendarTitle.text = schedule.name
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarScheduleTime.text = timeConverter.getScheduleTimeText(schedule.startDate, schedule.endDate)
            binding.itemCalendarScheduleColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            /** 기록 아이콘 색깔 **/
            if (schedule.hasDiary != false)
                binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context , R.color.mainOrange))

            // 기록 아이콘 클릭
            if (!schedule.moimSchedule) { // 개인 기록
                binding.itemCalendarScheduleRecord.setOnClickListener {
                    personalScheduleClickListener.onDiaryIconClicked(schedule, category.paletteId)
                }
            }
        }
    }
}