package com.example.namo.ui.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.home.Event
import com.example.namo.R
import com.example.namo.data.entity.home.Category
import com.example.namo.databinding.ItemCalendarEventBinding
import org.joda.time.DateTime
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class DailyPersonalRVAdapter() : RecyclerView.Adapter<DailyPersonalRVAdapter.ViewHolder>() {

    private val personal = ArrayList<Event>()
    private val categoryList = ArrayList<Category>()
    private lateinit var context : Context

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryInterface {
        fun onAddClicked(event:Event)
        fun onEditClicked(event: Event)
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
        val binding : com.example.namo.databinding.ItemCalendarEventBinding = ItemCalendarEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(personal[position])

        /** 기록 아이템 클릭 리스너 **/
        holder.binding.itemCalendarEventRecord.setOnClickListener {
            if(personal[position].hasDiary == 0){  // 기록 추가
                diaryRecordClickListener.onAddClicked(personal[position])
            } else{  // 기록 편집
                diaryRecordClickListener.onEditClicked(personal[position])
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
            val time = DateTime(personal.startLong).toString("HH:mm") + " - " + DateTime(personal.endLong).toString("HH:mm")
            val category = categoryList.find { it.categoryIdx == personal.categoryIdx }!!

            binding.itemCalendarEventTitle.text = personal.title
            binding.itemCalendarEventTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            binding.itemCalendarEventColorView.background.setTint(context.resources.getColor(category.color))
            binding.itemCalendarEventRecord.setColorFilter(context.resources.getColor(R.color.realGray))
            /** 기록 아이콘 색깔 **/
            if(personal.hasDiary !=0)
                binding.itemCalendarEventRecord.setColorFilter(context.resources.getColor(R.color.MainOrange))}
        }

}