package dev.borisochieng.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SketchDao {

	@Query("SELECT * FROM sketch ORDER BY lastModified DESC")
	fun getAllSketches(): Flow<List<dev.borisochieng.model.Sketch>>

	@Query("SELECT * FROM sketch WHERE id LIKE :sketchId")
	fun getSketch(sketchId: String): Flow<dev.borisochieng.model.Sketch>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun saveSketch(sketch: dev.borisochieng.model.Sketch)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSketches(sketches: List<dev.borisochieng.model.Sketch>)

	@Update
	suspend fun updateSketch(sketch: dev.borisochieng.model.Sketch)

	@Delete
	suspend fun deleteSketch(sketch: dev.borisochieng.model.Sketch)

	@Query("DELETE FROM sketch")
	suspend fun clearDatabase()

}