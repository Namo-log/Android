package com.mongmong.namo.domain.model

data class Friend(
    val userid: Long,
    val profileUrl: String,
    val nickname: String,
    val introduction: String,
    val isFavorite: Boolean // 즐겨찾기 여부
)