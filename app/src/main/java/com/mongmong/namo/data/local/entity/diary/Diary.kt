package com.mongmong.namo.data.local.entity.diary

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.presentation.state.RoomState
import com.mongmong.namo.BR

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val diaryId: Long = 0L,  // roomDB scheduleId
    var scheduleServerId: Long = 0L, // server scheduleId
    private var _content: String? = null,
    var images: List<String>? = null,
    var state: String = RoomState.DEFAULT.state,
    var isUpload: Boolean = false,
    var isHeader: Boolean = false
) : BaseObservable() {
    @get:Bindable
    var content: String?
        get() = _content
        set(value) {
            _content = value
            notifyPropertyChanged(BR.content)
        }
}
