package com.mongmong.namo.presentation.ui.home.schedule.map.data

/** 키워드로 장소 검색 */
data class ResultSearchPlace(
    var documents : List<Place>
)

data class Place(
    var id : String = "", // 장소 ID
    var place_name : String = "없음", // 장소명, 업체명
    var address_name : String = "", // 전체 지번 주소
    var road_address_name : String = "", // 전체 도로명 주소
    var x : Double = 0.0, // longitude(경도)
    var y : Double = 0.0 // latitude(위도)
)

/** 좌표로 주소 변환 */
data class ResultCoord2Address(
    val documents: List<Document>
)

data class Document(
    val address: Address,
    val road_address: RoadAddress?
)

data class Address(
    val address_name: String, // 전체 지번 주소
)
data class RoadAddress(
    val address_name: String, // 전체 도로명 주소
)