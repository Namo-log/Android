package com.mongmong.namo.presentation.ui.diary.moimDiary


import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.presentation.ui.diary.moimDiary.adapter.MoimActivityRVAdapter
import java.lang.Float.max
import java.lang.Float.min


class ItemTouchHelperCallback : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {

    private var currentPosition: Int? = null
    private var previousPosition: Int? = null
    private var currentDx = 0f

    // 삭제 버튼 width를 넣을 값
    private var clamp = 0f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // 순서 조정 완료 후 투명도 다시 1f로 변경
        viewHolder.itemView.alpha = 1.0f
        getDefaultUIUtil().clearView(getView(viewHolder))
        previousPosition = viewHolder.absoluteAdapterPosition
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            viewHolder?.let {
                // 삭제 버튼 width 획득
                clamp = getViewWidth(viewHolder) + 20f
                // 현재 뷰홀더
                currentPosition = viewHolder.bindingAdapterPosition
                getDefaultUIUtil().onSelected(getView(it))
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)

            val x = clampViewPositionHorizontal(view, dX, isClamped, isCurrentlyActive)

            currentDx = x

            getDefaultUIUtil().onDraw(
                c, recyclerView, view, x, dY, actionState, isCurrentlyActive
            )
        }
    }

    // 삭제버튼 width 구하는 함수
    private fun getViewWidth(viewHolder: RecyclerView.ViewHolder): Float {
        val viewWidth = (viewHolder as MoimActivityRVAdapter.Holder).binding.removeView.width
        return viewWidth.toFloat()
    }

    // swipe될 뷰 (우리가 스와이프할 시 움직일 화면)
    private fun getView(viewHolder: RecyclerView.ViewHolder): View {
        return (viewHolder as MoimActivityRVAdapter.Holder).binding.groupLayout
    }

    // view의 tag로 스와이프 고정됐는지 안됐는지 확인 (고정 == true)
    private fun getTag(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder.itemView.tag as? Boolean ?: false
    }

    // view의 tag에 스와이프 고정됐으면 true, 안됐으면 false 값 넣기
    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) {
        viewHolder.itemView.tag = isClamped
    }

    // 스와이프 될 가로(수평평) 길이
    private fun clampViewPositionHorizontal(
        view: View,
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ): Float {
        val maxSwipe: Float = -clamp * 1.5f

        val right = 0f

        val x = if (isClamped) {
            if (isCurrentlyActive) dX - clamp else -clamp
        } else dX

        return min(
            max(maxSwipe, x),
            right
        )
    }

    // 사용자가 Swipe 동작으로 간주할 최소 속도
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    // 사용자가 스와이프한 것으로 간주할 view 이동 비율
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        setTag(viewHolder, currentDx <= -clamp)
        return 2f
    }

    // 다른 아이템 클릭 시 기존 swipe 되어있던 아이템 원상 복구
    fun removePreviousClamp(recyclerView: RecyclerView) {
        if (currentPosition == previousPosition)
            return
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
        }
    }

    fun resetPreviousClamp(recyclerView: RecyclerView) {
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
        }
    }

}
