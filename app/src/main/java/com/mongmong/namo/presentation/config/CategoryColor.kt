package com.mongmong.namo.presentation.config

import android.content.res.ColorStateList
import android.graphics.Color

enum class PaletteType { DEFAULT_4, BASIC_PALETTE }

/** 내부 색상 저장 용도 */
enum class CategoryColor(val paletteType: PaletteType, val paletteId: Int, val hexColor: String) {
    /** 기본 색상 */
    SCHEDULE(PaletteType.DEFAULT_4, 1, "#DE8989"),
    YELLOW(PaletteType.DEFAULT_4, 2, "#E1B000"),
    BLUE(PaletteType.DEFAULT_4, 3, "#5C8596"),
    MOIM(PaletteType.DEFAULT_4, 4, "#DA6022"),
    /** 기본 팔레트 */
    DEFAULT_PALETTE_COLOR1(PaletteType.BASIC_PALETTE, 5, "#EB5353"),
    DEFAULT_PALETTE_COLOR2(PaletteType.BASIC_PALETTE, 6, "#EC9B3B"),
    DEFAULT_PALETTE_COLOR3(PaletteType.BASIC_PALETTE, 7, "#FBCB0A"),
    DEFAULT_PALETTE_COLOR4(PaletteType.BASIC_PALETTE, 8, "#96BB7C"),
    DEFAULT_PALETTE_COLOR5(PaletteType.BASIC_PALETTE, 9, "#5A8F7B"),
    DEFAULT_PALETTE_COLOR6(PaletteType.BASIC_PALETTE, 10, "#82C4C3"),
    DEFAULT_PALETTE_COLOR7(PaletteType.BASIC_PALETTE, 11, "#187498"),
    DEFAULT_PALETTE_COLOR8(PaletteType.BASIC_PALETTE, 12, "#8571BF"),
    DEFAULT_PALETTE_COLOR9(PaletteType.BASIC_PALETTE, 13, "#E36488"),
    DEFAULT_PALETTE_COLOR10(PaletteType.BASIC_PALETTE, 14, "#858585");

    companion object {
        // enum class의 모든 hexColor 반환
        fun getAllColors(): ArrayList<String> {
            return values().map { it.hexColor } as ArrayList<String>
        }

        // PaletteType에 해당하는 리스트 반환
        fun findPaletteByPaletteType(paletteType: PaletteType): ArrayList<CategoryColor> {
            return values().filter { it.paletteType == paletteType } as ArrayList<CategoryColor>
        }

        // 팔레트의 hexColor만 반환하기
        fun findColorsByPaletteType(paletteType: PaletteType): ArrayList<String> {
            return findPaletteByPaletteType(paletteType).map { it.hexColor } as ArrayList<String>
        }

        // paletteId로 CategoryColor 찾기
        fun findCategoryColorByPaletteId(paletteId: Int): CategoryColor { //
            return values().find { it.paletteId == paletteId } ?: SCHEDULE // 못 찾으면 기본 색상으로
        }

        // paletteId로 hexColor를 반환
        fun convertPaletteIdToHexColor(paletteId: Int): String {
            return findCategoryColorByPaletteId(paletteId).hexColor
        }

        // 카테고리 색상뷰 배경색 바꾸기 용도
        fun convertPaletteIdToColorStateList(paletteId: Int) : ColorStateList {
            return ColorStateList.valueOf(convertHexToInt(convertPaletteIdToHexColor(paletteId)))
        }


        private fun convertHexToInt(hexColor: String): Int {
            return Color.parseColor(hexColor)
        }
    }
}