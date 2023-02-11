package com.example.namo.data.entity.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groupListEvent")
data class Group(
    @PrimaryKey(autoGenerate = true)
    var groupId: Int = 0,

    @ColumnInfo(name = "group_title")
    var title: String = "", // 그룹명

    @ColumnInfo(name = "group_image")
    var coverImage: Int? = null, // 그룹 리스트 이미지 표시

    @ColumnInfo(name = "group_memberNum")
    var memberNum: Int = 0, // 참여자 수

    @ColumnInfo(name = "group_member")
    var member: String = "" // 멤버 이름
)