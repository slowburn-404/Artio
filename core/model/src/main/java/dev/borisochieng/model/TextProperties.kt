package dev.borisochieng.model

import androidx.compose.ui.geometry.Offset

data class TextProperties(
	val id: String = "",
	val text: String = "",
	val offset: Offset = Offset(100f, 100f),
	val scale: Float = 1f,
	val rotation: Float = 0f
)
