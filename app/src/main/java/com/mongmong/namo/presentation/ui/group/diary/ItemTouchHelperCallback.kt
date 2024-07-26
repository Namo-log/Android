package com.mongmong.namo.presentation.ui.group.diary

import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.presentation.ui.group.diary.adapter.MoimActivityRVAdapter
import kotlin.math.max
import kotlin.math.min

class ItemTouchHelperCallback : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {

    private var currentPosition: Int? = null
    private var previousPosition: Int? = null
    private var currentDx = 0f
    private var clamp = 0f

    // 드래그 이동을 허용하지 않음 (onMove는 사용하지 않음)
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    // 뷰가 선택 해제될 때 호출 (예: 스와이프 후)
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
        getDefaultUIUtil().clearView(getView(viewHolder))
        previousPosition = viewHolder.absoluteAdapterPosition
        setDeleteButtonEnabled(viewHolder, false) // 삭제 버튼 클릭 비활성화
    }

    // 뷰가 선택될 때 호출 (예: 스와이프 시작)
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            viewHolder?.let {
                // 삭제 버튼의 폭을 구함
                clamp = getViewWidth(viewHolder) + 20f
                currentPosition = viewHolder.bindingAdapterPosition
                getDefaultUIUtil().onSelected(getView(it))
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    // 스와이프 중에 호출됨
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

            // 삭제 버튼이 보일 때만 클릭 가능하도록 설정
            setDeleteButtonEnabled(viewHolder, x <= -clamp)

            getDefaultUIUtil().onDraw(
                c, recyclerView, view, x, dY, actionState, isCurrentlyActive
            )
        }
    }

    // 삭제 버튼의 폭을 구하는 메서드
    private fun getViewWidth(viewHolder: RecyclerView.ViewHolder): Float {
        val viewWidth = (viewHolder as MoimActivityRVAdapter.ViewHolder).binding.activityDeleteLl.width
        return viewWidth.toFloat()
    }

    // 스와이프할 뷰를 반환 (activityLayout 뷰)
    private fun getView(viewHolder: RecyclerView.ViewHolder): View {
        return (viewHolder as MoimActivityRVAdapter.ViewHolder).binding.activityLayout
    }

    // 뷰의 tag를 통해 스와이프 고정 여부 확인
    private fun getTag(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder.itemView.tag as? Boolean ?: false
    }

    // 뷰의 tag에 스와이프 고정 여부 설정
    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) {
        viewHolder.itemView.tag = isClamped
    }

    // 수평 이동을 제한 (삭제 버튼 보일 때만 이동 고정)
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
        return min(max(maxSwipe, x), right)
    }

    // 사용자가 스와이프 동작으로 간주할 최소 속도 조정
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    // 스와이프 후 호출 (실제로는 아무 동작도 하지 않음)
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    // 사용자가 스와이프한 것으로 간주할 view 이동 비율 조정
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        setTag(viewHolder, currentDx <= -clamp)
        return 2f
    }

    // 다른 아이템 클릭 시 이전 스와이프된 아이템 초기화
    fun removePreviousClamp(recyclerView: RecyclerView) {
        if (currentPosition == previousPosition)
            return
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
            setDeleteButtonEnabled(viewHolder, false) // 삭제 버튼 클릭 비활성화
        }
    }

    // 모든 이전 스와이프된 아이템 초기화
    fun resetPreviousClamp(recyclerView: RecyclerView) {
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
            setDeleteButtonEnabled(viewHolder, false) // 삭제 버튼 클릭 비활성화
        }
    }

    // 삭제 버튼 클릭 가능 여부 설정
    private fun setDeleteButtonEnabled(viewHolder: RecyclerView.ViewHolder, enabled: Boolean) {
        val deleteButton = (viewHolder as MoimActivityRVAdapter.ViewHolder).binding.activityDeleteLl
        deleteButton.isClickable = enabled
        deleteButton.isEnabled = enabled
        Log.d("ItemTouchHelper", "$enabled")
    }
}
