package dev.borisochieng.sketchpad.collab.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
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
        sketch: DBSketch
    ): FirebaseResponse<BoardDetails> =
        withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance().reference
                // generate board ids
                val boardId = database.child("Users").child(userId).child("boards").push().key

                if (boardId == null) {
                   Log.e("CreateSketch", "failed to generate board id")
                    //return early if board id has not been generated
                    return@withContext FirebaseResponse.Error("Falied to generate board id")
                }
                //create a map of generated path IDS to the corresponding DBProperties
                val pathData = sketch.paths.associateBy { _ ->
                    val pathId = database.push().key ?: ""
                    pathId
                }

                    val boardData = mapOf(
                        "id" to boardId,
                        "title" to sketch.title,
                        "paths" to pathData,
                        "dateCreated" to sketch.dateCreated,
                        "lastModified" to sketch.lastModified
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
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Create sketch", e.message.toString())
                FirebaseResponse.Error("Something went wrong please try again")
            }

        }

    override suspend fun fetchExistingSketches(userId: String): FirebaseResponse<List<Sketch>> =
        withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("Users").child(userId).child("boards")

            return@withContext try {
                val snapshot = userRef.get().await()
                val sketchesList = mutableListOf<DBSketch>()

                //check if snapshot has children
                if(snapshot.exists()){
                    //iterate over each child and cast to DBSKetch class
                snapshot.children.forEach { boardSnapshot ->
                    val board = boardSnapshot.value as? Map<String, DBSketch>
                    if (board != null) {
                        val dbSketch = DBSketch(
                            id = board["id"] as? String ?: "",
                            title = board["title"] as? String ?: "",
                            dateCreated = board["dateCreated"] as? String ?: "",
                            lastModified = board["lastModified"] as? String ?: "",
                            paths = board["paths"] as? List<DBPathProperties> ?: emptyList()
                        )

                        sketchesList.add(dbSketch)
                    }
                }
            }
                val domainSketches = sketchesList.map { it.toSketch() }

                FirebaseResponse.Success(domainSketches)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Fetch sketches", e.message.toString())
                FirebaseResponse.Error("Cannot fetch sketches, check your internet connection and try again")

            }
        }

    override suspend fun updatePathInDB(
        userId: String,
        boardId: String,
        paths: List<DBPathProperties>,
        pathIds: List<String>
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val database = FirebaseDatabase.getInstance().reference
            val pathRef =
                database.child("Users")
                    .child(userId)
                    .child("boards")
                    .child(boardId)
                    .child("paths")

            return@withContext try {
                //create a map of exisiting paths in the database
                val existingPathsSnapshot = pathRef.get().await()
                val existingPaths = existingPathsSnapshot.children.associateBy { it.key ?: "" }
                suspendCancellableCoroutine<FirebaseResponse<String>> { continuation ->


                    //match path objects to pathids
                    val pathsMap = pathIds.zip(paths).toMap().toMutableMap()

                    //add any new paths not in the database
                    paths.forEachIndexed { index, path ->
                        val pathId = pathIds.getOrNull(index) ?: pathRef.push().key ?: ""
                        if (!existingPaths.containsKey(pathId)) {
                            pathsMap[pathId] = path
                        }
                    }

                    //update database with new paths or updated paths
                    pathRef.setValue(pathsMap.mapValues { it.value })
                        .addOnSuccessListener {
                            //suspend coroutine and resume when setValue() completes
                            continuation.resume(
                                FirebaseResponse.Success("")
                            )
                        }
                        .addOnFailureListener { error ->
                            error.printStackTrace()
                            Log.e("Add path to DB", error.message.toString())
                            continuation.resume(
                                FirebaseResponse.Error("Error syncing sketches")
                            )
                        }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Update path", e.message.toString())
                FirebaseResponse.Error("Something went wrong. Check your internet connection and try again")
            }

        }


    override suspend fun listenForSketchChanges(
        userId: String,
        boardId: String
    ): Flow<FirebaseResponse<List<PathProperties>>> =
        callbackFlow {
            val database = FirebaseDatabase.getInstance().reference
            val boardRef =
                database.child("Users").child(userId).child("boards").child(boardId).child("paths")

            val pathsFromDb = mutableListOf<DBPathProperties>()

            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val path = snapshot.getValue(DBPathProperties::class.java)

                    if (path != null) {
                        pathsFromDb.add(path)
                        val domainPaths = pathsFromDb.map { it.toPathProperties() }

                        //emit new values
                        trySend(FirebaseResponse.Success(domainPaths))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val path = snapshot.getValue(DBPathProperties::class.java)
                    if (path != null) {
                        val index = pathsFromDb.indexOfFirst { it == path }

                        if (index != -1) {
                            pathsFromDb[index] = path
                            val domainPaths = pathsFromDb.map { it.toPathProperties() }

                            trySend(FirebaseResponse.Success(domainPaths))
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val path = snapshot.getValue(DBPathProperties::class.java)
                    if (path != null) {
                        pathsFromDb.removeAll { it == path }
                        val domainPaths = pathsFromDb.map { it.toPathProperties() }
                        trySend(FirebaseResponse.Success(domainPaths))
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.e("Update Child", error.message)
                    trySend(FirebaseResponse.Error("Failed to fetch latest path: ${error.message}"))
                }

            }

            boardRef.addChildEventListener(listener)

            awaitClose {
                boardRef.removeEventListener(listener)
            }
        }.flowOn(Dispatchers.IO)

    override fun generateCollabUrl(userId: String, boardId: String): Uri {
            val baseUrl = "https://collaborate.jcsketchpad/"
            val collabUri = Uri.parse(baseUrl)
                .buildUpon()
                .appendQueryParameter("user_id", userId)
                .appendQueryParameter("board_id", boardId)
                .build()

            return collabUri
    }


}