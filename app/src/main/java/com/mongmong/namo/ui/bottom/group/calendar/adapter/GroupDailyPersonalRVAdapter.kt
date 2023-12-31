package com.mongmong.namo.ui.bottom.group.calendar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.entity.home.Event
import com.mongmong.namo.R
import com.mongmong.namo.data.entity.home.Category
import com.mongmong.namo.data.remote.moim.MoimSchedule
import com.mongmong.namo.databinding.ItemCalendarEventBinding
import com.mongmong.namo.databinding.ItemCalendarEventGroupBinding
import org.joda.time.DateTime
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class GroupDailyPersonalRVAdapter() : RecyclerView.Adapter<GroupDailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<MoimSchedule>()
    lateinit var colorArray: IntArray
    private lateinit var context : Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemCalendarEventGroupBinding = ItemCalendarEventGroupBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context
        colorArray = context.resources.getIntArray(R.array.categoryColorArr)

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

    inner class ViewHolder(val binding : ItemCalendarEventGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceType")
        fun bind(personal: MoimSchedule) {
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

            binding.itemCalendarEventTitle.text = personal.name
            binding.itemCalendarEventTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(colorArray[paletteId - 1])
            binding.itemCalendarUserName.text = userName
        }
    }

}