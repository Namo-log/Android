package com.gradu.presentation.ui.home.diary.adapter

import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ItemDiaryCalendarDateBinding
import com.mongmong.namo.domain.model.CalendarDate
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.domain.model.ScheduleType
import com.mongmong.namo.presentation.utils.converter.DiaryDateConverter.toYearMonth

class DiaryCalendarAdapter(
    private val recyclerView: RecyclerView,
    private val items: List<CalendarDay>,
    private val listener: OnCalendarListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var diaryDates: MutableMap<String, Set<CalendarDate>> = mutableMapOf() // "yyyy-MM": [기록된 날짜들]
    private var isBottomSheetOpen: Boolean = false // 바텀시트 상태
    private var selectedDateView: TextView? = null  // 선택된 날짜의 TextView
    private var selectedDate: CalendarDay? = null  // 선택된 날짜
    private var visibleMonth: String? = null // 현재 화면 중앙의 달 ("yyyy-MM")

    fun updateDiaryDates(yearMonth: String, diaryDates: Set<CalendarDate>) {
        this.diaryDates[yearMonth] = diaryDates
        items.forEachIndexed { index, calendarDay ->
            if (calendarDay.toYearMonth() == yearMonth) {
                notifyItemChanged(index)
            }
        }
    }

    fun updateVisibleMonth(visibleMonth: String) {
        if (this.visibleMonth != visibleMonth) {
            this.visibleMonth = visibleMonth
            notifyDataSetChanged() // 현재 보이는 달이 바뀌었을 때 전체 갱신
        }
    }

    fun updateBottomSheetState(isOpened: Boolean) {
        if (isBottomSheetOpen == isOpened || itemCount == 0) return
        isBottomSheetOpen = isOpened

        val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.updateItemWithAnimate(isOpened)
        }

        for (i in 0 until firstVisibleItemPosition) {
            notifyItemChanged(i, isOpened)
        }
        for (i in lastVisibleItemPosition + 1 until itemCount) {
            notifyItemChanged(i, isOpened)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.updateItem(isBottomSheetOpen)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                PAYLOAD_SELECT -> holder.setSelected(true)
                PAYLOAD_DESELECT -> holder.setSelected(false)
                else -> {
                    Log.e("DiaryCalendarAdapter", "Unexpected payload: ${payloads[0]}")
                    holder.bind(items[position])
                }
            }
        } else {
            Log.e("DiaryCalendarAdapter", "Unexpected payload2")
            holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAtPosition(position: Int): CalendarDay? {
        return if (position in items.indices) items[position] else null
    }

    inner class ViewHolder(val binding: ItemDiaryCalendarDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.calendarDay = calendarDay

            // Indicator 설정
            val dateType = diaryDates[calendarDay.toYearMonth()]?.find { it.date == calendarDay.date.toString() }?.type
            binding.diaryCalendarHasDiaryIndicatorIv.visibility = if (dateType != null) View.VISIBLE else View.GONE

            val color = when (dateType) {
                ScheduleType.PERSONAL, ScheduleType.BIRTHDAY -> R.color.text_placeholder
                ScheduleType.MOIM -> R.color.main
                else -> null
            }

            color?.let {
                binding.diaryCalendarHasDiaryIndicatorIv.setColorFilter(
                    binding.root.context.getColor(it),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            // 텍스트 색상 설정
            binding.diaryCalendarDateTv.setTextColor(
                binding.root.context.getColor(
                    when {
                        calendarDay.isSameDate(selectedDate) -> R.color.main // 선택된 날짜
                        calendarDay.toYearMonth() == visibleMonth -> R.color.main_text // 현재 달
                        else -> R.color.text_placeholder // 다른 달
                    }
                )
            )

            // 클릭 리스너 설정
            binding.root.setOnClickListener {
                updateSelectedDateView(binding.diaryCalendarDateTv, calendarDay)
                listener.onCalendarDayClick(calendarDay)
            }

            // 아이템 높이 설정
            val height = dpToPx(if (isBottomSheetOpen) OPEN_HEIGHT else CLOSE_HEIGHT, binding.root.context)
            binding.root.layoutParams = binding.root.layoutParams.apply {
                this.height = height
            }
            binding.root.requestLayout()
        }

        fun setSelected(isSelected: Boolean) {
            binding.diaryCalendarDateTv.setTextColor(
                binding.root.context.getColor(
                    if (isSelected) R.color.main
                    else if (binding.calendarDay?.toYearMonth() == visibleMonth) R.color.main_text
                    else R.color.text_placeholder
                )
            )
        }

        fun updateItem(isOpening: Boolean) {
            // 높이 조정
            val height = dpToPx(if (isOpening) OPEN_HEIGHT else CLOSE_HEIGHT, binding.root.context)
            binding.root.layoutParams = binding.root.layoutParams.apply {
                this.height = height
            }
            binding.root.requestLayout()
        }

        fun updateItemWithAnimate(isOpening: Boolean) {
            // 높이 조정
            val fromHeight = binding.root.height
            val toHeight = dpToPx(if (isOpening) OPEN_HEIGHT else CLOSE_HEIGHT, binding.root.context)

            val valueAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
            valueAnimator.addUpdateListener { animator ->
                val layoutParams = binding.root.layoutParams
                layoutParams.height = animator.animatedValue as Int
                binding.root.layoutParams = layoutParams
            }
            valueAnimator.duration = ANIMATION_DURATION
            valueAnimator.start()
        }

        private fun updateSelectedDateView(newDateView: TextView, newDate: CalendarDay) {
            // 이전 선택된 날짜 초기화
            selectedDate?.let { oldDate ->
                val oldIndex = items.indexOfFirst { it.isSameDate(oldDate) }
                if (oldIndex != -1) {
                    notifyItemChanged(oldIndex, PAYLOAD_DESELECT)
                }
            }

            selectedDate = newDate
            selectedDateView = newDateView
            newDateView.setTextColor(newDateView.context.getColor(R.color.main))

            val newIndex = items.indexOfFirst { it.isSameDate(newDate) }
            if (newIndex != -1) {
                notifyItemChanged(newIndex, PAYLOAD_SELECT)
            }
        }
    }

    interface OnCalendarListener {
        fun onCalendarDayClick(calendarDay: CalendarDay)
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        const val ANIMATION_DURATION = 170L
        const val OPEN_HEIGHT = 56
        const val CLOSE_HEIGHT = 84
        const val PAYLOAD_SELECT = "select"
        const val PAYLOAD_DESELECT = "deselect"
    }
}
