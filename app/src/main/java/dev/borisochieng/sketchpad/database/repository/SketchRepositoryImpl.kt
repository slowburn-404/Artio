package dev.borisochieng.sketchpad.database.repository

import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.SketchDao
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class SketchRepositoryImpl: SketchRepository, KoinComponent {

	private val sketchDao by inject<SketchDao>()

	override fun getAllSketches(): Flow<List<Sketch>> {
		return sketchDao.getAllSketches()
	}

	override fun getSketch(sketchId: String): Flow<Sketch> {
		return sketchDao.getSketch(sketchId)
	}

	override suspend fun saveSketch(sketch: Sketch) {
		return sketchDao.saveSketch(sketch)
	}

	override suspend fun updateSketch(sketch: Sketch) {
		val updatedSketch = Sketch(
			id = sketch.id,
			name = sketch.name,
			dateCreated = sketch.dateCreated,
			lastModified = Calendar.getInstance().time,
			paths = sketch.paths
		)
		return sketchDao.updateSketch(updatedSketch)
	}

	override suspend fun deleteSketch(sketch: Sketch) {
		return sketchDao.deleteSketch(sketch)
	}

}