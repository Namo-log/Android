package com.mongmong.namo.presentation.ui.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.presentation.config.CategoryColor
import org.joda.time.DateTime

class DailyPersonalRVAdapter : RecyclerView.Adapter<DailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<Schedule>()
    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context

    interface PersonalScheduleClickListener {
        fun onContentClicked(schedule : Schedule)
        fun onDiaryIconClicked(schedule: Schedule)
    }

    private lateinit var personalScheduleClickListener : PersonalScheduleClickListener
    fun setPersonalScheduleClickListener(personalScheduleClickListener : PersonalScheduleClickListener) {
        this.personalScheduleClickListener = personalScheduleClickListener
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
        // 기록 아이콘 클릭
        if (!personal[position].moimSchedule) { // 개인 기록
            holder.binding.itemCalendarScheduleRecord.setOnClickListener {
                personalScheduleClickListener.onDiaryIconClicked(personal[position])
            }
        }
    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<Schedule>) {
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

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceType")
        fun bind(personal : Schedule) {
            val time = DateTime(personal.startLong * 1000L).toString("HH:mm") + " - " + DateTime(personal.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == personal.categoryServerId
                else it.categoryId == personal.categoryId }!!

            binding.itemCalendarTitle.text = personal.title
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarScheduleTime.text = time
            binding.itemCalendarScheduleColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            /** 기록 아이콘 색깔 **/
            if(personal.hasDiary != 0)
                binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context , R.color.MainOrange))
        }
    }

}