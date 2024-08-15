package dev.borisochieng.sketchpad.ui.screens.home

import dev.borisochieng.sketchpad.database.Sketch

sealed class HomeActions {

	data class BackupSketch(val sketch: Sketch) : HomeActions()

	data class RenameSketch(val sketch: Sketch) : HomeActions()

	data class DeleteSketch(val sketch: Sketch) : HomeActions()

	data object CheckIfUserIsLogged : HomeActions()

	data object ClearFeedback : HomeActions()

}