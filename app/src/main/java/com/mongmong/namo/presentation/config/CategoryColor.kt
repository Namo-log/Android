package com.mongmong.namo.presentation.config

enum class BelongType { BELONG1, BELONG2 }

enum class CategoryColor(val belongType: BelongType, val paletteId: Int, val hexColor: String) {
    /** 기본 색상 */
    SCHEDULE(BelongType.BELONG1, 1, "#DE8989"),
    YELLOW(BelongType.BELONG1, 2, "#E1B000"),
    BLUE(BelongType.BELONG1, 3, "#5C8596"),
    MOIM(BelongType.BELONG1, 4, "#DA6022"),
    /** 기본 팔레트 */
    DEFAULT_PALETTE_COLOR1(BelongType.BELONG2, 5, "#EB5353"),
    DEFAULT_PALETTE_COLOR2(BelongType.BELONG2, 6, "#EC9B3B"),
    DEFAULT_PALETTE_COLOR3(BelongType.BELONG2, 7, "#FBCB0A"),
    DEFAULT_PALETTE_COLOR4(BelongType.BELONG2, 8, "#96BB7C"),
    DEFAULT_PALETTE_COLOR5(BelongType.BELONG2, 9, "#5A8F7B"),
    DEFAULT_PALETTE_COLOR6(BelongType.BELONG2, 10, "#82C4C3"),
    DEFAULT_PALETTE_COLOR7(BelongType.BELONG2, 11, "#187498"),
    DEFAULT_PALETTE_COLOR8(BelongType.BELONG2, 12, "#8571BF"),
    DEFAULT_PALETTE_COLOR9(BelongType.BELONG2, 13, "#E36488"),
    DEFAULT_PALETTE_COLOR10(BelongType.BELONG2, 14, "#858585");

    companion object {
        fun convertPaletteIdToHexColor(paletteId: Int): String { // hexColor를 반환
            return CategoryColor.values().find { it.paletteId == paletteId }?.hexColor ?: ""
        }
    }
}