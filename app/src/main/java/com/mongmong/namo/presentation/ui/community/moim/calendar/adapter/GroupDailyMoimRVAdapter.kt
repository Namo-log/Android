package com.mongmong.namo.presentation.ui.community.moim.calendar.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.ItemSchedulePreviewBinding
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import org.joda.time.DateTime

class GroupDailyMoimRVAdapter : RecyclerView.Adapter<GroupDailyMoimRVAdapter.ViewHolder>() {

    private val moimScheduleList = ArrayList<MoimScheduleBody>()
    private lateinit var timeConverter: ScheduleTimeConverter

    interface MoimScheduleClickListener {
        fun onContentClicked(groupSchedule: MoimScheduleBody)
        fun onDiaryIconClicked(groupSchedule: MoimScheduleBody)
    }

    private lateinit var scheduleClickListener : MoimScheduleClickListener

    fun setScheduleClickListener(scheduleClickListener : MoimScheduleClickListener) {
        this.scheduleClickListener = scheduleClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType : Int) : ViewHolder {
        val binding : ItemSchedulePreviewBinding = ItemSchedulePreviewBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int) {
        holder.bind(moimScheduleList[position])
        // 아이템 전체 클릭
        holder.itemView.setOnClickListener {
            scheduleClickListener.onContentClicked(moimScheduleList[position])
        }
        // 기록 아이콘 클릭
        holder.binding.itemSchedulePreviewDiaryIv.setOnClickListener { // 모임 기록
            scheduleClickListener.onDiaryIconClicked(moimScheduleList[position])
        }
    }

    override fun getItemCount(): Int = moimScheduleList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addGroupSchedule(personal : ArrayList<MoimScheduleBody>) {
        this.moimScheduleList.clear()
        this.moimScheduleList.addAll(personal)
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
            binding.scheule = groupSchedule.convertMoimScheduleToSchedule()

            binding.itemSchedulePreviewTimeTv.text = timeConverter.getScheduleTimeText(groupSchedule.startLong, groupSchedule.endLong)
        }
    }
}