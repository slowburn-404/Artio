package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions

data class PathProperties(
	val id: String = "",
	val alpha: Float = 1f,
	val color: Color = Color.Black,
	val eraseMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	val strokeWidth: Float = 10f
)
