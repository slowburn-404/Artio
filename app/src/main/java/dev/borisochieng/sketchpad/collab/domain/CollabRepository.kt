package dev.borisochieng.sketchpad.collab.domain

import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties

interface CollabRepository {

    suspend fun saveSketchToDB(userId: String, title: String, paths: List<DBPathProperties>): FirebaseResponse<String>



}