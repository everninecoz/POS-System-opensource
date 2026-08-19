package com.posopensrc.printer

enum class PaperSize(
    val widthMm: Int,
    val maxCharacters: Int,
    val displayName: String
) {
    WIDTH_58MM(58, 32, "58mm (Mini)"),
    WIDTH_72MM(72, 42, "72mm (Sedang)"),
    WIDTH_80MM(80, 48, "80mm (Standar)");

    companion object {
        fun fromWidthMm(widthMm: Int): PaperSize {
            return when {
                widthMm <= 58 -> WIDTH_58MM
                widthMm <= 72 -> WIDTH_72MM
                else -> WIDTH_80MM
            }
        }

        fun fromMaxCharacters(maxChars: Int): PaperSize {
            return when {
                maxChars <= 32 -> WIDTH_58MM
                maxChars <= 42 -> WIDTH_72MM
                else -> WIDTH_80MM
            }
        }
    }
}
