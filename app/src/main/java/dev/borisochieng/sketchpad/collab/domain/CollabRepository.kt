package dev.borisochieng.sketchpad.collab.domain

import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.flow.Flow

interface CollabRepository {

    suspend fun createSketch(userId: String, title: String, paths: List<DBPathProperties>): FirebaseResponse<BoardDetails>

    suspend fun listenForSketchChanges(userId: String, boardId: String): Flow<FirebaseResponse<List<PathProperties>>>

    suspend fun updatePathInDB(userId: String, boardId: String, paths: List<DBPathProperties>, pathIds: List<String>): FirebaseResponse<String>



}