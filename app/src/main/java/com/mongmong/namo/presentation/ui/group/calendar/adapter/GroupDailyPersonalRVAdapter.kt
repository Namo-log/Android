package com.mongmong.namo.presentation.ui.group.calendar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.databinding.ItemSchedulePreviewMoimBinding
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.PaletteType
import org.joda.time.DateTime

class GroupDailyPersonalRVAdapter() : RecyclerView.Adapter<GroupDailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<MoimSchedule>()
    private lateinit var context : Context

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
    fun addPersonal(personal : ArrayList<MoimSchedule>) {
        this.personal.clear()
        this.personal.addAll(personal)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewMoimBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceType")
        fun bind(personal: MoimSchedule) {
            val colorArray = CategoryColor.getAllColors()

            val time =
                DateTime(personal.startDate * 1000L).toString("HH:mm") + " - " + DateTime(personal.endDate * 1000L).toString(
                    "HH:mm"
                )
//            val paletteId =
//                if (personal.users.size < 2 && personal.users[0].color != 0) personal.users[0].color
//                else 14
            val paletteId = personal.users[0].color

            val userName =
                if (personal.users.size < 2) personal.users[0].userName
//                else personal.users.map { it -> it.userName }.joinToString("\n")
                else personal.users.size.toString() + "명"

            binding.itemCalendarTitle.text = personal.name
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(Color.parseColor(colorArray[paletteId - 1]))
            binding.itemCalendarUserName.text = userName
        }
    }

}