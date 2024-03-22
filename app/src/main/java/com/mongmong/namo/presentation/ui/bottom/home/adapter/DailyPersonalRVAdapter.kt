package com.mongmong.namo.presentation.ui.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.ItemCalendarEventBinding
import org.joda.time.DateTime

class DailyPersonalRVAdapter() : RecyclerView.Adapter<DailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<Event>()
    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryInterface {
        fun onDetailClicked(event: Event)
    }
    private lateinit var diaryRecordClickListener: DiaryInterface
    fun setRecordClickListener(itemClickListener: DiaryInterface){
        diaryRecordClickListener=itemClickListener
    }
    /** ----- **/

    interface ContentClickListener {
        fun onContentClick(event : Event)
    }
    private lateinit var contentClickListener : ContentClickListener
    fun setContentClickListener(contentClickListener : ContentClickListener) {
        this.contentClickListener = contentClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemCalendarEventBinding = ItemCalendarEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(personal[position])

        /** 기록 아이템 클릭 리스너 **/
        if (!personal[position].moimSchedule){ // 개인 기록
            holder.binding.itemCalendarEventRecord.setOnClickListener {
                diaryRecordClickListener.onDetailClicked(personal[position])
            }
        }

        holder.binding.itemCalendarEventContentLayout.setOnClickListener {
            contentClickListener.onContentClick(personal[position])
        }

    }

    override fun getItemCount(): Int = personal.size

    @SuppressLint("NotifyDataSetChanged")
    fun addPersonal(personal : ArrayList<Event>) {
        this.personal.clear()
        this.personal.addAll(personal)
    }

    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    inner class ViewHolder(val binding : ItemCalendarEventBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceType")
        fun bind(personal : Event) {
            val time = DateTime(personal.startLong * 1000L).toString("HH:mm") + " - " + DateTime(personal.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == personal.categoryServerId
                else it.categoryId == personal.categoryId }!!

            binding.itemCalendarTitle.text = personal.title
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(category.color)
            binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            /** 기록 아이콘 색깔 **/
            if(personal.hasDiary !=0)
                binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context , R.color.MainOrange))}
        }

}