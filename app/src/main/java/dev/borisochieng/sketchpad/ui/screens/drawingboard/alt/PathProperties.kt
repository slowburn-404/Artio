package dev.borisochieng.sketchpad.ui.screens.drawingboard.alt

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID.randomUUID

data class PathProperties(
	val id: String = randomUUID().toString(),
	val alpha: Float = 1f,
	val color: Color = Color.Black,
	val eraseMode: Boolean = false,
	val start: Offset = Offset.Zero,
	val end: Offset = Offset.Zero,
	val strokeWidth: Float = 10f
)
