package com.mongmong.namo.presentation.state

enum class FilterType(val type: String, val request: String?) {
    NONE("검색 필터 없음", null),
    TITLES("일정 제목", "ScheduleName"),
    CONTENTS("기록 내용", "DiaryContent"),
    PARTICIPANTS("참석자", "MemberNickname")
}