package com.example.namo.data.entity.group

import android.media.Image
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="group_table")
data class Group(
    @PrimaryKey(autoGenerate = true)
    var groupIdx: Int = 0,

    @ColumnInfo(name = "group_title")
    var title: String = "", // 그룹명

//    @ColumnInfo(name = "group_image")
//    var coverImage: Uri? = null, // 그룹 리스트 이미지 표시

//    @ColumnInfo(name = "group_memberNum")
//    var memberNum: Int = 0, // 참여자 수

    @ColumnInfo(name = "group_member")
    var member: List<String>? // 멤버 이름
)