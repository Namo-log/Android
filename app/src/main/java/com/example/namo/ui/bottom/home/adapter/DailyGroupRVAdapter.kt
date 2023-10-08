package com.example.namo.ui.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.R
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ItemCalendarEventGroupBinding
import org.joda.time.DateTime

class DailyGroupRVAdapter : RecyclerView.Adapter<DailyGroupRVAdapter.ViewHolder>() {

    private val group = ArrayList<Event>()
    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemCalendarEventGroupBinding = ItemCalendarEventGroupBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(group[position])
    }

    override fun getItemCount(): Int = group.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroup(group : ArrayList<Event>) {
        this.group.clear()
        this.group.addAll(group)
    }

    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    inner class ViewHolder(val binding : ItemCalendarEventGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group : Event) {
            val time = DateTime(group.startLong * 1000L).toString("HH:mm") + " - " + DateTime(group.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverIdx != 0L) it.serverIdx == group.categoryServerIdx
                else it.categoryIdx == group.categoryIdx }!!

            binding.itemCalendarEventTitle.text = group.title
            binding.itemCalendarEventTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(category.color)
            binding.itemCalendarUserName.visibility = View.GONE
        }
    }
}