package dev.borisochieng.artio.ui.screens.home

import dev.borisochieng.artio.database.Sketch

sealed class HomeActions {

	data class BackupSketch(val sketch: Sketch) : HomeActions()

	data class RenameSketch(val sketch: Sketch) : HomeActions()

	data class DeleteSketch(val sketch: Sketch) : HomeActions()

	data object CheckIfUserIsLogged : HomeActions()

	data object Refresh : HomeActions()

	data object ClearFeedback : HomeActions()

}