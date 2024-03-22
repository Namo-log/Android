package com.mongmong.namo.data.local.entity.diary

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.RoomState

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val diaryId: Long = 0L,  // roomDB scheduleId
    var serverId: Long = 0L, // server scheduleId
    var content: String?,
    var images: List<String>? = null,
    var state: String = RoomState.DEFAULT.state,
    var isUpload: Boolean = false,
    var isHeader: Boolean = false
)