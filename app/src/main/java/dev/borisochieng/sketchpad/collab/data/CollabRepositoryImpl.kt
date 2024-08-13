package dev.borisochieng.sketchpad.collab.data

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.collab.domain.toPathProperties
import dev.borisochieng.sketchpad.collab.domain.toSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import kotlin.coroutines.resume

class CollabRepositoryImpl : CollabRepository, KoinComponent {
    override suspend fun createSketch(
        userId: String,
        title: String,
        paths: List<DBPathProperties>
    ): FirebaseResponse<BoardDetails> =
        withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance().reference
                // generate board ids
                val boardId = database.child("Users").child(userId).child("boards").push().key

                if (boardId != null) {
                    //create a map of generated path IDS to the corresponding DBProperties
                    val pathData = paths.associateBy { _ ->
                        val pathId = database.push().key ?: ""
                        pathId
                    }

                    val boardData = mapOf(
                        "title" to title,
                        "paths" to pathData
                    )


                        //save sketch to database
                    database.child("Users")
                        .child(userId)
                        .child("boards")
                        .child(boardId)
                        .setValue(boardData)
                        .await()

                    val boardDetails = BoardDetails(
                        userId = userId,
                        boardId = boardId,
                        pathIds = pathData.keys.toList()
                    )

                    FirebaseResponse.Success(boardDetails)
                } else {
                    FirebaseResponse.Error("Failed to generate sketch")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseResponse.Error("Something went wrong please try again")
            }

        }

    override suspend fun updatePathInDB(
        userId: String,
        boardId: String,
        path: PathProperties,
        pathId: String
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().reference
            val pathRef =
                database.child("Users")
                    .child(userId)
                    .child("boards")
                    .child(boardId)
                    .child("paths")
                    .child(pathId)

            return@withContext try {

                suspendCancellableCoroutine<FirebaseResponse<String>> { continuation ->
                    pathRef.setValue(path) //create a new path or update if it already exists
                        .addOnSuccessListener {
                            //suspend coroutine and resume when setValue() completes
                            continuation.resume(
                                FirebaseResponse.Success("")
                            )
                        }
                        .addOnFailureListener { error ->
                            error.printStackTrace()
                            continuation.resume(
                                FirebaseResponse.Error("Error syncing sketches")
                            )
                        }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseResponse.Error("Something went wrong. Check your internet connection and try again")
            }

        }


    override suspend fun listenForSketchChanges(
        userId: String,
        boardId: String
    ): Flow<FirebaseResponse<List<PathProperties>>> =
        callbackFlow {
            val database = FirebaseDatabase.getInstance().reference
            val boardRef = database.child("Users").child(userId).child("boards").child(boardId)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pathsFromDB = mutableListOf<DBPathProperties>()

                    snapshot.child("paths").children.forEach { pathSnapshot ->
                        val path = pathSnapshot.getValue(DBPathProperties::class.java)

                        if (path != null) {
                            pathsFromDB.add(path)
                        }
                    }

                    val domainPath = pathsFromDB.map { it.toPathProperties() }

                    trySend(FirebaseResponse.Success(domainPath))

                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(FirebaseResponse.Error("Failed to fetch latest path"))
                }

            }

            boardRef.addValueEventListener(listener)

            awaitClose {
                boardRef.removeEventListener(listener)
            }
        }.flowOn(Dispatchers.IO)

}