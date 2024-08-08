package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.components.HomeTopBar
import dev.borisochieng.sketchpad.ui.navigation.Screens

@Composable
fun HomeScreen(
	savedSketches: List<Sketch>,
	navigate: (Screens) -> Unit
) {
	Scaffold(
		topBar = { HomeTopBar() }
	) { paddingValues ->
		LazyVerticalGrid(
			columns = GridCells.Adaptive(150.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 10.dp),
			contentPadding = paddingValues
		) {
			item {
				OutlinedButton(
					onClick = { navigate(Screens.SketchPad(null)) },
					modifier = Modifier
						.fillMaxWidth()
						.padding(20.dp, 16.dp)
				) {
					Icon(Icons.Rounded.Add, null, Modifier.padding(vertical = 14.dp))
					Text("Create New Sketch", Modifier.padding(start = 10.dp))
				}
			}

			items(savedSketches.size) { index ->
				val sketch = savedSketches[index]
				SketchPoster(
					sketch = sketch,
					onClick = { navigate(Screens.SketchPad(it)) }
				)
			}
		}
	}
}
