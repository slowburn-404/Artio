package dev.borisochieng.sketchpad.collab.data

import com.google.firebase.database.FirebaseDatabase
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CollabRepositoryImpl : CollabRepository {
    override suspend fun saveSketchToDB(
        userId: String,
        title: String,
        paths: List<DBPathProperties>
    ): FirebaseResponse<String> =
        withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance().reference
                val boardId = database.child("Users").child(userId).child("boards").push().key

                if (boardId != null) {
                    val boardData = mapOf(
                        "title" to title,
                        "paths" to paths.associateBy {
                            database.push().key ?: ""
                        }
                    )


                    database.child("Users")
                        .child(userId)
                        .child("boards")
                        .child(boardId)
                        .setValue(boardData)
                        .await()

                    FirebaseResponse.Success("Sketch saved")
                } else {
                    FirebaseResponse.Error("Failed to generate sketch")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseResponse.Error("Something went wrong please try again")
            }

        }
}