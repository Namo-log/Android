package com.example.namo.data.entity.home

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName="category_table")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryIdx: Int = 0,
    var name : String = "",
    var color : Int = 0,
    var share : Boolean = false
) : Serializable
