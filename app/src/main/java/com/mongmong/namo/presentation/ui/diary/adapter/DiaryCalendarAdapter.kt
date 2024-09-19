package com.mongmong.namo.presentation.ui.diary.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ItemDiaryCalendarDateBinding
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.presentation.utils.DiaryDateConverter.toYearMonth

class DiaryCalendarAdapter(
    private val recyclerView: RecyclerView,
    private val items: List<CalendarDay>,
    private val listener: OnCalendarDayClickListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var diaryDates: MutableMap<String, Set<String>> = mutableMapOf() // "yyyy-MM": [기록된 날짜들]
    private var isOpeningBottomSheet: Boolean = false

    fun updateDiaryDates(yearMonth: String, diaryDates: Set<String>) {
        this.diaryDates[yearMonth] = diaryDates

        items.forEachIndexed { index, calendarDay ->
            if (calendarDay.toYearMonth() == yearMonth && diaryDates.contains(calendarDay.date.toString())) {
                notifyItemChanged(index)
            }
        }
    }

    fun updateBottomSheetState(isOpened: Boolean) {
        this.isOpeningBottomSheet = isOpened

        // 화면에 보이는 아이템들의 위치를 가져옴
        val layoutManager = recyclerView.layoutManager ?: return
        val firstVisibleItemPosition = (layoutManager as? GridLayoutManager)?.findFirstVisibleItemPosition()
            ?: (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
            ?: 0
        val lastVisibleItemPosition = (layoutManager as? GridLayoutManager)?.findLastVisibleItemPosition()
            ?: (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
            ?: itemCount - 1

        // 화면에 보이는 아이템들에 대해서는 애니메이션 적용
        for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.animateHeightChange(isOpened)
        }

        // 화면에 보이지 않는 아이템들은 높이 변경을 직접 적용하도록 notify
        if (firstVisibleItemPosition > 0) {
            notifyItemRangeChanged(0, firstVisibleItemPosition)
        }
        if (lastVisibleItemPosition < itemCount - 1) {
            notifyItemRangeChanged(lastVisibleItemPosition + 1, itemCount - lastVisibleItemPosition - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.updateItem(isOpeningBottomSheet)
    }

    fun getItemAtPosition(position: Int): CalendarDay? {
        return if (position in items.indices) items[position] else null
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemDiaryCalendarDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.calendarDay = calendarDay

            val hasDiary = diaryDates[calendarDay.toYearMonth()]?.contains(calendarDay.date.toString()) ?: false
            binding.diaryCalendarHasDiaryIndicatorIv.visibility = if (hasDiary) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                listener.onCalendarDayClick(calendarDay)
            }
        }

        fun updateItem(isOpening: Boolean) {
            val height = dpToPx(if (isOpening) OPEN_HEIGHT else CLOSE_HEIGHT, binding.root.context)
            binding.root.layoutParams = binding.root.layoutParams.apply {
                this.height = height
            }
            binding.root.requestLayout()

            val indicatorImage = if (isOpening) R.drawable.ic_calendar else R.drawable.img_mongi_default
            binding.diaryCalendarHasDiaryIndicatorIv.setImageResource(indicatorImage)
        }

        fun animateHeightChange(isOpening: Boolean) {
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

            val indicatorImage = if (isOpening) R.drawable.ic_calendar else R.drawable.img_mongi_default
            binding.diaryCalendarHasDiaryIndicatorIv.setImageResource(indicatorImage)
        }
    }

    interface OnCalendarDayClickListener {
        fun onCalendarDayClick(calendarDay: CalendarDay)
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        const val ANIMATION_DURATION = 170L
        const val OPEN_HEIGHT = 56
        const val CLOSE_HEIGHT = 84
    }
}
