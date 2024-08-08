package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import dev.borisochieng.sketchpad.database.Sketch
import io.ak1.drawbox.PathWrapper

sealed class SketchPadActions {

	data class SaveSketch(val sketch: Sketch) : SketchPadActions()

	data class UpdateSketch(
		val art: Bitmap,
		val backgroundColor: Color,
		val paths: List<PathWrapper>
	) : SketchPadActions()

	data class DeleteSketch(val sketch: Sketch) : SketchPadActions()

}