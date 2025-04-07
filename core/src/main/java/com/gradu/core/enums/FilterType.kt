package com.gradu.core.enums

enum class FilterType(val type: String, val request: String?) {
    NONE("검색 필터 없음", null),
    TITLES("일정 제목", "schedule_name"),
    CONTENTS("기록 내용", "diary_content"),
    PARTICIPANTS("참석자", "member_nickname")
}