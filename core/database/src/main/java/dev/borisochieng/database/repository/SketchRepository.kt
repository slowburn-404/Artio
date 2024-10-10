package dev.borisochieng.database.repository

import dev.borisochieng.model.MessageModel
import dev.borisochieng.model.Sketch
import kotlinx.coroutines.flow.Flow

interface SketchRepository {

	fun getAllSketches(): Flow<List<dev.borisochieng.model.Sketch>>

	fun getSketch(sketchId: String): Flow<dev.borisochieng.model.Sketch>

	suspend fun saveSketch(sketch: dev.borisochieng.model.Sketch)

	suspend fun refreshDatabase(sketches: List<dev.borisochieng.model.Sketch>)

	suspend fun updateSketch(sketch: dev.borisochieng.model.Sketch)

	suspend fun deleteSketch(sketch: dev.borisochieng.model.Sketch)

	suspend fun createChats(message1:String,boardId: String): Flow<Boolean>
	suspend fun getChats(boardId: String): Flow<List<dev.borisochieng.model.MessageModel>>
	suspend fun loadChats(boardId: String): Flow<List<dev.borisochieng.model.MessageModel>>
	suspend fun updateTypingStatus(isTyping: Boolean,boardId: String)
	suspend fun listenForTypingStatuses(boardId: String): Flow<List<String>>

}