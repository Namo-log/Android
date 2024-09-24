package com.mongmong.namo.presentation.ui.diary

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DiaryCalendarItemDecoration(
    private val topMargin: Float,
    private val bottomMargin: Float
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // 첫 번째 아이템에 topMargin 추가
        if (position in 0..6) {
            outRect.top = topMargin.toInt()
        }

        // 마지막 아이템에 bottomMargin 추가
        if (position in itemCount - 1 .. itemCount - 7) {
            outRect.bottom = bottomMargin.toInt()
        }
    }
}
