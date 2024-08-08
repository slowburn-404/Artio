package dev.borisochieng.sketchpad.database.repository

import dev.borisochieng.sketchpad.database.Sketch
import kotlinx.coroutines.flow.Flow

interface SketchRepository {

	fun getAllSketches(): Flow<List<Sketch>>

	fun getSketch(sketchId: Int): Flow<Sketch>

	suspend fun saveSketch(sketch: Sketch)

	suspend fun updateSketch(sketch: Sketch)

	suspend fun deleteSketch(sketch: Sketch)

}