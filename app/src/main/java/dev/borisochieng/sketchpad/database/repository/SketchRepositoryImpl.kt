package dev.borisochieng.sketchpad.database.repository

import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.SketchDao
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SketchRepositoryImpl: SketchRepository, KoinComponent {

	private val sketchDao by inject<SketchDao>()

	override fun getAllSketches(): Flow<List<Sketch>> {
		return sketchDao.getAllSketches()
	}

	override fun getSketch(sketchId: Int): Flow<Sketch> {
		return sketchDao.getSketch(sketchId)
	}

	override suspend fun saveSketch(sketch: Sketch) {
		return sketchDao.saveSketch(sketch)
	}

	override suspend fun refreshDatabase(sketches: List<Sketch>) {
		sketchDao.clearDatabase()
		return sketchDao.insertSketches(sketches)
	}

	override suspend fun updateSketch(sketch: Sketch) {
		return sketchDao.updateSketch(sketch)
	}

	override suspend fun deleteSketch(sketch: Sketch) {
		return sketchDao.deleteSketch(sketch)
	}

}
