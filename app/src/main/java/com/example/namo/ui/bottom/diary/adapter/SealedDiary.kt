package com.example.namo.ui.bottom.diary.adapter

import com.example.namo.R
import com.example.namo.data.entity.home.Event

sealed class TaskListItem {
    abstract val task: Event
    abstract val layoutId: Int

    data class Header(
        override val task: Event,
        override val layoutId: Int = VIEW_TYPE
    ) : TaskListItem() {

        companion object {
            const val VIEW_TYPE = R.layout.item_diary_list
        }
    }

    data class Item(
        override val task: Event,
        override val layoutId: Int = VIEW_TYPE
    ) : TaskListItem() {

        companion object {
            const val VIEW_TYPE = R.layout.item_diary_date_list
        }
    }
}