package dev.borisochieng.sketchpad.database.repository

import dev.borisochieng.sketchpad.database.MessageModel
import dev.borisochieng.sketchpad.database.Sketch
import kotlinx.coroutines.flow.Flow

interface SketchRepository {

	fun getAllSketches(): Flow<List<Sketch>>

	fun getSketch(sketchId: String): Flow<Sketch>

	suspend fun saveSketch(sketch: Sketch)

	suspend fun refreshDatabase(sketches: List<Sketch>)

	suspend fun updateSketch(sketch: Sketch)

	suspend fun deleteSketch(sketch: Sketch)

	suspend fun createChats(message1:String,boardId: String): Flow<Boolean>
	suspend fun getChats(boardId: String): Flow<List<MessageModel>>
	suspend fun loadChats(boardId: String): Flow<List<MessageModel>>
	suspend fun updateTypingStatus(isTyping: Boolean,boardId: String)
	suspend fun listenForTypingStatuses(boardId: String): Flow<List<String>>

}