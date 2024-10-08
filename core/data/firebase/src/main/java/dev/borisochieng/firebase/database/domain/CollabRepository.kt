package dev.borisochieng.firebase.database.domain

import android.net.Uri
import dev.borisochieng.firebase.auth.data.FirebaseResponse
import dev.borisochieng.firebase.database.data.models.BoardDetails
import dev.borisochieng.firebase.database.data.models.DBPathProperties
import dev.borisochieng.firebase.database.data.models.DBSketch
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import kotlinx.coroutines.flow.Flow

interface CollabRepository {

    suspend fun createSketch(
        userId: String,
        sketch: DBSketch,
    ): dev.borisochieng.firebase.auth.data.FirebaseResponse<BoardDetails>

    suspend fun fetchExistingSketches(userId: String): dev.borisochieng.firebase.auth.data.FirebaseResponse<List<Sketch>>

    suspend fun listenForPathChanges(userId: String, boardId: String): Flow<dev.borisochieng.firebase.auth.data.FirebaseResponse<List<PathProperties>>>

    suspend fun fetchSingleSketch(userId: String, boardId: String): dev.borisochieng.firebase.auth.data.FirebaseResponse<Sketch>

    suspend fun updatePathInDB(userId: String, boardId: String, paths: List<DBPathProperties>): dev.borisochieng.firebase.auth.data.FirebaseResponse<String>

    fun generateCollabUrl(userId: String, boardId: String): Uri

    suspend fun deleteSketch(userId: String, boardId : String): dev.borisochieng.firebase.auth.data.FirebaseResponse<String>

    suspend fun renameSketchInRemoteDB(userId: String, boardId: String, title: String): dev.borisochieng.firebase.auth.data.FirebaseResponse<String>

}