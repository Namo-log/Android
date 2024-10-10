package com.mongmong.namo.presentation.ui.community.diary.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MoimActivityItemDecoration(private val spaceHeight: Float) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
            outRect.bottom = spaceHeight.toInt()
        }
    }
}
