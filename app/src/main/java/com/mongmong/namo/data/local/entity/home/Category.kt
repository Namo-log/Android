package com.mongmong.namo.data.local.entity.home

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.RoomState
import java.io.Serializable

@Entity(tableName="category_table")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    var name : String = "",
    var paletteId : Int = 0,
    var share : Boolean = false,
    var active : Boolean = true,
    var isUpload : Boolean = false,
    var state : String = RoomState.DEFAULT.state,
    var serverId : Long = 0L
) : Serializable

data class CategoryForUpload(
    var name : String = "",
    var color : Int = 0,
    var share : Boolean = false
)