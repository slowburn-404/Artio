package dev.borisochieng.sketchpad.ui.screens.drawingboard.alt

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PathProperties(
	var alpha: Float = 1f,
	var color: Color = Color.Black,
	var eraseMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	var strokeWidth: Float = 10f
)
