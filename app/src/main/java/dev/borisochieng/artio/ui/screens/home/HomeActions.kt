package dev.borisochieng.artio.ui.screens.home

import dev.borisochieng.database.database.Sketch

sealed class HomeActions {

	data class BackupSketch(val sketch: dev.borisochieng.database.database.Sketch) : HomeActions()

	data class RenameSketch(val sketch: dev.borisochieng.database.database.Sketch) : HomeActions()

	data class DeleteSketch(val sketch: dev.borisochieng.database.database.Sketch) : HomeActions()

	data object CheckIfUserIsLogged : HomeActions()

	data object Refresh : HomeActions()

	data object ClearFeedback : HomeActions()

}