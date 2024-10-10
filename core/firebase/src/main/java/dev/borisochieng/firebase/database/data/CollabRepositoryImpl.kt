package dev.borisochieng.firebase.database.data

import android.net.Uri
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import dev.borisochieng.firebase.FirebaseResponse
import dev.borisochieng.firebase.database.domain.CollabRepository
import dev.borisochieng.firebase.database.domain.toPathProperties
import dev.borisochieng.firebase.database.domain.toSketch
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.firebase.model.BoardDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CollabRepositoryImpl : CollabRepository, KoinComponent {

    private val database by inject<FirebaseDatabase>()

    private val databaseRef = database.reference

    override suspend fun createSketch(
        userId: String,
        sketch: dev.borisochieng.firebase.model.SketchFirebase
    ): FirebaseResponse<BoardDetails> =
        withContext(Dispatchers.IO) {
            try {
                // generate board ids
                val boardId = databaseRef.child("Users").child(userId).child("boards").push().key

                if (boardId == null) {
                    Log.e("CreateSketch", "failed to generate board id")
                    //return early if board id has not been generated
                    return@withContext FirebaseResponse.Error("Failed to generate board id")
                }
                //create a map of generated path IDS to the corresponding DBProperties
                val pathData = sketch.paths.associateBy { path ->
                    path.id
                }

                val boardData = mapOf(
                    "id" to boardId,
                    "title" to sketch.title,
                    "paths" to pathData,
                    "dateCreated" to sketch.dateCreated,
                    "lastModified" to sketch.lastModified,
                    "isBackedUp" to true
                )


                //save sketch to database
                databaseRef.child("Users")
                    .child(userId)
                    .child("boards")
                    .child(boardId)
                    .setValue(boardData)
                    .await()

                val boardDetails = dev.borisochieng.firebase.model.BoardDetails(
                    userId = userId,
                    boardId = boardId,
                    pathIds = pathData.keys.toList()
                )
                Log.i("BoardDetails", boardDetails.toString())

                FirebaseResponse.Success(boardDetails)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Create sketch", e.message.toString())
                FirebaseResponse.Error("Something went wrong please try again")
            }

        }

    override suspend fun fetchExistingSketches(userId: String): FirebaseResponse<List<Sketch>> =
        withContext(Dispatchers.IO) {
            val userRef = database.getReference("Users").child(userId).child("boards")
            //userRef.keepSynced(true) //for disk persistentce

            return@withContext try {
                val snapshot = userRef.get().await()
                val sketchesList = mutableListOf<dev.borisochieng.firebase.model.SketchFirebase>()

                //check if snapshot has children
                if (snapshot.exists()) {
                    //iterate over each child and cast to DBSKetch class
                    for (boardSnapshot in snapshot.children) {
                        val board = boardSnapshot.getValue(object :
                            GenericTypeIndicator<Map<String, Any>>() {})
                        if (board != null) {
                            Log.i("Board", board.toString())
                            val dbSketch = deserializeDBSketch(board)

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
        paths: List<dev.borisochieng.firebase.model.PathPropertiesFirebase>,
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val pathRef =
                databaseRef.child("Users")
                    .child(userId)
                    .child("boards")
                    .child(boardId)
                    .child("paths")

            return@withContext try {
                val updates = mutableMapOf<String, Any>()

                //match path ids to PathPropertiesFirebase objects
                for (path in paths) {
                    val pathKey = path.id
                    updates[pathKey] = path
                }


                //update path in db
                pathRef.updateChildren(updates).await()

                FirebaseResponse.Success("Sketch updated")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Update path", e.message.toString())
                FirebaseResponse.Error("Error syncing sketches")
            }

        }


    override suspend fun listenForPathChanges(
        userId: String,
        boardId: String
    ): Flow<FirebaseResponse<List<PathProperties>>> =
        callbackFlow {
            val boardRef =
                databaseRef
                    .child("Users")
                    .child(userId).child("boards")
                    .child(boardId)
                    .child("paths")

            //boardRef.keepSynced(true)


            val listener = object : ChildEventListener {
                //monitor drawing of new lines
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val newPath = snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    val deserializedNewPath = newPath?.let { pathMap ->
                        deserializeDBPathProperties(pathObject = pathMap)
                    }

                    deserializedNewPath?.let {
                        //emit new paths
                        trySend(FirebaseResponse.Success(listOf(it.toPathProperties())))
                    }
                }

                //monitor modification of existing lines
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                    val updatedPath =
//                        snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
//                    val deserializedUpdatedPath = updatedPath?.let {
//                        deserializeDBPathProperties(
//                            pathObject = it
//                        )
//                    }
//                    deserializedUpdatedPath?.let {
//                        trySend(FirebaseResponse.Success(listOf(it.toPathProperties())))
//                    }
                }

                //monitor removal of lines
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val removedPath =
                        snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    val deserializedPath = removedPath?.let {
                        deserializeDBPathProperties(
                            pathObject = it
                        )
                    }
                    deserializedPath?.let {
                        trySend(FirebaseResponse.Success(listOf(it.toPathProperties())))
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    //ignore child moved for now
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

    override suspend fun deleteSketch(userId: String, boardId: String): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val boardRef = databaseRef.child("Users").child(userId).child("boards").child(boardId)

            return@withContext try {
                boardRef.removeValue().await()
                FirebaseResponse.Success("Sketch deleted")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Deleted Board", e.message.toString())
                FirebaseResponse.Error("Failed to delete board, please try again.")
            }
        }

    override suspend fun renameSketchInRemoteDB(
        userId: String,
        boardId: String,
        title: String
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val boardTitleRef = databaseRef
                .child("Users")
                .child(userId)
                .child("boards")
                .child(boardId)
                .child("title")

            return@withContext try {
                boardTitleRef.setValue(title).await()
                FirebaseResponse.Success("Sketch renamed successfully")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Rename sketch", e.message.toString())
                FirebaseResponse.Error("Cannot rename sketch, please try again")
            }
        }

    override suspend fun fetchSingleSketch(
        userId: String,
        boardId: String
    ): FirebaseResponse<Sketch> =
        withContext(Dispatchers.IO) {
            try {
                val boardRef =
                    databaseRef.child("Users").child(userId).child("boards").child(boardId)

                //boardRef.keepSynced(true)

                //fetch board data
                val dataSnapshot = boardRef.get().await()

                //cast snapshot to DBSKetch
                val dbSketch =
                    dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})

                val deserializedDBSketch = dbSketch?.let { deserializeDBSketch(it) }
                if (dbSketch != null) {
                    Log.i("Single Sketch", deserializedDBSketch.toString())
                    FirebaseResponse.Success(deserializedDBSketch?.toSketch())
                } else {
                    FirebaseResponse.Error("Canvas not found")
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Fetch single sketch", e.message.toString())
                FirebaseResponse.Error("Failed to fetch sketch")

            }
        }

    private fun deserializeDBSketch(board: Map<String, Any?>): dev.borisochieng.firebase.model.SketchFirebase {
        val paths = (board["paths"] as? Map<*, *>)?.mapNotNull { (pathId, pathObject) ->
            if (pathId is String && pathObject is Map<*, *>) {
                deserializeDBPathProperties(pathObject)
            } else {
                null
            }
        } ?: emptyList()

        return dev.borisochieng.firebase.model.SketchFirebase(
            id = board["id"] as? String ?: "",
            title = board["title"] as? String ?: "",
            dateCreated = board["dateCreated"] as? String ?: "",
            lastModified = board["lastModified"] as? String ?: "",
            paths = paths,
            isBackedUp = board["isBackedUp"] as? Boolean ?: true
        )
    }

    private fun deserializeDBPathProperties(
        pathObject: Map<*, *>
    ): dev.borisochieng.firebase.model.PathPropertiesFirebase {
        return dev.borisochieng.firebase.model.PathPropertiesFirebase(
            id = (pathObject["id"]) as? String ?: "",
            alpha = (pathObject["alpha"] as? Number)?.toFloat() ?: 0f,
            color = pathObject["color"] as? String ?: "",
            eraseMode = pathObject["textMode"] as? Boolean ?: false,
            start = (pathObject["start"] as? Map<*, *>)?.let { startMap ->
                dev.borisochieng.firebase.model.OffsetFirebase(
                    y = (startMap["y"] as? Number)?.toFloat() ?: 0f,
                    x = (startMap["x"] as? Number)?.toFloat() ?: 0f
                )
            } ?: dev.borisochieng.firebase.model.OffsetFirebase(0f, 0f),
            end = (pathObject["end"] as? Map<*, *>)?.let { endMap ->
                dev.borisochieng.firebase.model.OffsetFirebase(
                    x = (endMap["x"] as? Number)?.toFloat() ?: 0f,
                    y = (endMap["y"] as? Number)?.toFloat() ?: 0f
                )
            } ?: dev.borisochieng.firebase.model.OffsetFirebase(0f, 0f),
            strokeWidth = (pathObject["strokeWidth"] as? Number)?.toFloat() ?: 0f
        )
    }

}