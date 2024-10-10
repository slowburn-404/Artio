package dev.borisochieng.artio.ui.screens.drawingboard.data

import dev.borisochieng.database.database.Sketch

sealed class SketchPadActions {

	data class SaveSketch(val sketch: dev.borisochieng.database.database.Sketch) : SketchPadActions()

	data class UpdateSketch(
        val paths: List<dev.borisochieng.model.PathProperties>,
        val texts: List<dev.borisochieng.model.TextProperties>
	) : SketchPadActions()

	data object CheckIfUserIsLoggedIn : SketchPadActions()

	data object SketchClosed : SketchPadActions()
}