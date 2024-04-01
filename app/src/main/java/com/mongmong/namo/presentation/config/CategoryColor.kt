package com.mongmong.namo.presentation.config

import android.content.res.ColorStateList
import android.graphics.Color
import com.mongmong.namo.R

enum class BelongType { BELONG1, BELONG2 }

/** 내부 색상 저장 용도 */
enum class CategoryColor(val belongType: BelongType, val paletteId: Int, val hexColor: String, val intColor: Int) {
    /** 기본 색상 */
    SCHEDULE(BelongType.BELONG1, 1, "#DE8989", R.color.schedule),
    YELLOW(BelongType.BELONG1, 2, "#E1B000", R.color.schedule_plan),
    BLUE(BelongType.BELONG1, 3, "#5C8596", R.color.schedule_parttime),
    MOIM(BelongType.BELONG1, 4, "#DA6022", R.color.schedule_group),
    /** 기본 팔레트 */
    DEFAULT_PALETTE_COLOR1(BelongType.BELONG2, 5, "#EB5353", R.color.palette1),
    DEFAULT_PALETTE_COLOR2(BelongType.BELONG2, 6, "#EC9B3B", R.color.palette2),
    DEFAULT_PALETTE_COLOR3(BelongType.BELONG2, 7, "#FBCB0A", R.color.palette3),
    DEFAULT_PALETTE_COLOR4(BelongType.BELONG2, 8, "#96BB7C", R.color.palette4),
    DEFAULT_PALETTE_COLOR5(BelongType.BELONG2, 9, "#5A8F7B", R.color.palette5),
    DEFAULT_PALETTE_COLOR6(BelongType.BELONG2, 10, "#82C4C3", R.color.palette6),
    DEFAULT_PALETTE_COLOR7(BelongType.BELONG2, 11, "#187498", R.color.palette7),
    DEFAULT_PALETTE_COLOR8(BelongType.BELONG2, 12, "#8571BF", R.color.palette8),
    DEFAULT_PALETTE_COLOR9(BelongType.BELONG2, 13, "#E36488", R.color.palette9),
    DEFAULT_PALETTE_COLOR10(BelongType.BELONG2, 14, "#858585", R.color.palette10);

    companion object {
        // BelongType에 해당하는 리스트 반환
        fun findPalettes(belongType: BelongType): ArrayList<CategoryColor> {
            return values().filter { it.belongType == belongType } as ArrayList<CategoryColor>
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