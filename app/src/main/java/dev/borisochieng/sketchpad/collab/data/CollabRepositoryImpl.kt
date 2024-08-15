package dev.borisochieng.sketchpad.collab.data

import android.net.Uri
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.collab.data.models.DBOffset
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.collab.domain.toPathProperties
import dev.borisochieng.sketchpad.collab.domain.toSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class CollabRepositoryImpl : CollabRepository {
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
                Log.i("Board details", boardDetails.toString())

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
                if (snapshot.exists()) {
                    //iterate over each child and cast to DBSKetch class
                    for (boardSnapshot in snapshot.children) {
                        val board = boardSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        if (board != null) {
                            Log.i("Board", board.toString())

                            //convert firebase response to data level object
                            val dbSketch = DBSketch(
                                id = board["id"] as? String ?: "",
                                title = board["title"] as? String ?: "",
                                dateCreated = board["dateCreated"] as? String ?: "",
                                lastModified = board["lastModified"] as? String ?: "",
                                paths = (board["paths"] as? Map<*,*>)
                                    ?.mapNotNull { (pathId, pathObject) ->
                                        //ensure pathmap is a map
                                        if (pathId is String && pathObject is Map<*, *>) {
                                            DBPathProperties(
                                                alpha = pathObject["alpha"] as? Double ?: 0.0,
                                                color = pathObject["color"] as? String ?: "",
                                                eraseMode = pathObject["eraseMode"] as Boolean,
                                                start = (pathObject["start"] as? Map<*, *>)?.let { startMap ->
                                                    DBOffset(
                                                        y = startMap["y"] as? Double ?: 0.0,
                                                        x = startMap["x"] as? Double ?: 0.0
                                                    )
                                                } ?: DBOffset(0.0, 0.0),
                                                end = (pathObject["end"] as? Map<*, *>)?.let { endMap ->
                                                    DBOffset(
                                                        x = endMap["x"] as? Double ?: 0.0,
                                                        y = endMap["y"] as? Double ?: 0.0
                                                    )
                                                } ?: DBOffset(0.0, 0.0),
                                                strokeWidth = pathObject["strokeWidth"] as? Double
                                                    ?: 0.0

                                            )
                                        } else {
                                            null
                                        }
                                    } ?: emptyList()
                            )

                            Log.i("SketchInfo", "$dbSketch")
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


    override suspend fun listenForPathChanges(
        userId: String,
        boardId: String
    ): Flow<FirebaseResponse<List<PathProperties>>> =
        callbackFlow {
            val database = FirebaseDatabase.getInstance().reference
            val boardRef =
                database.child("Users").child(userId).child("boards").child(boardId).child("paths")

            val pathsFromDb = mutableListOf<DBPathProperties>()

            val listener = object : ChildEventListener {
                //monitor drawing of new lines
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val newPath = snapshot.getValue(DBPathProperties::class.java)

                    if (newPath != null) {
                        pathsFromDb.add(newPath)
                        val domainPaths = pathsFromDb.map { it.toPathProperties() }

                        //emit new values
                        trySend(FirebaseResponse.Success(domainPaths))
                    }
                }

                //monitor modification of existing lines
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val updatedPath = snapshot.getValue(DBPathProperties::class.java)
                    if (updatedPath != null) {
                        val index = pathsFromDb.indexOfFirst { it == updatedPath }

                        if (index != -1) {
                            pathsFromDb[index] = updatedPath
                            val domainPaths = pathsFromDb.map { it.toPathProperties() }

                            trySend(FirebaseResponse.Success(domainPaths))
                        }
                    }
                }

                //monitor removal of lines
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val removedPath = snapshot.getValue(DBPathProperties::class.java)
                    if (removedPath != null) {
                        pathsFromDb.removeAll { it == removedPath }
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

    override suspend fun fetchSingleSketch(
        userId: String,
        boardId: String
    ): FirebaseResponse<Sketch> =
        withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance().reference
                val boardRef = database.child("Users").child(userId).child("boards").child(boardId)

                //fetch board data
                val dataSnapshot = boardRef.get().await()

                //cast snapshot to DBSKetch
                val dbSketch = dataSnapshot.getValue(DBSketch::class.java)

                if (dbSketch != null) {
                    FirebaseResponse.Success(dbSketch.toSketch())
                } else {
                    FirebaseResponse.Error("Board not found")
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Fetch single sketch", e.message.toString())
                FirebaseResponse.Error("Failed to fetch sketch")

            }
        }


}