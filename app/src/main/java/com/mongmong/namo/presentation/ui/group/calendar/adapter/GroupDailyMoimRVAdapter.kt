package com.mongmong.namo.presentation.ui.group.calendar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class GroupDailyMoimRVAdapter : RecyclerView.Adapter<GroupDailyMoimRVAdapter.ViewHolder>() {

    private val groupSchedule = ArrayList<MoimScheduleBody>()
    private lateinit var context : Context
    private lateinit var timeConverter: ScheduleTimeConverter

    interface MoimScheduleClickListener {
        fun onContentClicked(groupSchedule: MoimScheduleBody)
        fun onDiaryIconClicked(groupSchedule: MoimScheduleBody)
    }

    private lateinit var moimScheduleClickListener : MoimScheduleClickListener

    fun setMoimScheduleClickListener(moimScheduleClickListener : MoimScheduleClickListener) {
        this.moimScheduleClickListener = moimScheduleClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(groupSchedule[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            moimScheduleClickListener.onContentClicked(groupSchedule[position])
        }
        // 기록 아이콘 클릭
        holder.binding.itemSchedulePreviewDiaryIv.setOnClickListener { // 모임 기록
            moimScheduleClickListener.onDiaryIconClicked(groupSchedule[position])
        }
    }

    override fun getItemCount(): Int = groupSchedule.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupSchedule(personal : ArrayList<MoimScheduleBody>) {
        this.groupSchedule.clear()
        this.groupSchedule.addAll(personal)
        notifyDataSetChanged()
    }

    fun initScheduleTimeConverter() {
        timeConverter = ScheduleTimeConverter(DateTime.now())
    }

    fun setClickedDate(date: DateTime) {
        // converter에서 선택한 날짜 업데이트
        timeConverter.updateClickedDate(date)
    }

    inner class ViewHolder(val binding : ItemSchedulePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(groupSchedule: MoimScheduleBody) {
            binding.itemSchedulePreviewTitleTv.text = groupSchedule.name
            binding.itemSchedulePreviewTitleTv.isSelected = true
            binding.itemSchedulePreviewTimeTv.text = timeConverter.getScheduleTimeText(groupSchedule.startLong, groupSchedule.endLong)
            binding.itemSchedulePreviewColorView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mainOrange))

            if (groupSchedule.hasDiaryPlace)
                binding.itemSchedulePreviewDiaryIv.setColorFilter(ContextCompat.getColor(context,R.color.mainOrange))
            else
                binding.itemSchedulePreviewDiaryIv.setColorFilter(ContextCompat.getColor(context,R.color.realGray))
        }
    }

}