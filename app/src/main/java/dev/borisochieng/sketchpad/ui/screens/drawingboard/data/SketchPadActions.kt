package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

import dev.borisochieng.sketchpad.database.Sketch

sealed class SketchPadActions {

	data class SaveSketch(val sketch: Sketch) : SketchPadActions()

	data class UpdateSketch(val paths: List<PathProperties>) : SketchPadActions()

	data object CheckIfUserIsLoggedIn : SketchPadActions()

	data object SketchClosed : SketchPadActions()

}