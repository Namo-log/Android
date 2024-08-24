package com.mongmong.namo.presentation.ui.diary.adapter

import android.animation.Animator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryCalendarBinding

data class CalendarDay(val date: String, val year: Int, val month: Int)

class DiaryCalendarAdapter(
    private val items: List<CalendarDay>,
    private val listener: OnCalendarDayClickListener
) : RecyclerView.Adapter<DiaryCalendarAdapter.ViewHolder>() {

    private var isOpeningBottomSheet: Boolean = false
    private var shouldAnimate: Boolean = false // 애니메이션 적용 여부 플래그

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiaryCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        val fromHeight = if (isOpeningBottomSheet) dpToPx(84, holder.binding.root.context) else dpToPx(56, holder.binding.root.context)
        val toHeight = if (isOpeningBottomSheet) dpToPx(56, holder.binding.root.context) else dpToPx(84, holder.binding.root.context)

        if (shouldAnimate) {
            Log.d("DiaryCalendarAdapter", "shouldAnimate : $toHeight")
            holder.animateHeightChange(fromHeight, toHeight)
        } else {
            Log.d("DiaryCalendarAdapter", "should not Animate : $toHeight")
            // 애니메이션 없이 높이 조정
            val layoutParams = holder.binding.root.layoutParams
            layoutParams.height = toHeight
            holder.binding.root.layoutParams = layoutParams
        }

        Log.d("DiaryCalendarAdapter", "${holder.binding.itemDiaryCalendarRootCl.height}")
    }

    override fun getItemCount(): Int = items.size

    fun updateAllItems(isOpened: Boolean) {
        this.isOpeningBottomSheet = isOpened
        this.shouldAnimate = true // 바텀시트 상태 변경 시 애니메이션 활성화
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDiaryCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(calendarDay: CalendarDay) {
            binding.diaryCalendarDateTv.text = calendarDay.date

            binding.root.setOnClickListener {
                listener.onCalendarDayClick(calendarDay)
            }
        }

        fun animateHeightChange(fromHeight: Int, toHeight: Int) {
            val valueAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
            valueAnimator.apply {
                addUpdateListener { animator ->
                    val layoutParams = binding.root.layoutParams
                    layoutParams.height = animator.animatedValue as Int
                    binding.root.layoutParams = layoutParams
                }
                duration = 170
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        shouldAnimate = false
                    }
                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                })
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
