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
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.CategoryColor
import org.joda.time.DateTime

class DailyGroupRVAdapter : RecyclerView.Adapter<DailyGroupRVAdapter.ViewHolder>() {

    private val group = ArrayList<Schedule>()
    private val categoryList = ArrayList<Category>()
    private val groupDiary = ArrayList<MoimDiary>()
    private lateinit var context : Context

    interface GroupContentClickListener {
        fun onGroupContentClick(event: Schedule)
    }
    private lateinit var groupContentClickListener: GroupContentClickListener

    fun setGorupContentClickListener(groupContentClickListener: GroupContentClickListener) {
        this.groupContentClickListener = groupContentClickListener
    }

    /** 기록 아이템 클릭 리스너 **/
    interface DiaryInterface {
        fun onGroupDetailClicked(monthDiary: MoimDiary?)
    }
    private lateinit var diaryRecordClickListener: DiaryInterface
    fun setRecordClickListener(itemClickListener: DiaryInterface){
        diaryRecordClickListener=itemClickListener
    }
    /** ----- **/

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(group[position])

        holder.binding.itemCalendarEventBaseLayout.setOnClickListener {
            groupContentClickListener.onGroupContentClick(group[position])
        }
        
        holder.binding.itemCalendarEventRecord.setOnClickListener {

            val newCategoryId = group[position].categoryServerId

            val diary = groupDiary.find {
                it.scheduleId == group[position].serverId
            }

            if (diary != null) {
                val updatedDiary = diary.copy(categoryId = newCategoryId)
                diaryRecordClickListener.onGroupDetailClicked(updatedDiary)
            }
        }
    }

    override fun getItemCount(): Int = group.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroup(group : ArrayList<Schedule>) {
        this.group.clear()
        this.group.addAll(group)

        notifyDataSetChanged()
    }

    fun setCategory(categoryList : List<Category>) {
        this.categoryList.clear()
        this.categoryList.addAll(categoryList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupDiary(diaryGroup: ArrayList<MoimDiary>){
        this.groupDiary.addAll(diaryGroup)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group : Schedule) {
            val time = DateTime(group.startLong * 1000L).toString("HH:mm") + " - " + DateTime(group.endLong * 1000L).toString("HH:mm")
            val category = categoryList.find {
                if (it.serverId != 0L) it.serverId == group.categoryServerId
                else it.categoryId == group.categoryId }

            binding.itemCalendarTitle.text = group.title
            binding.itemCalendarTitle.isSelected = true
            binding.itemCalendarEventTime.text = time
            if (category != null) {
                binding.itemCalendarEventColorView.backgroundTintList = CategoryColor.convertPaletteIdToColorStateList(category.paletteId)
            }
            binding.itemCalendarEventRecord.setColorFilter(ContextCompat.getColor(context,R.color.realGray))

            val diary=groupDiary.find {
                it.scheduleId==group.serverId
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