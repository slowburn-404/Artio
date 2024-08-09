package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			OutlinedButton(
				onClick = { navigate(Screens.SketchPad("0")) },
				modifier = Modifier
					.fillMaxWidth()
					.padding(20.dp, 16.dp)
			) {
				Icon(Icons.Rounded.Add, null, Modifier.padding(vertical = 14.dp))
				Text("Create New Sketch", Modifier.padding(start = 10.dp))
			}
			LazyVerticalGrid(
				columns = GridCells.Adaptive(150.dp),
				modifier = Modifier.padding(start = 10.dp),
				contentPadding = PaddingValues(bottom = 100.dp)
			) {
				items(savedSketches.size) { index ->
					val sketch = savedSketches[index]
					SketchPoster(
						sketch = sketch,
						onClick = { navigate(Screens.SketchPad(it)) }
					)
				}
			}

			if (savedSketches.isEmpty()) { EmptyScreen() }
		}
	}
}

@Composable
private fun EmptyScreen() {
	val displayText = "No drawings saved"

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(30.dp)
			.alpha(0.7f),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = Icons.Rounded.History,
			contentDescription = displayText,
			modifier = Modifier
				.padding(bottom = 20.dp)
				.size(100.dp)
		)
		Text(
			text = displayText,
			fontSize = 24.sp,
			fontStyle = FontStyle.Italic,
			fontWeight = FontWeight.Medium,
			textAlign = TextAlign.Center
		)
	}
}
