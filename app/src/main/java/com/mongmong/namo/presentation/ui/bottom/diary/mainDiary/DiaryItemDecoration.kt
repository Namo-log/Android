package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DiaryItemDecoration(private val context: Context) : RecyclerView.ItemDecoration() {
    private val bottomPadding = dpToPx(context, 15) // 마지막 아이템 아래에 15dp

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = state.itemCount
        val itemPosition = parent.getChildAdapterPosition(view)


        if (itemPosition == itemCount - 1) {
            // 마지막 아이템 아래에 공백 추가
            outRect.bottom = bottomPadding
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}
