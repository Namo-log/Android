package com.mongmong.namo.presentation.ui.diary.adapter

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DiaryImageItemDecoration(context: Context, spaceDp: Int) : RecyclerView.ItemDecoration() {
    private val space: Int = dpToPx(context, spaceDp)
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // 첫 번째 항목이 아니면 왼쪽 간격을 추가
        if (position != 0) {
            outRect.left = space
        }

        // 마지막 항목이 아니면 오른쪽 간격을 추가
        if (position != itemCount - 1) {
            outRect.right = space
        }
    }
}
