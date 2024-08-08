package dev.borisochieng.sketchpad.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SketchDao {

	@Query("SELECT * FROM sketch ORDER BY lastModified DESC")
	fun getAllSketches(): Flow<List<Sketch>>

	@Query("SELECT * FROM sketch WHERE id LIKE :sketchId")
	fun getSketch(sketchId: Int): Flow<Sketch>

	@Insert
	suspend fun saveSketch(sketch: Sketch)

	@Insert
	suspend fun updateSketch(sketch: Sketch)

	@Delete
	suspend fun deleteSketch(sketch: Sketch)

}