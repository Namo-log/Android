package com.gradu.core.enums

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

/** 내부 색상 저장 용도 */
enum class CategoryColor(val colorId: Int, val hexColor: String) {
    NAMO_ORANGE(1, "#DA6022"),
    NAMO_PINK(2, "#DE8989"),
    NAMO_YELLOW(3, "#E1B000"),
    NAMO_BLUE(4, "#5C8596"),
    LIGHT_GRAY(5, "#DADADA"),
    RED(6, "#EB5353"),
    PINK(7, "#FFA192"),
    ORANGE(8, "#EC9B3B"),
    YELLOW(9, "#FFE70F"),
    LIME(10, "#B3DF67"),
    LIGHT_GREEN(11, "#78A756"),
    GREEN(12, "#24794F"),
    CYAN(13, "#5AE0BC"),
    LIGHT_BLUE(14, "#45C1D4"),
    BLUE(15, "#355080"),
    LAVENDER(16, "#8571BF"),
    PURPLE(17, "#833286"),
    MAGENTA(18, "#FF70DE"),
    DARK_GRAY(19, "#9C9C9C"),
    BLACK(20, "#1D1D1D");

    fun toFormattedString(): String {
        return name.lowercase() // 전체 소문자로 변환
            .replace("_", " ") // `_`을 공백으로 변경
            .split(" ") // 단어 단위로 분리
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } } // 첫 글자만 대문자로 변환
    }

    companion object {
        // enum class의 모든 CategoryColor 반환
        fun getAllColors(): ArrayList<CategoryColor> {
            return values().map { it } as ArrayList
        }

        // enum class의 모든 hexColor 반환
        fun getAllHexColors(): ArrayList<String> {
            return values().map { it.hexColor } as ArrayList<String>
        }

        // colorId로 CategoryColor 찾기
        fun findCategoryColorByColorId(paletteId: Int): CategoryColor { //
            return values().find { it.colorId == paletteId } ?: NAMO_ORANGE // 못 찾으면 기본 색상으로
        }

        // colorId로 hexColor를 반환
        fun convertColorIdToHexColor(paletteId: Int): String {
            return findCategoryColorByColorId(paletteId).hexColor
        }

        // 카테고리 색상뷰 배경색 바꾸기 용도
        @JvmStatic
        fun convertColorIdToColorStateList(paletteId: Int) : ColorStateList {
            return ColorStateList.valueOf(convertHexToInt(convertColorIdToHexColor(paletteId)))
        }

        private fun convertHexToInt(hexColor: String): Int {
            return Color.parseColor(hexColor)
        }

        @JvmStatic
        fun convertColorIdToDrawable(paletteId: Int): Drawable {
            val colorHex = convertColorIdToHexColor(paletteId)
            return ColorDrawable(Color.parseColor(colorHex)) // 색상 Drawable 반환
        }
    }
}