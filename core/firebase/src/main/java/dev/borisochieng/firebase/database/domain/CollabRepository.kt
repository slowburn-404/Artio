package dev.borisochieng.firebase.database.domain

import android.net.Uri
import dev.borisochieng.firebase.FirebaseResponse
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.firebase.model.BoardDetails
import kotlinx.coroutines.flow.Flow

interface CollabRepository {

    suspend fun createSketch(
        userId: String,
        sketch: dev.borisochieng.firebase.model.SketchFirebase,
    ): FirebaseResponse<BoardDetails>

    suspend fun fetchExistingSketches(userId: String): FirebaseResponse<List<Sketch>>

    suspend fun listenForPathChanges(userId: String, boardId: String): Flow<FirebaseResponse<List<PathProperties>>>

    suspend fun fetchSingleSketch(userId: String, boardId: String): FirebaseResponse<Sketch>

    suspend fun updatePathInDB(userId: String, boardId: String, paths: List<dev.borisochieng.firebase.model.PathPropertiesFirebase>): FirebaseResponse<String>

    fun generateCollabUrl(userId: String, boardId: String): Uri

    suspend fun deleteSketch(userId: String, boardId : String): FirebaseResponse<String>

    suspend fun renameSketchInRemoteDB(userId: String, boardId: String, title: String): FirebaseResponse<String>

}