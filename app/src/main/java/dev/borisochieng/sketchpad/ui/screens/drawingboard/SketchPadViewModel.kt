package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dev.borisochieng.sketchpad.collab.domain.CollabRepository
import dev.borisochieng.sketchpad.collab.data.toDBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.repository.SketchRepository
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SketchPadViewModel : ViewModel(), KoinComponent {

	private val sketchRepository by inject<SketchRepository>()

	private val collabRepository by inject<CollabRepository>()

	private val firebaseUser by inject<FirebaseUser>()

	var sketch by mutableStateOf<Sketch?>(null); private set

	fun fetchSketch(sketchId: Int) {
		sketch = null
		viewModelScope.launch {
			sketchRepository.getSketch(sketchId).collect {
				sketch = it
			}
		}
	}

	fun actions(action: SketchPadActions) {
		when (action) {
			is SketchPadActions.SaveSketch -> saveSketch(action.sketch)
			is SketchPadActions.UpdateSketch -> updateSketch(action.paths)
			SketchPadActions.SketchClosed -> sketch = null
			SketchPadActions.SketchClosed -> sketch = null
		}
	}

	private fun saveSketch(sketch: Sketch) {
		viewModelScope.launch {
			sketchRepository.saveSketch(sketch)
			saveSketchToRemoteDb(sketch)
		}
	}

	private fun saveSketchToRemoteDb(sketch: Sketch) {
		viewModelScope.launch {
			try {
				val dbSketch = sketch.toDBSketch()
				collabRepository.saveSketchToDB(
					userId = firebaseUser.uid,
					title = dbSketch.title,
					paths = dbSketch.paths
				)
			} catch (e: Exception) {
				Log.e("RemoteDbError", "User is not logged in", e)
			}
		}
	}

	private fun updateSketch(paths: List<PathProperties>) {
		viewModelScope.launch {
			if (sketch == null) return@launch
			val updatedSketch = Sketch(
				id = sketch!!.id,
				name = sketch!!.name,
				dateCreated = sketch!!.dateCreated,
				pathList = paths
			)
			sketchRepository.updateSketch(updatedSketch)
		}
	}

}