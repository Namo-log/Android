package com.example.namo.data.entity.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.namo.R

@Entity(tableName = "diaryTable")
data class Diary(
    @PrimaryKey(autoGenerate = false) val diaryLocalId: Int = 0,
    var content: String = "",
    var images: List<String?>? = null,
    @ColumnInfo(name = "diary_upload")
    var isUpload: Int = 0,
    @ColumnInfo(name = "diary_state")
    var state: String = R.string.event_current_default.toString(),
    @ColumnInfo(name = "scheduleId")
    val diaryServerId: Int = 0,
)