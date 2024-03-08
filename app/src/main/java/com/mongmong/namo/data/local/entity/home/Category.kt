package com.mongmong.namo.data.local.entity.home

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R
import java.io.Serializable

@Entity(tableName="category_table")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryIdx: Long = 0,
    var name : String = "",
    var color : Int = 0,
    var share : Boolean = false,
    var active : Boolean = true,
    var isUpload : Int = 0,
    var state : String = R.string.event_current_default.toString(),
    var serverIdx : Long = 0L
) : Serializable

data class CategoryForUpload(
    var name : String = "",
    var color : Int = 0,
    var share : Boolean = false
)