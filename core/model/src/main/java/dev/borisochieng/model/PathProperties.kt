package dev.borisochieng.model

import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PathProperties(
	val id: String = "",
	val alpha: Float = 1f,
	val color: Color = Color.Black,
	val eraseMode: Boolean = false,
	val textMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	val strokeWidth: Float = 10f
)
