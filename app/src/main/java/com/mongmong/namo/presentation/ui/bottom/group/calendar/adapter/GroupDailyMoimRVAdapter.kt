package com.mongmong.namo.presentation.ui.bottom.group.calendar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.presentation.config.CategoryColor
import org.joda.time.DateTime

class GroupDailyMoimRVAdapter : RecyclerView.Adapter<GroupDailyMoimRVAdapter.ViewHolder>() {

    private val groupSchedule = ArrayList<MoimSchedule>()
    lateinit var colorArray: ArrayList<String>
    private lateinit var context : Context

    interface MoimScheduleClickListener {
        fun onContentClicked(groupSchedule: MoimSchedule)
        fun onDiaryIconClicked(groupSchedule: MoimSchedule)
    }

    private lateinit var moimScheduleClickListener : MoimScheduleClickListener

    fun setMoimScheduleClickListener(moimScheduleClickListener : MoimScheduleClickListener) {
        this.moimScheduleClickListener = moimScheduleClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context
        colorArray = CategoryColor.getAllColors()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(groupSchedule[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            moimScheduleClickListener.onContentClicked(groupSchedule[position])
        }
        // 기록 아이콘 클릭
        holder.binding.itemCalendarScheduleRecord.setOnClickListener { // 모임 기록
            moimScheduleClickListener.onDiaryIconClicked(groupSchedule[position])
        }
    }

    override fun getItemCount(): Int = groupSchedule.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupSchedule(personal : ArrayList<MoimSchedule>) {
        this.groupSchedule.clear()
        this.groupSchedule.addAll(personal)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceType")
        fun bind(groupSchedule: MoimSchedule) {
            val time =
                DateTime(groupSchedule.startDate * 1000L).toString("HH:mm") + " - " + DateTime(groupSchedule.endDate * 1000L).toString(
                    "HH:mm"
                )
            val paletteId = if (groupSchedule.curMoimSchedule) 4
                        else if (groupSchedule.users.size < 2 && groupSchedule.users[0].color != 0) groupSchedule.users[0].color
                        else 3

            binding.itemCalendarTitle.text = groupSchedule.name
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarScheduleTime.text = time
            binding.itemCalendarScheduleColorView.background.setTint(Color.parseColor(colorArray[paletteId - 1]))

            if(groupSchedule.hasDiaryPlace)
                binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.MainOrange))
            else
                binding.itemCalendarScheduleRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))
        }
    }

}