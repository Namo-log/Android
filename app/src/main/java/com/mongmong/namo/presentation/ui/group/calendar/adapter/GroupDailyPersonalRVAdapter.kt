package com.mongmong.namo.presentation.ui.group.calendar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.ItemSchedulePreviewMoimBinding
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class GroupDailyPersonalRVAdapter : RecyclerView.Adapter<GroupDailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<MoimScheduleBody>()
    private lateinit var context : Context
    private lateinit var timeConverter: ScheduleTimeConverter

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewMoimBinding = ItemSchedulePreviewMoimBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(personal[position])
    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<MoimScheduleBody>) {
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

        @SuppressLint("ResourceType")
        fun bind(personal: MoimScheduleBody) {
            val colorArray = CategoryColor.getAllColors()

            val paletteId = personal.users[0].color

            val userName =
                if (personal.users.size < 2) personal.users[0].userName
                else personal.users.size.toString() + "명"

            binding.itemCalendarTitle.text = personal.name
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarEventTime.text = timeConverter.getScheduleTimeText(personal.startDate, personal.endDate)
            binding.itemCalendarEventColorView.background.setTint(Color.parseColor(colorArray[paletteId - 1]))
            binding.itemCalendarUserName.text = userName
        }
    }

}