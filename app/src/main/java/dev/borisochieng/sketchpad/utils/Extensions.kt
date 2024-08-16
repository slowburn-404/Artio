package dev.borisochieng.sketchpad.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATE_PATTERN = "dd/MM/yyyy"
const val VOID_ID = "0000"

object Extensions {

    fun Date.formatDate(): String {
        val style = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        return style.format(this)
    }

    fun String.toDate(): Date? {
        val style = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val style2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            style.parse(this)
        } catch (e: Exception) {
            style2.parse(this)
        }
    }

    fun String.toColor(): Color {
        val cleanHex = if (this.startsWith("#")) this.substring(1) else this
        return when (cleanHex.length) {
            6 -> { // RGB format
                val colorInt = cleanHex.toLong(16).toInt()
                Color(colorInt or 0xFF000000.toInt()) // Add full alpha
            }

            8 -> { // ARGB format
                val colorLong = cleanHex.toLong(16)
                Color((colorLong and 0xFFFFFFFF).toInt())
            }

            else -> {
                throw IllegalArgumentException("Invalid hex color format")
            }
        }
    }

    fun Color.toHexString(): String {
        val argb = this.toArgb()
        return String.format("#%08x", argb)
    }

    fun <T> List<T>.transformList(): List<List<T>> {
        var i = 0
        val list = mutableListOf<List<T>>()
        while (i < size) {
            val tList = mutableListOf<T>()
            tList.add(this[i])
            if (i + 1 < size) {
                tList.add(this[i + 1])
            }
            if (i + 2 < size) {
                tList.add(this[i + 2])
            }
            if (i + 3 < size) {
                tList.add(this[i + 3])
            }
            list.add(tList.toList())
            i += when {
                i + 3 < size -> 4
                i + 2 < size -> 3
                i + 1 < size -> 2
                else -> 1
            }
        }
        return list.toList()
    }

}