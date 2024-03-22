package com.mongmong.namo.data.local.entity.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.RoomState

@Entity(tableName = "diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val diaryId: Long = 0L,  // roomDB eventId
    var serverId: Long = 0L, // server scheduleId
    var content: String?,
    var images: List<String>? = null,
    @ColumnInfo(name = "diary_state")
    var state: String = RoomState.DEFAULT.state,
    @ColumnInfo(name = "diary_upload")
    var isUpload: Boolean = false,
    var isHeader: Boolean = false
)