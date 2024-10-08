package dev.borisochieng.database.database

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
	fun getAllSketches(): Flow<List<Sketch>>

	@Query("SELECT * FROM sketch WHERE id LIKE :sketchId")
	fun getSketch(sketchId: String): Flow<Sketch>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun saveSketch(sketch: Sketch)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSketches(sketches: List<Sketch>)

	@Update
	suspend fun updateSketch(sketch: Sketch)

	@Delete
	suspend fun deleteSketch(sketch: Sketch)

	@Query("DELETE FROM sketch")
	suspend fun clearDatabase()

}