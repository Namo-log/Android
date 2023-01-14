package com.example.namo.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.bottom.home.calendar.events.Event
import com.example.namo.databinding.ItemCalendarEventBinding
import org.joda.time.DateTime

class DailyPersonalRVAdapter() : RecyclerView.Adapter<DailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<Event>()
    private lateinit var context : Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemCalendarEventBinding = ItemCalendarEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(personal[position])
    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<Event>) {
        this.personal.clear()
        this.personal.addAll(personal)
    }

    inner class ViewHolder(val binding : ItemCalendarEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(personal : Event) {
            val time = DateTime(personal.startLong).toString("HH:mm") + " - " + DateTime(personal.endLong).toString("HH:mm")
            val color = personal.color

            binding.itemCalendarEventTitle.text = personal.title
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(context.resources.getColor(personal.color))
        }
    }
}