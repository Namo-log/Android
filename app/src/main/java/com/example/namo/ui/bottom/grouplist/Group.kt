package com.example.namo.ui.bottom.grouplist

data class Group(

    var title : String = "", //그룹명
    var coverImg : Int? = null , //그룹 리스트 표지
    var num : Int? = null, //참여자 수
    var member : String = "" //멤버 이름

)