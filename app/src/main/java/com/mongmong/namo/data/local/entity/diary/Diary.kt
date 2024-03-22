package com.mongmong.namo.data.local.entity.diary

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val diaryId: Long = 0L,  // roomDB eventId
    var serverId: Long = 0L, // server scheduleId
    var content: String?,
    var images: List<String>? = null,
    var state: String = R.string.event_current_default.toString(),
    var isUpload: Int = 0,
    var isHeader: Boolean = false
)