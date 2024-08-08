package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import dev.borisochieng.sketchpad.utils.Extensions.toPath
import io.ak1.drawbox.PathWrapper
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SketchPadViewModel : ViewModel(), KoinComponent {

	private val sketchRepository by inject<SketchRepository>()

	var sketch by mutableStateOf<Sketch?>(null); private set

	fun fetchSketch(sketchId: Int?) {
		sketch = null
		if (sketchId == null) return
		viewModelScope.launch {
			sketchRepository.getSketch(sketchId).collect {
				sketch = it
			}
		}
	}

	fun actions(action: SketchPadActions) {
		when (action) {
			is SketchPadActions.SaveSketch -> saveSketch(action.sketch)
			is SketchPadActions.UpdateSketch -> updateSketch(action.art, action.backgroundColor, action.paths)
			is SketchPadActions.DeleteSketch -> deleteSketch(action.sketch)
		}
	}

	private fun saveSketch(sketch: Sketch) {
		viewModelScope.launch {
			sketchRepository.saveSketch(sketch)
		}
	}

	private fun updateSketch(
		art: Bitmap,
		backgroundColor: Color,
		paths: List<PathWrapper>
	) {
		viewModelScope.launch {
			if (sketch == null) return@launch
			val updatedSketch = Sketch(
				id = sketch!!.id,
				name = sketch!!.name,
				dateCreated = sketch!!.dateCreated,
				art = art,
				backgroundColor = backgroundColor,
				pathList = paths.map { it.toPath() }
			)
			sketchRepository.updateSketch(updatedSketch)
		}
	}

	private fun deleteSketch(sketchToDelete: Sketch) {
		viewModelScope.launch {
			sketchRepository.deleteSketch(sketchToDelete)
		}
	}

}