package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.utils.Extensions.formatDate

@Composable
fun SketchPoster(
	sketch: Sketch,
	modifier: Modifier = Modifier,
	onClick: (Int) -> Unit
) {
	Column(
		modifier = modifier
			.padding(end = 10.dp, bottom = 10.dp)
			.clip(MaterialTheme.shapes.large)
			.background(MaterialTheme.colorScheme.scrim)
			.clickable { onClick(sketch.id) },
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Image(
			bitmap = sketch.art.asImageBitmap(),
			contentDescription = null,
			modifier = Modifier
				.fillMaxWidth()
				.height(120.dp)
		)
		Text(
			text = sketch.name,
			modifier = Modifier.padding(top = 10.dp)
		)
		Text(
			text = sketch.lastModified.formatDate(),
			modifier = Modifier
				.padding(bottom = 10.dp)
				.alpha(0.8f),
			fontSize = 14.sp
		)
	}
}