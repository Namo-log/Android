package com.mongmong.namo.presentation.ui.bottom.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.databinding.ItemCalendarEventBinding
import org.joda.time.DateTime

class DailyGroupRVAdapter : RecyclerView.Adapter<DailyGroupRVAdapter.ViewHolder>() {

    private val group = ArrayList<Event>()
    private val categoryList = ArrayList<Category>()
    private val groupDiary = ArrayList<DiaryResponse.MonthDiary>()
    private lateinit var context : Context

    interface GroupContentClickListener {
        fun onGroupContentClick(event: Event)
    }
    private lateinit var groupContentClickListener: GroupContentClickListener

    fun setGorupContentClickListener(groupContentClickListener: GroupContentClickListener) {
        this.groupContentClickListener = groupContentClickListener
    }

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryInterface {
        fun onGroupDetailClicked(monthDiary: DiaryResponse.MonthDiary?)
    }
    private lateinit var diaryRecordClickListener: DiaryInterface
    fun setRecordClickListener(itemClickListener: DiaryInterface){
        diaryRecordClickListener=itemClickListener
    }
    /** ----- **/

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemCalendarEventBinding = ItemCalendarEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(group[position])

        holder.binding.itemCalendarEventBaseLayout.setOnClickListener {
            groupContentClickListener.onGroupContentClick(group[position])
        }
        
        holder.binding.itemCalendarEventRecord.setOnClickListener {

            val newCategoryId = group[position].categoryServerIdx

            val diary = groupDiary.find {
                it.scheduleIdx == group[position].serverIdx
            }

            if (diary != null) {
                val updatedDiary = diary.copy(categoryId = newCategoryId)
                diaryRecordClickListener.onGroupDetailClicked(updatedDiary)
            }
        }
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

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupDiary(diaryGroup: ArrayList<DiaryResponse.MonthDiary>){
        this.groupDiary.addAll(diaryGroup)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding : ItemCalendarEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group : Event) {
            val time = DateTime(group.startLong * 1000L).toString("HH:mm") + " - " + DateTime(group.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverIdx != 0L) it.serverIdx == group.categoryServerIdx
                else it.categoryIdx == group.categoryIdx }

            binding.itemCalendarEventTitle.text = group.title
            binding.itemCalendarEventTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            if (category != null) {
                binding.itemCalendarEventColorView.background.setTint(category.color)
            }
            binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            val diary=groupDiary.find {
                it.scheduleIdx==group.serverIdx
            }
            if(group.hasDiary !=0) {
                binding.itemCalendarEventRecord.visibility=View.VISIBLE
                if (diary?.content.isNullOrEmpty()) binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))
                else binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context,R.color.MainOrange))
            }
            else binding.itemCalendarEventRecord.visibility=View.GONE

        }
    }
}