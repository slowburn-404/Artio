package dev.borisochieng.sketchpad.collab.data

import android.net.Uri
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.getValue
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
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

class CollabRepositoryImpl(private val database: FirebaseDatabase) : CollabRepository {

    private val databaseRef = database.reference

    override suspend fun createSketch(
        userId: String,
        sketch: DBSketch
    ): FirebaseResponse<BoardDetails> =
        withContext(Dispatchers.IO) {
            try {
                // generate board ids
                val boardId = databaseRef.child("Users").child(userId).child("boards").push().key

                if (boardId == null) {
                    Log.e("CreateSketch", "failed to generate board id")
                    //return early if board id has not been generated
                    return@withContext FirebaseResponse.Error("Falied to generate board id")
                }
                //create a map of generated path IDS to the corresponding DBProperties
                val pathData = sketch.paths.associateBy { _ ->
                    val pathId = databaseRef.push().key ?: ""
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
                databaseRef.child("Users")
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

            return@withContext try {
                val snapshot = userRef.get().await()
                val sketchesList = mutableListOf<DBSketch>()

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
        paths: List<DBPathProperties>,
        pathIds: List<String>
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            val pathRef =
                databaseRef.child("Users")
                    .child(userId)
                    .child("boards")
                    .child(boardId)
                    .child("paths")

            return@withContext try {
                    //match path objects to pathids
                    val pathsMap = pathIds.zip(paths).associate { it.first to it.second }

                pathRef.updateChildren(pathsMap.mapValues { it.value }).await()


                FirebaseResponse.Success("")

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
                databaseRef.child("Users").child(userId).child("boards").child(boardId)
                    .child("paths")

            val pathsFromDb = mutableListOf<DBPathProperties>()

            val listener = object : ChildEventListener {
                //monitor drawing of new lines
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val newPath = snapshot.getValue(object : GenericTypeIndicator<Map<*, *>>() {})
                    val deserializedNewPath = newPath?.let {
                        deserializeDBPathProperties(pathId = snapshot.key ?: "", pathObject = it)
                    }

                    if (deserializedNewPath != null) {
                        pathsFromDb.add(deserializedNewPath)
                        val domainPaths = pathsFromDb.map { it.toPathProperties() }

                        //emit new values
                        trySend(FirebaseResponse.Success(domainPaths))
                    }
                }

                //monitor modification of existing lines
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val updatedPath = snapshot.getValue(object : GenericTypeIndicator<Map<*, *>>() {})
                    val deserializedUpdatedPath = updatedPath?.let{ deserializeDBPathProperties(pathId = snapshot.key ?: "", pathObject = it)}
                    if (updatedPath != null) {
                        val index = pathsFromDb.indexOfFirst { it == deserializedUpdatedPath}

                        if (index != -1 && deserializedUpdatedPath != null) {
                            pathsFromDb[index] = deserializedUpdatedPath
                            val domainPaths = pathsFromDb.map { it.toPathProperties() }

                            trySend(FirebaseResponse.Success(domainPaths))
                        }
                    }
                }

                //monitor removal of lines
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val removedPath = snapshot.getValue(object : GenericTypeIndicator<Map<*, *>>() {})
                    val deserializedPath = removedPath?.let { deserializeDBPathProperties(pathId = snapshot.key ?: "", pathObject = it) }
                    if (removedPath != null) {
                        pathsFromDb.removeAll { it == deserializedPath }
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

    override suspend fun renameSketchInRemoteDB(userId: String, boardId: String, title: String): FirebaseResponse<String> =
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
                Log.e("Rename sketch",e.message.toString())
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

                //fetch board data
                val dataSnapshot = boardRef.get().await()

                //cast snapshot to DBSKetch
                val dbSketch =
                    dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})

                val deserializedDBSketch = dbSketch?.let { deserializeDBSketch(it) }
                if (dbSketch != null) {
                    FirebaseResponse.Success(deserializedDBSketch?.toSketch())
                } else {
                    FirebaseResponse.Error("Board not found")
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Fetch single sketch", e.message.toString())
                FirebaseResponse.Error("Failed to fetch sketch")

            }
        }

    private fun deserializeDBSketch(board: Map<String, Any?>): DBSketch {
        val paths = (board["paths"] as? Map<*, *>)?.mapNotNull { (pathId, pathObject) ->
            if (pathId is String && pathObject is Map<*, *>) {
                deserializeDBPathProperties(pathId, pathObject)
            } else {
                null
            }
        } ?: emptyList()

        return DBSketch(
            id = board["id"] as? String ?: "",
            title = board["title"] as? String ?: "",
            dateCreated = board["dateCreated"] as? String ?: "",
            lastModified = board["lastModified"] as? String ?: "",
            paths = paths
        )
    }

    private fun deserializeDBPathProperties(
        pathId: String,
        pathObject: Map<*, *>
    ): DBPathProperties {
        return DBPathProperties(
            alpha = (pathObject["alpha"] as? Number)?.toFloat() ?: 0f,
            color = pathObject["color"] as? String ?: "",
            eraseMode = pathObject["eraseMode"] as? Boolean ?: false,
            start = (pathObject["start"] as? Map<*, *>)?.let { startMap ->
                DBOffset(
                    y = (startMap["y"] as? Number)?.toFloat() ?: 0f,
                    x = (startMap["x"] as? Number)?.toFloat() ?: 0f
                )
            } ?: DBOffset(0f, 0f),
            end = (pathObject["end"] as? Map<*, *>)?.let { endMap ->
                DBOffset(
                    x = (endMap["x"] as? Number)?.toFloat() ?: 0f,
                    y = (endMap["y"] as? Number)?.toFloat() ?: 0f
                )
            } ?: DBOffset(0f, 0f),
            strokeWidth = (pathObject["strokeWidth"] as? Number)?.toFloat() ?: 0f
        )
    }


}