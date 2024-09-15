package com.mongmong.namo.presentation.ui.diary.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ItemDiaryCalendarDateBinding
import com.mongmong.namo.domain.model.CalendarDay


class DiaryCalendarAdapter(
    private val recyclerView: RecyclerView,
    private val items: List<CalendarDay>,
    private val listener: OnCalendarDayClickListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var diaryDates: MutableMap<String, Set<String>> = mutableMapOf() // "yyyy-MM": [기록된 날짜들]
    private var isOpeningBottomSheet: Boolean = false
    private var shouldAnimate: Boolean = false

    // 기록된 날짜 정보 업데이트
    fun updateDiaryDates(yearMonth: String, recordDates: Set<String>) {
        diaryDates[yearMonth] = recordDates
        notifyDataSetChanged()
    }

    fun updateBottomSheetState(isOpened: Boolean) {
        this.isOpeningBottomSheet = isOpened
        this.shouldAnimate = true

        for (i in 0 until itemCount) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.updateItem(isOpened)
        }

        this.shouldAnimate = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    fun getItemAtPosition(position: Int): CalendarDay? {
        return if (position in items.indices) items[position] else null
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemDiaryCalendarDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.calendarDay = calendarDay

            val yearMonth = "${calendarDay.year}-${String.format("%02d", calendarDay.month + 1)}"
            val day = calendarDay.date.split("/").last() // "/"로 구분하여 마지막 부분을 사용 (예: "n/1" -> "1")
            val hasDiary = diaryDates[yearMonth]?.contains(day) ?: false
            binding.diaryCalendarHasDiaryIndicatorIv.visibility = if (hasDiary) View.VISIBLE else View.GONE

            updateItem(isOpeningBottomSheet)

            binding.root.setOnClickListener {
                listener.onCalendarDayClick(calendarDay)
            }
        }

        fun updateItem(isOpening: Boolean) {
            val fromHeight = if (isOpening) dpToPx(84, binding.root.context) else dpToPx(56, binding.root.context)
            val toHeight = if (isOpening) dpToPx(56, binding.root.context) else dpToPx(84, binding.root.context)

            if (shouldAnimate) {
                animateHeightChange(fromHeight, toHeight)
            } else {
                // 애니메이션 없이 높이 변경
                binding.root.layoutParams = binding.root.layoutParams.apply {
                    height = toHeight
                }
                binding.root.requestLayout()
            }

            val indicatorImage = if(isOpening) R.drawable.ic_calendar else R.drawable.img_mongi_default
            binding.diaryCalendarHasDiaryIndicatorIv.setImageResource(indicatorImage)
        }

        fun animateHeightChange(fromHeight: Int, toHeight: Int) {
            val valueAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
            valueAnimator.apply {
                addUpdateListener { animator ->
                    val layoutParams = binding.root.layoutParams
                    layoutParams.height = animator.animatedValue as Int
                    binding.root.layoutParams = layoutParams
                    binding.root.requestLayout()
                }
                duration = 170
            }
            valueAnimator.start()
        }
    }

    interface OnCalendarDayClickListener {
        fun onCalendarDayClick(calendarDay: CalendarDay)
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
