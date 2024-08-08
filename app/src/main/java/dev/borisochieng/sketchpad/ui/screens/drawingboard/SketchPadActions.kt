package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import dev.borisochieng.sketchpad.database.Sketch

sealed class SketchPadActions {

	data class SaveSketch(val sketch: Sketch) : SketchPadActions()

	data class UpdateSketch(val art: Bitmap) : SketchPadActions()

	data class DeleteSketch(val sketch: Sketch) : SketchPadActions()

}