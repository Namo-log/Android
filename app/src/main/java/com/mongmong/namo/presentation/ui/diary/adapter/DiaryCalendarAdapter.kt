package com.mongmong.namo.presentation.ui.diary.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryCalendarDateBinding

data class CalendarDay(val date: String, val year: Int, val month: Int)

class DiaryCalendarAdapter(
    private val recyclerView: RecyclerView,
    private val items: List<CalendarDay>,
    private val listener: OnCalendarDayClickListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var isOpeningBottomSheet: Boolean = false
    private var shouldAnimate: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, isOpeningBottomSheet)
    }

    override fun getItemCount(): Int = items.size

    fun updateBottomSheetState(isOpened: Boolean) {
        this.isOpeningBottomSheet = isOpened
        this.shouldAnimate = true

        // RecyclerView에서 ViewHolder를 찾아 직접 업데이트
        for (i in 0 until itemCount) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.updateHeight(isOpened)
        }

        this.shouldAnimate = false // 애니메이션 후에는 다시 false로 설정
    }

    inner class ViewHolder(val binding: ItemDiaryCalendarDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay, isOpeningBottomSheet: Boolean) {
            binding.calendarDay = calendarDay
            updateHeight(isOpeningBottomSheet)

            binding.root.setOnClickListener {
                listener.onCalendarDayClick(calendarDay)
            }
        }

        fun updateHeight(isOpening: Boolean) {
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
        }

        fun animateHeightChange(fromHeight: Int, toHeight: Int) {
            val valueAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
            valueAnimator.apply {
                addUpdateListener { animator ->
                    val layoutParams = binding.root.layoutParams
                    layoutParams.height = animator.animatedValue as Int
                    binding.root.layoutParams = layoutParams
                    binding.root.requestLayout() // 뷰의 레이아웃을 강제로 갱신
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
